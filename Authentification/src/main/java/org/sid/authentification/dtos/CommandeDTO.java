package org.sid.authentification.dtos;


import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class CommandeDTO {
    private Long id;
    private Double gasoil;
    private Double essence;
    private Double quantitetotale;

    private Double tdgmin;
    private Double tdgmax;
    private Double tdemin;
    private Double tdemax;

    private List<Long> codeproduit;
    private Long stationId;
    private Long userId;
    private Date dateCommande;
}