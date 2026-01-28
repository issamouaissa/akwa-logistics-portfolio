package org.sid.stationservice.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.sid.stationservice.entities.Station;
import org.sid.stationservice.repositories.StationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StationService {

    @Autowired
    private StationRepository repository;

    private final String API_KEY = "32be6175-d30c-4298-bfc2-0c2981cc73ce";

    public Station save(Station station) {
        // Générer automatiquement le code unique si non fourni
        if (station.getCode() == null || station.getCode().trim().isEmpty()) {
            station.setCode(generateUniqueCode());
        }

        // Géocodage de l'adresse
        double[] latlng = getCoordinatesFromAddress(station.getAdresse());
        station.setPosilat(latlng[0]);
        station.setPosilong(latlng[1]);

        return repository.save(station);
    }

    private String generateUniqueCode() {
        long count = repository.count() + 1;
        String code;
        do {
            code = String.format("ST%03d", count);
            count++;
        } while (repository.existsByCode(code));
        return code;
    }

    public Optional<Station> getById(Long id) {
        return repository.findById(id);
    }

    public List<Station> getAll() {
        return repository.findAll();
    }

    private double[] getCoordinatesFromAddress(String adresse) {
        try {
            String encodedAddress = URLEncoder.encode(adresse, StandardCharsets.UTF_8);
            String url = "https://graphhopper.com/api/1/geocode?q=" + encodedAddress + "&locale=fr&limit=1&key=" + API_KEY;

            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String json = reader.lines().collect(Collectors.joining());
            reader.close();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);
            JsonNode location = root.get("hits").get(0);

            double lat = location.get("point").get("lat").asDouble();
            double lng = location.get("point").get("lng").asDouble();

            return new double[]{lat, lng};

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la géocodification de l'adresse", e);
        }
    }

    public Optional<Station> update(Long id, Station updatedStation) {
        return repository.findById(id).map(existing -> {
            existing.setLibelle(updatedStation.getLibelle());
            existing.setCode(updatedStation.getCode());
            existing.setAdresse(updatedStation.getAdresse());
            existing.setVille(updatedStation.getVille());
            existing.setPompe(updatedStation.getPompe());
            existing.setSousTraitant(updatedStation.getSousTraitant());
            existing.setNormal(updatedStation.getNormal());
            existing.setFlottePropre(updatedStation.getFlottePropre());
            existing.setFlexibilite(updatedStation.getFlexibilite());
            existing.setSolo(updatedStation.getSolo());
            existing.setContact(updatedStation.getContact());
            existing.setTelephone(updatedStation.getTelephone());
            existing.setActive(updatedStation.getActive());

            // 🧭 Recalcul des coordonnées si l’adresse change
            double[] latlng = getCoordinatesFromAddress(updatedStation.getAdresse());
            existing.setPosilat(latlng[0]);
            existing.setPosilong(latlng[1]);

            return repository.save(existing);
        });
    }

    public boolean delete(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }

    public List<Station> getByUserId(Long userId) {
        return repository.findByUserId(userId);
    }


}


