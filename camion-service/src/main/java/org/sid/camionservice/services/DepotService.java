package org.sid.camionservice.services;

import lombok.RequiredArgsConstructor;
import org.sid.camionservice.dtos.DepotDTO;
import org.sid.camionservice.entities.Depot;
import org.sid.camionservice.repositories.DepotRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DepotService {

    private final DepotRepository depotRepository;

    public Depot saveDepot(DepotDTO dto) {
        Depot depot = Depot.builder()
                .code(dto.getCode())
                .posilat(dto.getPosilat())
                .posilong(dto.getPosilong())
                .build();
        return depotRepository.save(depot);
    }

    public List<Depot> getAllDepots() {
        return depotRepository.findAll();
    }

    public Optional<Depot> getDepotById(Long id) {
        return depotRepository.findById(id);
    }

    public Depot updateDepot(Long id, DepotDTO dto) {
        Depot existing = depotRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Depot non trouvé avec id: " + id));
        existing.setCode(dto.getCode());
        existing.setPosilat(dto.getPosilat());
        existing.setPosilong(dto.getPosilong());
        return depotRepository.save(existing);
    }

    public void deleteDepot(Long id) {
        depotRepository.deleteById(id);
    }
}
