import pulp
import requests
from datetime import datetime
import json
import itertools
import math
from itertools import combinations, permutations # Need permutations for routing within a group
import time # To measure execution time
import sys
from collections import defaultdict


# 🔁 Lecture des commandes sélectionnées depuis un fichier
with open("commandes_selectionnees.json", "r", encoding="utf-8") as f:
    commandes_list = json.load(f)


# Convertir en dictionnaire avec id comme clé et normaliser les clés en minuscules
commandes = {}
for cmd in commandes_list:
    normalized = {k.lower(): v for k, v in cmd.items()}
    commandes[normalized["id"]] = normalized



# --- Lecture des données ---

# 🔧 Normaliser les clés des commandes
for cmd in commandes.values():
    keys = list(cmd.keys())
    for k in keys:
        cmd[k.lower()] = cmd.pop(k)

#  Vérification après chargement et normalisation
required_fields = ['gasoil', 'essence', 'pompe', 'solo', 'normal', 'posilat', 'posilong', 'quantitetotale']
for cid, cmd in commandes.items():
    for field in required_fields:
        if field not in cmd:
            print(f"❌ Champ manquant: {field} dans commande {cid}")


# --- Récupération des camions depuis l’API ---
#  Lecture des camions disponibles depuis un fichier
with open("camions_disponibles.json", "r", encoding="utf-8") as f:
    camions_list = json.load(f)

camions = {}

for truck in camions_list:
    depot = truck.get("depot", {})  # Correctement utiliser la clé chaîne
    normalized_truck = {k.lower(): v for k, v in truck.items()}  # manquait `:` dans la compréhension de dict
    normalized_truck["depot_code"] = depot.get("code")  # Utiliser des chaînes pour les clés
    normalized_truck["depot_posilat"] = depot.get("posilat")
    normalized_truck["depot_posilong"] = depot.get("posilong")
    matricule = normalized_truck.get("matricule")
    if matricule:
        camions[matricule] = normalized_truck


# --- Étape  Récupérer automatiquement le dépôt utilisé par les camions ---
depot_code = None
for truck in camions.values():
    if "depot_code" in truck and truck["depot_code"]:
        depot_code = truck["depot_code"]
        break

if depot_code is None:
    raise ValueError(" Aucun camion n'est associé à un dépôt. Impossible de continuer.")

# Requête pour obtenir les détails du dépôt via API Gateway
depot_response = requests.get("")
if depot_response.status_code != 200:
    raise RuntimeError(f" Erreur lors de la récupération des dépôts : {depot_response.status_code}")

all_depots = depot_response.json()
depot = next((d for d in all_depots if d["code"] == depot_code), None)

if depot is None or "posilat" not in depot or "posilong" not in depot:
    raise ValueError(" Impossible de déterminer les coordonnées du dépôt depuis le code.")

print(f"Dépôt utilisé automatiquement : {depot['code']} ({depot['posilat']}, {depot['posilong']})")







GRAPHOPPER_API_KEY = "" 
TEMPS_SERVICE_S = 3600 # 1 heure de service PAR CLIENT dans une tournée groupée
MAX_TEMPS_TRAVAIL_S = 8 * 3600
FLOAT_TOLERANCE = 1e-6

# --- Fonctions Auxiliaires (get_route_info, find_compartment_allocation - modifiée) ---

