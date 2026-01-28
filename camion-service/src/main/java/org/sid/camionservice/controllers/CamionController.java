package org.sid.camionservice.controllers;

import org.sid.camionservice.dtos.CamionDTO;
import org.sid.camionservice.entities.Camion;
import org.sid.camionservice.services.CamionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/camions")
//@CrossOrigin(origins = "http://localhost:4200")
public class CamionController {

    private final CamionService camionService;

    public CamionController(CamionService camionService) {
        this.camionService = camionService;
    }

    @PostMapping
    public Camion createCamion(@RequestBody CamionDTO camionDTO) {
        return camionService.saveCamion(camionDTO);
    }

    @GetMapping
    public List<Map<String, Object>> getAllCamionsFormatted() {
        return camionService.getAllCamionsFormatted();  // ⚡ correction ici
    }

    @GetMapping("/{id}")
    public Camion getCamionById(@PathVariable Long id) {
        return camionService.getCamionById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteCamion(@PathVariable Long id) {
        camionService.deleteCamion(id);
    }

    @GetMapping("/matricule/{matricule}")
    public ResponseEntity<Map<String, Object>> getCamionByMatricule(@PathVariable String matricule) {
        List<Map<String, Object>> all = camionService.getAllCamionsFormatted();
        for (Map<String, Object> camion : all) {
            if (camion.get("matricule").equals(matricule)) {
                return ResponseEntity.ok(camion);
            }
        }
        return ResponseEntity.notFound().build();
    }

}
