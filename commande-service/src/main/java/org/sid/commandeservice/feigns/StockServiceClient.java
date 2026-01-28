/*
package org.sid.commandeservice.feigns;

import org.sid.commandeservice.enums.TypeCarburant;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "stock-service")
public interface StockServiceClient {

    // Modifiez le retour pour qu'il soit un boolean, indiquant si la quantité demandée est disponible
    @GetMapping("/api/stocks/check-stock/{fuelType}/{quantity}")
    boolean verifierStock(@PathVariable("fuelType") TypeCarburant fuelType, @PathVariable("quantity") double quantity);
}
*/