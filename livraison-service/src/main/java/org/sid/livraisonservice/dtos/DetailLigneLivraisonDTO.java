package org.sid.livraisonservice.dtos;

import lombok.Data;

@Data
public class DetailLigneLivraisonDTO {
    private Long compartiment;
    private Double capaciteCompartiment;
    private Double quantiteDemandee;
    private Double quantiteProgrammee;
}