# (get_route_info reste identique)
def get_route_info(start_lat, start_lon, end_lat, end_lon, vehicle_profile="car"):
    """Interroge l'API GraphHopper pour obtenir distance (m) et durée (s)."""
    url = ""
    query = {
        "key": GRAPHOPPER_API_KEY,
        "point": [f"{start_lat},{start_lon}", f"{end_lat},{end_lon}"],
        "vehicle": vehicle_profile,
        "calc_points": "false",
        "details": ["road_class", "surface"]
    }
    try:
        response = requests.get(url, params=query)
        response.raise_for_status()
        data = response.json()
        if "paths" in data and len(data["paths"]) > 0:
            path = data["paths"][0]
            return path["distance"], path["time"] / 1000
        else:
            # print(f"Avertissement: Réponse GraphHopper sans chemin pour {start_lat},{start_lon} -> {end_lat},{end_lon}")
            return None, None
    except requests.exceptions.RequestException as e:
        print(f"Erreur API GraphHopper ({start_lat},{start_lon} -> {end_lat},{end_lon}): {e}")
        return None, None
    except Exception as e:
        print(f"Erreur traitement réponse GraphHopper: {e}")
        return None, None

# find_compartment_allocation reste TRÈS similaire, mais on l'appelle avec le TOTAL DG/DE du groupe
def find_compartment_allocation(dg_to_load, de_to_load, compartments_with_indices, truck_capacity):
    """
    Tente de trouver une allocation spécifique des quantités DG/DE dans les compartiments.
    Retourne un dictionnaire {"DG": {comp_idx: qty, ...}, "DE": {comp_idx: qty, ...}} ou None si impossible.
    """
    # Vérifications initiales
    if dg_to_load < -FLOAT_TOLERANCE or de_to_load < -FLOAT_TOLERANCE:
        return None
    total_compartment_capacity = sum(c[0] for c in compartments_with_indices)
    # Check against actual compartment sum AND nominal truck capacity
    if dg_to_load + de_to_load > total_compartment_capacity + FLOAT_TOLERANCE or \
            dg_to_load + de_to_load > truck_capacity + FLOAT_TOLERANCE:
        return None

    num_comps = len(compartments_with_indices)
    comp_caps = [c[0] for c in compartments_with_indices]
    comp_indices = [c[1] for c in compartments_with_indices] # Indices originaux

    memo = {} # Pour la mémoïsation

    def can_place_recursive(comp_idx_in_list, dg_remaining, de_remaining, current_alloc_dg, current_alloc_de):
        state = (comp_idx_in_list, round(dg_remaining, 5), round(de_remaining, 5))
        if state in memo:
            return memo[state]

        if dg_remaining < FLOAT_TOLERANCE and de_remaining < FLOAT_TOLERANCE:
            return True, current_alloc_dg, current_alloc_de
        if comp_idx_in_list == num_comps:
            return False, None, None

        current_comp_cap = comp_caps[comp_idx_in_list]
        current_comp_orig_idx = comp_indices[comp_idx_in_list]

        # Option 1: Skip current compartment
        res_skip, final_dg_skip, final_de_skip = can_place_recursive(
            comp_idx_in_list + 1, dg_remaining, de_remaining,
            current_alloc_dg.copy(), current_alloc_de.copy()
        )
        if res_skip:
            memo[state] = (True, final_dg_skip, final_de_skip)
            return True, final_dg_skip, final_de_skip

            # Option 2: Try placing DG
        if dg_remaining > FLOAT_TOLERANCE and current_comp_cap > FLOAT_TOLERANCE:
            qty_to_place_dg = min(dg_remaining, current_comp_cap)
            new_alloc_dg = current_alloc_dg.copy()
            new_alloc_dg[current_comp_orig_idx] = qty_to_place_dg
            res_dg, final_dg_dg, final_de_dg = can_place_recursive(
                comp_idx_in_list + 1, dg_remaining - qty_to_place_dg, de_remaining,
                new_alloc_dg, current_alloc_de.copy()
            )
            if res_dg:
                memo[state] = (True, final_dg_dg, final_de_dg)
                return True, final_dg_dg, final_de_dg

        # Option 3: Try placing DE
        if de_remaining > FLOAT_TOLERANCE and current_comp_cap > FLOAT_TOLERANCE:
            qty_to_place_de = min(de_remaining, current_comp_cap)
            new_alloc_de = current_alloc_de.copy()
            new_alloc_de[current_comp_orig_idx] = qty_to_place_de
            res_de, final_dg_de, final_de_de = can_place_recursive(
                comp_idx_in_list + 1, dg_remaining, de_remaining - qty_to_place_de,
                current_alloc_dg.copy(), new_alloc_de
            )
            if res_de:
                memo[state] = (True, final_dg_de, final_de_de)
                return True, final_dg_de, final_de_de

        memo[state] = (False, None, None)
        return False, None, None

    # Lancer la récursion
    success, final_alloc_dg, final_alloc_de = can_place_recursive(0, dg_to_load, de_to_load, {}, {})

    if success:
        final_alloc_dg = {k: v for k, v in final_alloc_dg.items() if v > FLOAT_TOLERANCE}
        final_alloc_de = {k: v for k, v in final_alloc_de.items() if v > FLOAT_TOLERANCE}
        # Final checks (optional but good practice)
        if set(final_alloc_dg.keys()).intersection(set(final_alloc_de.keys())):
            # print(f"ERREUR INTERNE find_compartment_allocation: Chevauchement!") # Should not happen with this logic
            return None
        # Check individual compartment capacity again (should be guaranteed by recursion)
        # ... (add checks similar to original if needed) ...
        return {"gasoil": final_alloc_dg, "essence": final_alloc_de}
    else:
        return None


