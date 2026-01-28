package org.sid.livraisonservice.dtos;

import lombok.Data;

@Data
public class LigneLivraisonReliquatDTO {
    private Long ligneLivraisonId;
    private Long codeProduit;
    private Double quantiteRestante;
    private Long ligneCommandeId;
    private String referenceCommande;
    private Long stationInitialeId;
}
