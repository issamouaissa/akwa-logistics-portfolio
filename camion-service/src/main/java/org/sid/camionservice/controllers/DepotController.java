package org.sid.camionservice.controllers;

import lombok.RequiredArgsConstructor;
import org.sid.camionservice.dtos.DepotDTO;
import org.sid.camionservice.entities.Depot;
import org.sid.camionservice.services.DepotService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/depots")
@RequiredArgsConstructor
public class DepotController {

    private final DepotService depotService;

    @PostMapping
    public ResponseEntity<Depot> createDepot(@RequestBody DepotDTO dto) {
        Depot saved = depotService.saveDepot(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public List<Depot> getAll() {
        return depotService.getAllDepots();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Depot> getDepotById(@PathVariable Long id) {
        return depotService.getDepotById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Depot> updateDepot(@PathVariable Long id, @RequestBody DepotDTO dto) {
        Depot updated = depotService.updateDepot(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepot(@PathVariable Long id) {
        depotService.deleteDepot(id);
        return ResponseEntity.noContent().build();
    }
}