# --- Nouvelle Fonction: Calculer Route pour un Groupe ---
memo_route = {} # Cache pour les segments de route

def get_route_segment(lat1, lon1, lat2, lon2):
    """Wrapper pour get_route_info avec cache."""
    key = (round(lat1, 6), round(lon1, 6), round(lat2, 6), round(lon2, 6))
    if key in memo_route:
        return memo_route[key]
    dist, dur = get_route_info(lat1, lon1, lat2, lon2)
    if dist is not None:
        memo_route[key] = (dist, dur)
    return dist, dur

def calculate_group_route(group_cmd_ids, commandes, depot):
    """
    Calcule la distance et la durée optimales pour une tournée (en testant toutes les permutations).
    """
    min_total_dist = float('inf')
    best_duration = float('inf')

    for perm in permutations(group_cmd_ids):
        total_dist_m = 0
        total_dur_s = 0
        points = [(depot["posilat"], depot["posilong"])]
        points.extend([(commandes[c_id]["posilat"], commandes[c_id]["posilong"]) for c_id in perm])
        points.append((depot["posilat"], depot["posilong"]))

        route_valid = True
        for i in range(len(points) - 1):
            lat1, lon1 = points[i]
            lat2, lon2 = points[i+1]
            dist, dur = get_route_segment(lat1, lon1, lat2, lon2)
            if dist is None:
                route_valid = False
                break
            total_dist_m += dist
            total_dur_s += dur

        if route_valid:
            total_dur_s += len(perm) * TEMPS_SERVICE_S
            if total_dist_m < min_total_dist:
                min_total_dist = total_dist_m
                best_duration = total_dur_s

    if min_total_dist < float('inf'):
        return min_total_dist / 1000.0, best_duration
    else:
        return None, None

