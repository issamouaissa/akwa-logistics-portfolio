package org.sid.camionservice.entities;



import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;


@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "camions")
public class Camion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String matricule;

    private Double capacite;
    private Boolean pompe;
    private Boolean solo;
    private Boolean normal;
    private Double kilometrage;
    private Double km30j;

    @ManyToOne
    @JoinColumn(name = "depot_id")
    private Depot depot;

    @OneToOne
    @JoinColumn(name = "chauffeur_id")
    private Chauffeur chauffeur;



    @OneToMany(mappedBy = "camion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Compartiment> compartiments = new ArrayList<>();
}