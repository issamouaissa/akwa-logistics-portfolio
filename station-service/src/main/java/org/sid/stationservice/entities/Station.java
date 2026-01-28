package org.sid.stationservice.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "stations")
public class Station {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String libelle;

    @Column(unique = true)
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

    @Column(name = "user_id")
    private Long userId;

}

