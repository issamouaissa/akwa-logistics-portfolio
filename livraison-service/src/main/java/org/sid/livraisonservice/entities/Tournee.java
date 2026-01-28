package org.sid.livraisonservice.entities;


import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "tournees")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tournee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String referenceTournee;

    private String depot;
    private Date dateTournee;
    private String statut;

    private String camion;
    private String chauffeurMatricule;

    @OneToMany(mappedBy = "tournee", cascade = CascadeType.ALL)
    private List<Livraison> livraisons = new ArrayList<>();

    //commandeId || commandeId, livraisonId, tourneeId dans un autre table 

}