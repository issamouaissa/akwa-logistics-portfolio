package org.sid.livraisonservice.dtos;

import lombok.Data;

@Data
public class ReliquatAffectationDTO {
    private Long ligneLivraisonId;
    private Long nouvelleStationId;
    private Double quantiteAAffecter;
}