# --- Nouvelle Fonction: Trouver les Groupes Valides ---
def find_valid_group_assignments(commandes, camions, depot, max_group_size=3):
    """
    Identifie toutes les combinaisons (groupe_commandes, camion) valides
    qui remplissent EXACTEMENT la capacité du camion, respectent les contraintes
    de base (pompe, type), de temps de travail, et d'allocation compartiments.
    """

    # 🔒 Sécurité : interdire les commandes non sélectionnées
    commandes_selectionnees_ids = set(commandes.keys())

    valid_assignments = []
    commande_ids = list(commandes_selectionnees_ids)

    camion_ids = list(camions.keys())
    print(f"Recherche de groupes valides (max size {max_group_size})...")
    start_time = time.time()

    for k_id in camion_ids:
        truck = camions[k_id]
        truck_capacity = truck["capacite"]
        compartments_with_indices = [(cap, idx) for idx, cap in enumerate(truck["compartiment"])]
        print(f"  Analyse pour Camion {k_id} (Cap: {truck_capacity:.1f}T)")

        # Itérer sur les tailles de groupe possibles (1 à max_group_size)
        for r in range(1, min(max_group_size, len(commande_ids)) + 1):
            # print(f"    Test groupes de taille {r}...")
            count_tested = 0
            count_passed_basic = 0
            count_passed_alloc = 0
            count_passed_route = 0

            for group_tuple in combinations(commande_ids, r):
                count_tested += 1
                group_cmds_data = [commandes[cid] for cid in group_tuple]

                commandes_totales_dg = sum(cmd["gasoil"] for cmd in group_cmds_data)
                commandes_totales_de = sum(cmd["essence"] for cmd in group_cmds_data)

                # 1. Vérification Compatibilité de Base (Pompe, Solo/Normal)
                compatible = True
                for cmd_data in group_cmds_data:
                    # Pompe
                    if cmd_data["pompe"] == 1 and truck["pompe"] == 0:
                        compatible = False; break
                    # Type Solo/Normal (logique simplifiée: si une cmd exige X, le camion doit être X)
                    station_requires_solo = cmd_data.get("solo", 0) == 1 and cmd_data.get("normal", 0) == 0
                    station_requires_normal = cmd_data.get("normal", 0) == 1 and cmd_data.get("solo", 0) == 0
                    truck_is_solo = truck.get("solo", 0) == 1
                    truck_is_normal = truck.get("normal", 0) == 1
                    if station_requires_solo and not truck_is_solo:
                        compatible = False; break
                    if station_requires_normal and not truck_is_normal:
                        compatible = False; break
                if not compatible:
                    continue
                count_passed_basic += 1

                # 2. Calculer Flexibilité Combinée
                total_min_dg = sum(max(0, cmd["gasoil"] + cmd["tdgmin"]) for cmd in group_cmds_data)
                total_max_dg = sum(max(0, cmd["gasoil"] + cmd["tdgmax"]) for cmd in group_cmds_data) # Max ne peut être < 0
                total_min_de = sum(max(0, cmd["essence"] + cmd["tdemin"]) for cmd in group_cmds_data)
                total_max_de = sum(max(0, cmd["essence"] + cmd["tdemax"]) for cmd in group_cmds_data) # Max ne peut être < 0

                # 3. Vérifier si la capacité EXACTE est atteignable
                #    Trouver la plage de DG possible pour atteindre EXACTEMENT la capacité
                min_possible_dg_for_exact_fill = max(total_min_dg, truck_capacity - total_max_de)
                max_possible_dg_for_exact_fill = min(total_max_dg, truck_capacity - total_min_de)

                found_exact_allocatable_combo = False
                exact_dg = -1
                exact_de = -1
                allocation_map = None

                # Itérer sur les DG possibles pour voir si une allocation fonctionne
                # NOTE: Itérer sur des flottants est délicat. On pourrait tester les bornes
                # et peut-être un point milieu, ou utiliser une granularité.
                # Pour l'instant, testons les bornes DG calculées.


                # Préparer la liste des DG à tester, en priorisant celle sans flexibilité si elle est valide
                possible_dg_to_test = []

                # Est-ce que la valeur exacte sans flexibilité tombe dans la plage autorisée ?
                if (
                        min_possible_dg_for_exact_fill - FLOAT_TOLERANCE <= commandes_totales_dg <= max_possible_dg_for_exact_fill + FLOAT_TOLERANCE
                        and total_min_de - FLOAT_TOLERANCE <= commandes_totales_de <= total_max_de + FLOAT_TOLERANCE
                ):
                    possible_dg_to_test.append(round(commandes_totales_dg, 5))  # Priorité à la valeur "par défaut"

                # 2. Ajouter les bornes min et max si différentes
                for val in [round(min_possible_dg_for_exact_fill, 5), round(max_possible_dg_for_exact_fill, 5)]:
                    if val not in possible_dg_to_test:
                        possible_dg_to_test.append(val)

                        # On pourrait ajouter d'autres points ici si nécessaire

                for test_dg in possible_dg_to_test:
                    test_de = truck_capacity - test_dg
                    # Vérifier si ce DE est dans les bornes globales du groupe
                    if total_min_de <= test_de <= total_max_de + FLOAT_TOLERANCE and \
                            total_min_dg <= test_dg <= total_max_dg + FLOAT_TOLERANCE: # Re-check DG bounds too
                        # Tenter l'allocation compartiment
                        current_allocation_map = find_compartment_allocation(
                            test_dg, test_de, compartments_with_indices, truck_capacity
                        )
                        if current_allocation_map:
                            found_exact_allocatable_combo = True
                            exact_dg = test_dg
                            exact_de = test_de
                            allocation_map = current_allocation_map
                            break # On a trouvé une combinaison qui marche

                if not found_exact_allocatable_combo:
                    continue # Impossible de remplir exactement ET d'allouer
                count_passed_alloc += 1

                # 4. Calculer la Route et vérifier le Temps de Travail
                route_dist_km, route_dur_s = calculate_group_route(group_tuple, commandes, depot)

                if route_dist_km is None or route_dur_s > MAX_TEMPS_TRAVAIL_S:
                    continue # Route impossible ou trop longue
                count_passed_route += 1

                # 5. Stocker l'assignation valide
                assignment_details = {
                    "group": group_tuple,
                    "truck_id": k_id,
                    "distance_km": route_dist_km,
                    "duration_s": route_dur_s,
                    "total_dg": exact_dg,
                    "total_de": exact_de,
                    "allocation": allocation_map,
                    # Ajouter les infos pour l'objectif d'équilibrage
                    "truck_km_j1": truck.get("kilometrage", 0),
                    "truck_km_30j": truck.get("km_30j", 0)
                }
                valid_assignments.append(assignment_details)
                # print(f"      -> Groupe {group_tuple} VALIDE pour camion {k_id} (Dist: {route_dist_km:.1f}km)")

            # print(f"    Taille {r}: Testés={count_tested}, Compatibles={count_passed_basic}, Allouables={count_passed_alloc}, Route OK={count_passed_route}")

    end_time = time.time()
    print(f"Recherche terminée en {end_time - start_time:.2f}s. {len(valid_assignments)} assignations groupe-camion valides trouvées.")
    return valid_assignments

