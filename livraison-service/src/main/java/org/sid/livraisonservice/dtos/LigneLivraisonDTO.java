package org.sid.livraisonservice.dtos;

import lombok.Data;

import java.util.List;

@Data
public class LigneLivraisonDTO {
    private Long id;
    private Double quantiteLivree;
    private Double quantiteDemandee;
    private Double quantiteProgrammee;
    private Double prixUnitaire;
    private Long codeProduit;
//    private Long compartiment;
    private Long ligneCommandeId;
    private List<DetailLigneLivraisonDTO> detailsCompartiments;
}