package org.sid.camionservice.services;

import org.sid.camionservice.dtos.CamionDTO;
import org.sid.camionservice.entities.Camion;
import org.sid.camionservice.entities.Chauffeur;
import org.sid.camionservice.entities.Compartiment;
import org.sid.camionservice.entities.Depot;
import org.sid.camionservice.repositories.CamionRepository;
import org.sid.camionservice.repositories.ChauffeurRepository;
import org.sid.camionservice.repositories.CompartimentRepository;
import org.sid.camionservice.repositories.DepotRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CamionService {

    private final CamionRepository camionRepository;
    private final DepotRepository depotRepository;

    private final ChauffeurRepository chauffeurRepository;
    private final CompartimentRepository compartimentRepository;

    public CamionService(CamionRepository camionRepository,
                         CompartimentRepository compartimentRepository,
                         DepotRepository depotRepository,
                         ChauffeurRepository chauffeurRepository) {
        this.camionRepository = camionRepository;
        this.compartimentRepository = compartimentRepository;
        this.depotRepository = depotRepository;
        this.chauffeurRepository = chauffeurRepository;
    }


    public Camion saveCamion(CamionDTO camionDTO) {
        Camion camion = new Camion();
        camion.setMatricule(camionDTO.getMatricule());
        camion.setCapacite(camionDTO.getCapacite());
        camion.setPompe(camionDTO.getPompe());
        camion.setSolo(camionDTO.getSolo());
        camion.setNormal(camionDTO.getNormal());
        camion.setKilometrage(camionDTO.getKilometrage());
        camion.setKm30j(camionDTO.getKm30j());

        // 🔥 Associer le dépôt
        Depot depot = depotRepository.findByCode(camionDTO.getDepotCode())
                .orElseThrow(() -> new RuntimeException("Dépôt non trouvé avec le code : " + camionDTO.getDepotCode()));
        camion.setDepot(depot);

        List<Compartiment> compartiments = camionDTO.getCompartiments().stream()
                .map(capacite -> {
                    Compartiment c = new Compartiment();
                    c.setCapacite(capacite);
                    c.setCamion(camion);
                    return c;
                }).collect(Collectors.toList());

        camion.setCompartiments(compartiments);

        if (camionDTO.getChauffeurMatricule() != null) {
            Chauffeur chauffeur = chauffeurRepository.findByMatricule(camionDTO.getChauffeurMatricule())
                    .orElseThrow(() -> new RuntimeException("Chauffeur non trouvé"));
            camion.setChauffeur(chauffeur);
        }


        return camionRepository.save(camion);
    }


    public List<Map<String, Object>> getAllCamionsFormatted() {
        List<Camion> camions = camionRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Camion camion : camions) {
            Map<String, Object> camionDetails = new HashMap<>();
            camionDetails.put("matricule", camion.getMatricule());
            camionDetails.put("Compartiment", camion.getCompartiments().stream()
                    .map(Compartiment::getCapacite)
                    .collect(Collectors.toList()));
            camionDetails.put("Pompe", camion.getPompe() != null && camion.getPompe() ? 1 : 0);
            camionDetails.put("solo", camion.getSolo() != null && camion.getSolo() ? 1 : 0);
            camionDetails.put("Normal", camion.getNormal() != null && camion.getNormal() ? 1 : 0);
            camionDetails.put("Capacite", camion.getCapacite());
            camionDetails.put("kilometrage", camion.getKilometrage());
            camionDetails.put("km_30j", camion.getKm30j());
            Chauffeur chauffeur = camion.getChauffeur();
            if (chauffeur != null) {
                Map<String, Object> chauffeurMap = new HashMap<>();
                chauffeurMap.put("matricule", chauffeur.getMatricule());
                chauffeurMap.put("nom", chauffeur.getNom());
                chauffeurMap.put("prenom", chauffeur.getPrenom());
                chauffeurMap.put("telephone", chauffeur.getTelephone());
                chauffeurMap.put("email", chauffeur.getEmail());
                camionDetails.put("chauffeur", chauffeurMap);
            } else {
                camionDetails.put("chauffeur", null);
            }

            if (camion.getDepot() != null) {
                Map<String, Object> depotDetails = new HashMap<>();
                depotDetails.put("code", camion.getDepot().getCode());
                depotDetails.put("posilat", camion.getDepot().getPosilat());
                depotDetails.put("posilong", camion.getDepot().getPosilong());
                camionDetails.put("depot", depotDetails);
            } else {
                camionDetails.put("depot", null);
            }



            result.add(camionDetails);
        }

        return result;
    }

    public Camion getCamionById(Long id) {
        return camionRepository.findById(id).orElse(null);
    }

    public void deleteCamion(Long id) {
        camionRepository.deleteById(id);
    }
}