# --- Fonction d'Équilibrage (Identique) ---
def compute_equilibrage_weights(camions):
    if not camions:
        return 0.01, 0.001  # Valeurs par défaut plus fortes

    km_j1_vals = [v.get("kilometrage", 0) for v in camions.values()]
    km_30j_vals = [v.get("km_30j", 0) for v in camions.values()]

    max_j1 = max(km_j1_vals)
    min_j1 = min(km_j1_vals)
    max_30j = max(km_30j_vals)
    min_30j = min(km_30j_vals)

    diff_j1 = max_j1 - min_j1 if max_j1 != min_j1 else 1
    diff_30j = max_30j - min_30j if max_30j != min_30j else 1

    # Augmentation du poids d’équilibrage
    weight_j1 = 0.1 / diff_j1
    weight_30j = 0.01 / diff_30j
    return weight_j1, weight_30j


# --- Fonction Principale d'Optimisation (Refondue) ---
def optimize_group_assignments(commandes, camions, depot, valid_assignments):
    if not valid_assignments:
        print("Erreur: Aucune assignation groupe-camion valide n'a été trouvée. Impossible d'optimiser.")
        return None, None

    print("\nLancement de l'optimisation PuLP pour sélectionner les meilleures assignations de groupes...")

    # Calculer les poids d'équilibrage
    POIDS_EQUILIBRAGE_KM_J1, POIDS_EQUILIBRAGE_KM_30J = compute_equilibrage_weights(camions)
    print(f"Poids équilibrage: J1={POIDS_EQUILIBRAGE_KM_J1:.6f}, 30J={POIDS_EQUILIBRAGE_KM_30J:.6f}")

    prob = pulp.LpProblem("AffectationGroupesCarburant", pulp.LpMinimize)

    assignment_indices = list(range(len(valid_assignments)))
    x = pulp.LpVariable.dicts("AssignGroup", assignment_indices, cat='Binary')

    # --- Fonction Objectif améliorée ---
    PENALITE_NON_AFFECTATION = -1000  # Encourage fortement à affecter si possible

    prob += pulp.lpSum(
        (valid_assignments[i]["distance_km"]
         + POIDS_EQUILIBRAGE_KM_J1 * valid_assignments[i]["truck_km_j1"]
         + POIDS_EQUILIBRAGE_KM_30J * valid_assignments[i]["truck_km_30j"]
         + PENALITE_NON_AFFECTATION  # Récompense toute affectation
         ) * x[i]
        for i in assignment_indices
    ), "CoutTotalPondere"

    all_commande_ids = list(commandes.keys())
    for c_id in all_commande_ids:
        relevant_indices = [i for i in assignment_indices if c_id in valid_assignments[i]["group"]]
        if relevant_indices:
            prob += pulp.lpSum(x[i] for i in relevant_indices) <= 1, f"Commande_{c_id}_au_plus_une_fois"

    all_camion_ids = list(camions.keys())
    for k_id in all_camion_ids:
        relevant_indices = [i for i in assignment_indices if valid_assignments[i]["truck_id"] == k_id]
        if relevant_indices:
            prob += pulp.lpSum(x[i] for i in relevant_indices) <= 1, f"Camion_{k_id}_utilise_au_plus_une_fois"

    # 🔍 Log commandes sans groupe valide
    print("\n--- Vérification des commandes sans groupe valide ---")
    for c_id in all_commande_ids:
        in_groups = any(c_id in va["group"] for va in valid_assignments)
        if not in_groups:
            print(f"[NON-AFFECTÉE] Commande {c_id} ignorée (aucun groupe valide).")

    # Résolution
    print("\nRésolution du problème d'optimisation...")
    try:
        prob.solve()
    except Exception as e:
        print(f"Erreur lors de la résolution : {e}")
        return None, None

    print("\n--- Statut de la Solution ---")
    status = pulp.LpStatus[prob.status]
    print(f"Statut: {status}")

    final_assignments_details = []
    assigned_trucks = set()
    assigned_orders = set()

    if status in ['Optimal', 'Feasible']:
        print(f"Coût total pondéré trouvé: {pulp.value(prob.objective):.4f}")
        print("\n--- Assignations Optimales Sélectionnées ---")
        for i in assignment_indices:
            if x[i].varValue > 0.9:
                selected_assignment = valid_assignments[i]
                final_assignments_details.append(selected_assignment)
                assigned_trucks.add(selected_assignment["truck_id"])
                for cmd_id in selected_assignment["group"]:
                    assigned_orders.add(cmd_id)

                print(f"-> Groupe {selected_assignment['group']} -> Camion {selected_assignment['truck_id']} "
                      f"[{selected_assignment['distance_km']:.1f} km | {selected_assignment['duration_s']/3600:.2f} h]")

        # Résumé
        unassigned_orders = set(all_commande_ids) - assigned_orders
        if unassigned_orders:
            print("\n--- Commandes NON Affectées ---")
            for c_id in sorted(unassigned_orders):
                was_possible = any(c_id in va['group'] for va in valid_assignments)
                reason = "(Aucun groupe valide ne l'incluait)" if not was_possible else "(Non sélectionnée)"
                print(f"  - Commande {c_id} {reason}")

        unused_trucks = set(all_camion_ids) - assigned_trucks
        if unused_trucks:
            print("\n--- Camions Disponibles NON Utilisés ---")
            for k_id in sorted(unused_trucks):
                print(f"  - Camion {k_id}")

        return final_assignments_details, pulp.value(prob.objective)
    else:
        print("Aucune solution exploitable trouvée.")
        return None, None



