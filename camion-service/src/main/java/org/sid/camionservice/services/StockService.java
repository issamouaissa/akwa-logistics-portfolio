package org.sid.camionservice.services;

import lombok.RequiredArgsConstructor;
import org.sid.camionservice.dtos.StockDTO;
import org.sid.camionservice.entities.Depot;
import org.sid.camionservice.entities.Stock;
import org.sid.camionservice.repositories.DepotRepository;
import org.sid.camionservice.repositories.StockRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;
    private final DepotRepository depotRepository;

    public Stock addOrUpdateStock(StockDTO dto) {
        Depot depot = depotRepository.findById(dto.getDepotId())
                .orElseThrow(() -> new RuntimeException("Depot non trouvé"));

        Stock stock = stockRepository.findByDepotId(dto.getDepotId())
                .orElse(Stock.builder().depot(depot).build());

        stock.setEssence(dto.getEssence());
        stock.setGasoil(dto.getGasoil());
        stock.setQuantite(dto.getEssence() + dto.getGasoil()); // Calcul automatique

        return stockRepository.save(stock);
    }



    public Optional<Stock> getStockByDepot(Long depotId) {
        return stockRepository.findByDepotId(depotId);
    }

    public void deleteStock(Long stockId) {
        stockRepository.deleteById(stockId);
    }

}
