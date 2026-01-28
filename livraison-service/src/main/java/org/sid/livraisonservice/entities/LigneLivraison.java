package org.sid.livraisonservice.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ligne_livraisons")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LigneLivraison {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double quantiteLivree;
    private Double quantiteDemandee;
    private Double quantiteProgrammee;
    private Double prixUnitaire;

    private Long codeProduit;
    // private Long compartiment;

    @ManyToOne
    @JoinColumn(name = "livraison_id")
    private Livraison livraison;

    private Long ligneCommandeId; // Pour liaison simple

    @OneToMany(mappedBy = "ligneLivraison", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetailLigneLivraison> detailsCompartiments = new ArrayList<>();

}