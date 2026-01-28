package org.sid.livraisonservice.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "livraisons")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Livraison {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Date dateLivraison;
    private String camionMatricule;
    //    private String referenceTournee;   //objet --> reftournee, depot, datetournee, statut, camion, chauffeur,
    // nouveau champ sur livraison . tourneeID
    private String statut;

    @ManyToOne(cascade = CascadeType.ALL) // ou @OneToOne si une livraison a exactement une seule tournée unique
    @JoinColumn(name = "tournee_id")
    private Tournee tournee;

    @OneToMany(mappedBy = "livraison", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LigneLivraison> lignesLivraison = new ArrayList<>();
}