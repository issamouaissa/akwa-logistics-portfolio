package org.sid.stationservice.controllers;

import org.sid.stationservice.entities.Station;
import org.sid.stationservice.services.StationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stations")
public class StationController {

    @Autowired
    private StationService service;

    @PostMapping
    public Station create(@RequestBody Station station) {
        return service.save(station);
    }

    @GetMapping
    public List<Station> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Station> getById(@PathVariable Long id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Station> update(@PathVariable Long id, @RequestBody Station updatedStation) {
        return service.update(id, updatedStation)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (service.delete(id)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/user/{userId}")
    public List<Station> getByUserId(@PathVariable Long userId) {
        return service.getByUserId(userId);
    }

}