# --- Exécution ---
if __name__ == "__main__":

    print("Début du processus d'optimisation d'affectation (avec groupes)...")

    # Étape 1: Trouver tous les groupes valides qui remplissent exactement un camion
    # Augmenter max_group_size si nécessaire, mais attention à la performance
    valid_group_assignments = find_valid_group_assignments(commandes, camions, depot, max_group_size=2)

    if not valid_group_assignments:
        print("\nAucun groupe valide trouvé respectant toutes les contraintes (capacité exacte, compartiments, temps, etc.).")
        with open("resultats_affectation.json", "w", encoding="utf-8") as f:
            json.dump({"message": "Aucune commande affectée."}, f, indent=4, ensure_ascii=False)
        sys.exit(0)
    else:
        # ✅ Nouvelle vérification ici
        commandes_affectables = set()
        for va in valid_group_assignments:
            commandes_affectables.update(va["group"])

        if not commandes_affectables:
            print("\n❌ Aucune commande sélectionnée ne respecte les contraintes d’affectation.")
            with open("resultats_affectation.json", "w", encoding="utf-8") as f:
                json.dump({"message": "Aucune commande affectée."}, f, indent=4, ensure_ascii=False)
            sys.exit(0)

        # Étape 2: Optimiser la sélection des groupes
        optimal_selected_assignments, total_cost = optimize_group_assignments(commandes, camions, depot, valid_group_assignments)

        # ✅ Nouvelle vérification après l'optimisation
        if not optimal_selected_assignments or len(optimal_selected_assignments) == 0:
            print("\n❌ Aucune commande sélectionnée n’a pu être affectée après optimisation.")
            with open("resultats_affectation.json", "w", encoding="utf-8") as f:
                json.dump({"message": "Aucune commande affectée."}, f, indent=4, ensure_ascii=False)
            sys.exit(0)



            import json

