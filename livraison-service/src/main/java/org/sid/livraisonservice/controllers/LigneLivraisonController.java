package org.sid.livraisonservice.controllers;

import lombok.RequiredArgsConstructor;
import org.sid.livraisonservice.services.LigneLivraisonService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/livraisons/lignes")
@RequiredArgsConstructor
public class LigneLivraisonController {

    private final LigneLivraisonService ligneLivraisonService;

    @PutMapping("/{id}/quantite-livree")
    public ResponseEntity<Void> updateQuantiteLivree(
            @PathVariable Long id,
            @RequestBody Double quantiteLivree) {
        ligneLivraisonService.updateQuantiteLivree(id, quantiteLivree);
        return ResponseEntity.ok().build();
    }
}
