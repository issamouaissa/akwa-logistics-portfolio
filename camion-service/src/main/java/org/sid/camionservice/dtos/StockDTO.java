package org.sid.camionservice.dtos;

import lombok.Data;

@Data
public class StockDTO {
    private Double essence;
    private Double gasoil;
    private Long depotId;
}
