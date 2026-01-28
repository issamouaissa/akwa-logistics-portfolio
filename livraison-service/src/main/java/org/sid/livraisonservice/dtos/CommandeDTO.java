package org.sid.livraisonservice.dtos;


import lombok.Data;

@Data
public class CommandeDTO {
    private Long id;
    private String referenceCommande;
    private Long stationId;
}
