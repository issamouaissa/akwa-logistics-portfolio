package org.sid.camionservice.controllers;

import lombok.RequiredArgsConstructor;
import org.sid.camionservice.dtos.StockDTO;
import org.sid.camionservice.entities.Stock;
import org.sid.camionservice.services.StockService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @PostMapping
    public ResponseEntity<Stock> createOrUpdateStock(@RequestBody StockDTO dto) {
        Stock stock = stockService.addOrUpdateStock(dto);
        return ResponseEntity.ok(stock);
    }

    @GetMapping("/depot/{depotId}")
    public ResponseEntity<Stock> getStockByDepot(@PathVariable Long depotId) {
        return stockService.getStockByDepot(depotId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{stockId}")
    public ResponseEntity<Void> deleteStock(@PathVariable Long stockId) {
        stockService.deleteStock(stockId);
        return ResponseEntity.noContent().build();
    }
}
