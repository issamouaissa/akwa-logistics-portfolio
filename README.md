# Plateforme d’optimisation logistique – Akwa Group (Version Portfolio)

## Contexte
Ce projet correspond à mon projet de fin d’études, réalisé dans un contexte industriel réel pour Akwa Group.  
L’objectif était de concevoir et développer une plateforme logicielle permettant d’optimiser les opérations de livraison de carburant, en tenant compte des contraintes terrain, des capacités des camions, des quantités réellement livrées et des besoins de réaffectation en cas d’écart par rapport au planning initial.

Le système est destiné à être utilisé par différents profils (administrateurs, logistiques, chauffeurs, Station service) et repose sur une architecture microservices sécurisée et scalable.

---

## Objectifs du projet
- Optimisation des tournées de livraison de carburant
- Affectation intelligente des commandes aux camions
- Suivi détaillé des livraisons sur le terrain
- Gestion des écarts entre quantités programmées et quantités réellement livrées
- Réaffectation automatique ou manuelle des livraisons en cas d’anomalie
- Centralisation et sécurisation des données logistiques
- Scalabilité et maintenabilité via une architecture microservices

---

## Architecture Générale
La solution repose sur une architecture microservices avec les composants suivants :
- **API Gateway** comme point d’entrée unique du système
- **Service d’authentification** pour la gestion sécurisée des utilisateurs
- **Services métier** dédiés aux camions, commandes, stations et livraisons
- **Moteur d’optimisation** indépendant développé en Python
- **Application frontend** pour la visualisation et l’administration des opérations

L’architecture a été pensée pour faciliter l’évolution du système, l’isolation des responsabilités et la gestion de charges variables.

---

## Description des microservices

- **api-gateway**  
  Point d’entrée unique du système. Assure le routage des requêtes vers les microservices, la gestion des accès et la sécurité globale.

- **authentification-service**  
  Gestion des utilisateurs, des rôles et de l’authentification via des tokens JWT.

- **camion-service**  
  Gestion des camions (capacités, disponibilités, affectations aux tournées).

- **commande-service**  
  Gestion des commandes de livraison, des quantités demandées, programmées, livrées et des contraintes associées.

- **livraison-service**  
  Enregistrement et suivi des données de chaque livraison réalisée sur le terrain, associées à un chauffeur, un camion et une station.  
  Permet de comparer les quantités livrées aux quantités prévues et de déclencher des mécanismes de réaffectation en cas d’écart.

- **station-service**  
  Gestion des informations relatives aux stations (localisation, capacités, demandes).

- **optimisation-service**  
  Moteur d’optimisation développé en Python, basé sur la modélisation mathématique et la résolution par l’algorithme du simplexe.  
  Il permet de calculer des tournées optimales en minimisant la distance totale tout en respectant les contraintes opérationnelles.

- **frontend**  
  Interface web permettant :
  - la visualisation des tournées et des trajets,
  - le suivi des livraisons,
  - l’accès aux détails des lignes de livraison,
  - la gestion et la réaffectation des opérations en cas d’anomalie.

---

## Stack technique
- **Backend** : Java, Spring Boot, architecture microservices
- **Frontend** : Angular
- **Base de données** : PostgreSQL
- **Optimisation** : Python, PuLP, algorithme du simplexe
- **Sécurité** : JWT, gestion des rôles
- **Architecture** : API REST, communication inter-services

---

## Fonctionnalités principales
- Gestion des utilisateurs et des rôles
- Gestion des camions, stations et commandes
- Optimisation automatique des tournées
- Suivi détaillé des livraisons terrain
- Comparaison quantités prévues / quantités livrées
- Réaffectation des livraisons en cas d’écart
- Visualisation des lignes de livraison et des trajets
- Tableaux de bord de suivi des opérations

---

## Confidentialité – Version Portfolio
Ce dépôt correspond à une **version portfolio** du projet :
- Les fichiers de configuration (`.yml`, `.properties`) ont été supprimés ou remplacés par des fichiers d’exemple
- Les données réelles ont été anonymisées ou remplacées par des données fictives
- Aucun secret, clé API ou accès interne n’est exposé

---

## Rôle personnel
- Analyse des besoins fonctionnels et techniques
- Conception de l’architecture microservices
- Développement des services backend
- Développement du moteur d’optimisation en Python
- Implémentation de la sécurité (JWT, rôles)
- Développement du frontend
- Tests, validation et intégration des services

---

## Remarque
Ce projet met l’accent sur la conception de systèmes distribués robustes, adaptés à des usages terrain et à des contraintes opérationnelles réelles.