final_json_result = []

if optimal_selected_assignments is not None and len(optimal_selected_assignments) > 0:
    for assignment in optimal_selected_assignments:
        truck = camions[assignment['truck_id']]
        tournage_info = {
            "Camion": assignment['truck_id'],
            "Capacite": truck.get("capacite", 0),
            "Kilometrage": truck.get("kilometrage", 0),
            "KM_30j": truck.get("km_30j", 0),
            "QuantiteDemandee": round(sum(commandes[cid]['gasoil'] + commandes[cid]['essence'] for cid in assignment['group']), 2),
            "QuantiteProgrammee": round(assignment['total_dg'] + assignment['total_de'], 2),
            "Distance_km": round(assignment['distance_km'], 2),
            "Duree_heures": round(assignment['duration_s'] / 3600, 2),
            "Commandes": [],
            "Quantites_Totales": {
                "DG": round(assignment['total_dg'], 2),
                "DE": round(assignment['total_de'], 2),
                "Total": round(assignment['total_dg'] + assignment['total_de'], 2)
            },
            "Allocation_Compartiments": assignment['allocation']
        }


        group_cmds = assignment['group']
        dg_initial_total = sum(commandes[cid]['gasoil'] for cid in group_cmds)
        de_initial_total = sum(commandes[cid]['essence'] for cid in group_cmds)

        for cmd_id in group_cmds:
            cmd = commandes[cmd_id]
            dg_init = cmd['gasoil']
            de_init = cmd['essence']

            # Flexibilité appliquée
            dg_flex = (dg_init / dg_initial_total) * assignment['total_dg'] if dg_initial_total > 0 else 0
            de_flex = (de_init / de_initial_total) * assignment['total_de'] if de_initial_total > 0 else 0

            produits = []
            codeproduit_list = []



            # ✅ Extraire les codes produits depuis les lignes de commande
            if "lignes" in cmd:
                for ligne in cmd["lignes"]:
                    code = ligne.get("codeProduit")
                    quantite_demandee = ligne.get("quantiteDemandee", 0)
                    if code == cmd["lignes"][0]["codeProduit"]:
                        produits.append({
                            "ProduitCode": code,
                            "Quantite_DG": round(dg_flex),
                            "quantiteDemandee": quantite_demandee,
                            "quantiteProgrammee": round(dg_flex)
                        })
                    else:
                        produits.append({
                            "ProduitCode": code,
                            "Quantite_DE": round(de_flex),
                            "quantiteDemandee": quantite_demandee,
                            "quantiteProgrammee": round(de_flex)
                        })
            else:
                produits.append({
                    "ProduitCode": "UNKNOWN",
                    "Quantite_DG": round(dg_flex),
                    "Quantite_DE": round(de_flex),
                    "quantiteDemandee": round(dg_init + de_init),
                    "quantiteProgrammee": round(dg_flex + de_flex)
                })

            cmd_info = {
                "CommandeID": cmd_id,
                "Reference": cmd.get("referencecommande", ""),  # ⚠️ clé en minuscule normalisée
                "Produits": produits
            }

            tournage_info["Commandes"].append(cmd_info)

        final_json_result.append(tournage_info)

    # # ✅ Extraire les codes produits depuis les lignes de commande
    #         if "lignes" in cmd:
    #             for ligne in cmd["lignes"]:
    #                 codeproduit_list.append(ligne.get("codeProduit"))
    #
    # # Affectation des produits
    #         if len(codeproduit_list) == 2:
    #             produits.append({
    #                 "ProduitCode": codeproduit_list[0],
    #                 # "Quantite_DG": round(dg_flex),
    #                 "quantiteDemandee": round(dg_init),
    #                 "quantiteProgrammee": round(dg_flex)
    #             })
    #             produits.append({
    #                 "ProduitCode": codeproduit_list[1],
    #                 #"Quantite_DE": round(de_flex),
    #                 "quantiteDemandee": round(de_init),
    #                 "quantiteProgrammee": round(de_flex)
    #             })
    #
    #
    #         elif len(codeproduit_list) == 1:
    #             produits.append({
    #                 "ProduitCode": codeproduit_list[0],
    #                 "Quantite_DG": round(dg_flex)
    #             })
    #         else:
    #             produits.append({
    #                 "ProduitCode": "UNKNOWN",
    #                 "Quantite_DG": round(dg_flex),
    #                 "Quantite_DE": round(de_flex)
    #             })
    #
    # # ✅ Correction ici
    #         cmd_info = {
    #             "CommandeID": cmd_id,
    #             "Produits": produits
    #         }
    #
    #         tournage_info["Commandes"].append(cmd_info)
    #
    #
    #     final_json_result.append(tournage_info)

    # --- Affichage propre du JSON final ---
    print("\n" + "="*50)
    print("--- Résumé Final JSON (Produit lié à DG et DE) ---")
    print("="*50)
    print(json.dumps(final_json_result, indent=4, ensure_ascii=False))

    # Optionnel : sauvegarde dans un fichier
    with open("resultats_affectation.json", "w", encoding="utf-8") as f:
        json.dump(final_json_result, f, indent=4, ensure_ascii=False)

else:
    print("Aucune assignation optimale n'a été trouvée.")


# --- Au lieu d'écrire dans un fichier, on imprime directement ---
print(json.dumps(final_json_result, ensure_ascii=False))
sys.exit(0)  # S'assurer que le script retourne 0