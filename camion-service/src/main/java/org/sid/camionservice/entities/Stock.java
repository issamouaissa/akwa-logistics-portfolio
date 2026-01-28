package org.sid.camionservice.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "stocks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double essence;
    private Double gasoil;
    private Double quantite;

    @ManyToOne
    @JoinColumn(name = "depot_id")
    private Depot depot;
}
