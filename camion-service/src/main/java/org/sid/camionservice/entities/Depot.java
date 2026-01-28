package org.sid.camionservice.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "depots")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Depot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String code;

    private Double posilat;
    private Double posilong;

}

