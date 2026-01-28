package org.sid.commandeservice.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;

@Entity
@Data
@Table(name = "commandes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Commande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double gasoil;
    private Double essence;
    private Double quantitetotale;

    private Double tdgmin;
    private Double tdgmax;
    private Double tdemin;
    private Double tdemax;

//    @ElementCollection
//    @CollectionTable(name = "commande_produits", joinColumns = @JoinColumn(name = "commande_id"))
//    @Column(name = "code_produit")
//    private List<Long> codeproduit; // Liste des codes produits (ex: 952243, 952242)

    @Column(name = "date_commande")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCommande;

    @Column(name = "ttc_estime")
    private Double ttcEstime;

    @Column(name = "reference_commande", unique = true)
    private String referenceCommande;


    @Column(name = "station_id")
    private Long stationId; // Foreign Key vers la station

    @Column(name = "user_id")
    private Long userId;

    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LigneCommande> lignes;

    @Column(name = "statut")
    private String statut;
}