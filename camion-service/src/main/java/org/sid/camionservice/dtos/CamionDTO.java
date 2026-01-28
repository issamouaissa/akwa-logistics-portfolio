package org.sid.camionservice.dtos;

import lombok.Data;
import java.util.List;

@Data
public class CamionDTO {
    private String matricule;   // ex: "11111-B-7"
    private Double capacite;    // 33.0, 35.0 etc
    private Boolean pompe;      // true ou false
    private Boolean solo;       // true ou false
    private Boolean normal;     // true ou false
    private Double kilometrage; // km aujourd'hui
    private Double km30j;       // km sur les 30 derniers jours
    private String depotCode;  // Pour lier un camion à un dépôt
    private String chauffeurMatricule;



    // Ajouter compartiments directement ici (en litres)
    private List<Double> compartiments;
}

