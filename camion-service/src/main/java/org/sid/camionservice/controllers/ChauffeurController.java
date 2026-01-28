package org.sid.camionservice.controllers;

import lombok.RequiredArgsConstructor;
import org.sid.camionservice.dtos.ChauffeurDTO;
import org.sid.camionservice.entities.Chauffeur;
import org.sid.camionservice.services.ChauffeurService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chauffeurs")
@RequiredArgsConstructor
public class ChauffeurController {

    private final ChauffeurService chauffeurService;

    @PostMapping
    public ResponseEntity<Chauffeur> create(@RequestBody ChauffeurDTO dto) {
        return ResponseEntity.ok(chauffeurService.save(dto));
    }

    @GetMapping
    public List<Chauffeur> getAll() {
        return chauffeurService.getAll();
    }

    @GetMapping("/{matricule}")
    public ResponseEntity<Chauffeur> getByMatricule(@PathVariable String matricule) {
        return ResponseEntity.ok(chauffeurService.getByMatricule(matricule));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Chauffeur> getByUserId(@PathVariable Long userId) {
        Chauffeur chauffeur = chauffeurService.getByUserId(userId);
        return ResponseEntity.ok(chauffeur);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Chauffeur> update(@PathVariable Long id, @RequestBody ChauffeurDTO dto) {
        return ResponseEntity.ok(chauffeurService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        chauffeurService.delete(id);
        return ResponseEntity.noContent().build();
    }


}
