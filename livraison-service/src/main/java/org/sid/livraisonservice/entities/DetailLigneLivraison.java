package org.sid.livraisonservice.entities;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "detail_ligne_livraison")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetailLigneLivraison {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long compartiment;
    private Double capaciteCompartiment;
    private Double quantiteDemandee;
    private Double quantiteProgrammee;

    @ManyToOne
    @JoinColumn(name = "ligne_livraison_id")
    private LigneLivraison ligneLivraison;
}