package org.sid.authentification.dtos;

import lombok.Data;

@Data
public class StationDTO {
    private Long id;
    private String libelle;
    private String code;
    private String adresse;
    private String ville;
    private Double posilat;
    private Double posilong;

    private Boolean pompe;
    private Boolean sousTraitant;
    private Boolean normal;
    private Boolean flottePropre;
    private Boolean flexibilite;
    private Boolean solo;

    private String contact;
    private String telephone;
    private Boolean active;

    private Long userId;
}