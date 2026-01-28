package org.sid.camionservice.entities;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "compartiments")
public class Compartiment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double capacite;

    @ManyToOne
    @JoinColumn(name = "camion_matricule", referencedColumnName = "matricule")
    private Camion camion;
}
