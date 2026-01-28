package org.sid.livraisonservice.controllers;

import lombok.RequiredArgsConstructor;
import org.sid.livraisonservice.dtos.LigneLivraisonReliquatDTO;
import org.sid.livraisonservice.dtos.LivraisonDTO;
import org.sid.livraisonservice.dtos.ReliquatAffectationDTO;
import org.sid.livraisonservice.dtos.TourneeDTO;
import org.sid.livraisonservice.services.LivraisonService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/livraisons")
//@CrossOrigin("*")
@RequiredArgsConstructor
public class LivraisonController {

    private final LivraisonService service;

    @PostMapping
    public ResponseEntity<LivraisonDTO> create(@RequestBody LivraisonDTO dto) {
        LivraisonDTO saved = service.save(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public List<LivraisonDTO> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<LivraisonDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/tournees-avec-livraisons")
    public List<TourneeDTO> getTourneesWithLivraisons() {
        return service.findTourneesWithLivraisons();
    }

    @GetMapping("/tournees/next-reference")
    public String getNextReferenceTournee() {
        return service.generateNextReferenceTournee(); // expose la méthode du service
    }

    @GetMapping("/chauffeur/{matricule}")
    public ResponseEntity<List<LivraisonDTO>> getLivraisonsByChauffeur(@PathVariable String matricule) {
        List<LivraisonDTO> livraisons = service.findByChauffeurMatricule(matricule);
        return ResponseEntity.ok(livraisons);
    }

    @GetMapping("/reliquats")
    public ResponseEntity<List<LigneLivraisonReliquatDTO>> getReliquats() {
        return ResponseEntity.ok(service.getLignesAvecReliquats());
    }

    @PostMapping("/reliquats/affecter")
    public ResponseEntity<Map<String, Object>> affecterReliquat(@RequestBody ReliquatAffectationDTO dto) {
        service.affecterReliquat(dto);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Reliquat affecté avec succès");
        response.put("success", true);
        return ResponseEntity.ok(response);
    }

}