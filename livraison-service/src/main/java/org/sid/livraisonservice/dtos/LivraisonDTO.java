package org.sid.livraisonservice.dtos;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class LivraisonDTO {
    private Long id;
    private Date dateLivraison;
    private String camionMatricule;
    //    private String referenceTournee;
    private String statut;
    private List<LigneLivraisonDTO> lignesLivraison;
    private TourneeDTO tournee;

}