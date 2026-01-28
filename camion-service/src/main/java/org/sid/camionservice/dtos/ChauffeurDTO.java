package org.sid.camionservice.dtos;

import lombok.Data;

@Data
public class ChauffeurDTO {
    private String matricule;
    private String nom;
    private String prenom;
    private String telephone;
    private String email;
    private Long userId;
}
