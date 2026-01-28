package org.sid.commandeservice.controllers;

import org.sid.commandeservice.entities.Commande;

import org.sid.commandeservice.entities.LigneCommande;
import org.sid.commandeservice.repositories.CommandeRepository;
import org.sid.commandeservice.repositories.LigneCommandeRepository;
import org.sid.commandeservice.services.CommandeService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping("/api/commandes")
public class CommandeController {

    @Autowired
    private CommandeService service;

    @Autowired
    private LigneCommandeRepository ligneCommandeRepository;

    @Autowired
    private CommandeRepository commandeRepository;



    @GetMapping
    public List<Map<String, Object>> getAll() {
        return service.getAllCommandesEnriched();
    }

    @PostMapping
    public Commande create(@RequestBody Commande commande) {
        return service.save(commande);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Commande> getById(@PathVariable Long id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Commande> updateCommande(@PathVariable Long id, @RequestBody Commande updated) {
        Optional<Commande> optional = service.getById(id);
        if (optional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            Commande updatedCommande = service.updateQuantites(id, updated.getGasoil(), updated.getEssence(), updated.getStationId());
            return ResponseEntity.ok(updatedCommande);
        } catch (Exception e) {
            return ResponseEntity.status(500).build(); // ou log l'erreur
        }
    }




    @GetMapping("/{id}/station")
    public ResponseEntity<Object> getStationByCommande(@PathVariable Long id) {
        Optional<Commande> cmd = service.getById(id);
        if (cmd.isPresent()) {
            Long stationId = cmd.get().getStationId();
            try {
                RestTemplate restTemplate = new RestTemplate();
                String stationUrl = "http://localhost:8090/api/stations/" + stationId;
                Object station = restTemplate.getForObject(stationUrl, Object.class);
                return ResponseEntity.ok(station);
            } catch (Exception e) {
                return ResponseEntity.status(404).body("Station introuvable");
            }
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/enriched")
    public List<Map<String, Object>> getAllCommandesEnriched() {
        return service.getAllCommandesEnriched();
    }

    @GetMapping("/enriched/user/{userId}")
    public List<Map<String, Object>> getAllCommandesEnrichedByUser(@PathVariable Long userId) {
        return service.getCommandesEnrichedByUser(userId);
    }

    @GetMapping("/user/{userId}")
    public List<Commande> getByUserId(@PathVariable Long userId) {
        return service.getByUserId(userId);
    }

    @GetMapping("/{id}/facture")
    public ResponseEntity<byte[]> downloadFacture(
            @PathVariable Long id,
            @RequestParam(defaultValue = "pdf") String format) {
        return service.generateFacture(id, format);
    }


    @PutMapping("/{id}/update-ligne")
    public ResponseEntity<?> updateLigneCommande(@PathVariable Long id, @RequestBody Map<String, Object> ligneDto) {
        Optional<Commande> opt = service.getById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        Commande commande = opt.get();
        Long codeProduit = Long.valueOf(ligneDto.get("codeProduit").toString());

        Optional<LigneCommande> ligneOpt = commande.getLignes().stream()
                .filter(l -> l.getCodeProduit().equals(codeProduit))
                .findFirst();

        if (ligneOpt.isEmpty()) return ResponseEntity.badRequest().body("Ligne non trouvée");

        LigneCommande ligne = ligneOpt.get();
        Double newQuantite = ligneDto.get("quantiteProgrammee") != null
                ? ((Number) ligneDto.get("quantiteProgrammee")).doubleValue()
                : null;

        ligne.setQuantiteProgrammee(newQuantite);
        ligneCommandeRepository.save(ligne);

        // ✅ Mettre à jour le statut de la commande
        String statut = "EN_COURS";
        boolean anyProgrammee = commande.getLignes().stream().anyMatch(l -> l.getQuantiteProgrammee() != null);
        boolean anyLivree = commande.getLignes().stream().anyMatch(l -> l.getQuantiteLivree() != null);

        if (anyLivree) {
            statut = "LIVREE";
        } else if (anyProgrammee) {
            statut = "EN_LIVRAISON";
        }

        commande.setStatut(statut);
        commandeRepository.save(commande); // 🔁 Sauvegarder le statut mis à jour

        return ResponseEntity.ok("Ligne mise à jour avec statut = " + statut);
    }



    @GetMapping("/reference/{ref}")
    public ResponseEntity<Commande> getByReference(@PathVariable String ref) {
        return service.getByReference(ref)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @PutMapping("/{commandeId}/update-quantite-livree")
    public ResponseEntity<?> updateQuantiteLivree(
            @PathVariable Long commandeId,
            @RequestBody Map<String, Object> body) {

        Long codeProduit = Long.valueOf(body.get("codeProduit").toString());
        Double quantiteLivree = ((Number) body.get("quantiteLivree")).doubleValue();

        Optional<Commande> opt = service.getById(commandeId);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        Commande commande = opt.get();

        Optional<LigneCommande> ligneOpt = commande.getLignes().stream()
                .filter(l -> l.getCodeProduit().equals(codeProduit))
                .findFirst();

        if (ligneOpt.isEmpty()) return ResponseEntity.badRequest().body("Ligne non trouvée");

        LigneCommande ligne = ligneOpt.get();
        ligne.setQuantiteLivree(quantiteLivree);
        ligneCommandeRepository.save(ligne);

        // Mettre à jour le statut de la commande
        String statut = "EN_COURS";
        boolean anyProgrammee = commande.getLignes().stream().anyMatch(l -> l.getQuantiteProgrammee() != null);
        boolean anyLivree = commande.getLignes().stream().anyMatch(l -> l.getQuantiteLivree() != null);
        if (anyLivree) {
            statut = "LIVREE";
        } else if (anyProgrammee) {
            statut = "EN_LIVRAISON";
        }
        commande.setStatut(statut);
        commandeRepository.save(commande);

        return ResponseEntity.ok("Quantité livrée mise à jour");
    }

    @GetMapping("/lignes-commandes/{id}")
    public ResponseEntity<LigneCommande> getLigneCommandeById(@PathVariable Long id) {
        return ligneCommandeRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/lignes-commandes/by-commande-produit")
    public ResponseEntity<LigneCommande> getLigneByCommandeAndProduit(
            @RequestParam Long commandeId,
            @RequestParam Long codeProduit) {
        return ligneCommandeRepository.findByCommandeIdAndCodeProduit(commandeId, codeProduit)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}