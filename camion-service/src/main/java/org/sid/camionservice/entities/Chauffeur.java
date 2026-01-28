package org.sid.camionservice.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "chauffeurs")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Chauffeur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String matricule;

    private String nom;
    private String prenom;
    private String telephone;
    private String email;

    @OneToOne(mappedBy = "chauffeur")
    @JsonIgnore
    private Camion camion;

    @Column(name = "user_id", unique = true)
    private Long userId;
}
