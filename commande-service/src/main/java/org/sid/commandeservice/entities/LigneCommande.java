package org.sid.commandeservice.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ligne_commandes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LigneCommande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long codeProduit;

    @Column(name = "quantite_demandee")
    private Double quantiteDemandee;

    @Column(name = "quantite_programmee")
    private Double quantiteProgrammee;

    @Column(name = "quantite_livree")
    private Double quantiteLivree;

    @Column(name = "prix_unitaire")
    private Double prixUnitaire;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commande_id")
    @JsonIgnore
    private Commande commande;
}