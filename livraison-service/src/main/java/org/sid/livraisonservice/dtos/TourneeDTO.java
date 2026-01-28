package org.sid.livraisonservice.dtos;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class TourneeDTO {
    private Long id;
    private String referenceTournee;
    private String depot;
    private Date dateTournee;
    private String statut;
    private String camion;
    private String chauffeurMatricule;
    private List<LivraisonDTO> livraisons;
}