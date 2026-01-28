package org.sid.optimisationservice.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.sid.optimisationservice.services.OptimisationService;
import org.sid.optimisationservice.services.ResultTransferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/optimisation")
public class OptimisationController {

    private final OptimisationService optimisationService;

    @Autowired
    private ResultTransferService resultTransferService;

    // ✅ Constructeur pour l’injection
    public OptimisationController(OptimisationService optimisationService) {
        this.optimisationService = optimisationService;
    }

//    @PostMapping("/run")
//    public String runOptimisationWithSelection(@RequestBody List<Long> selectedCommandeIds) {
//        try {
//            // 1. Récupère uniquement les commandes sélectionnées
//            Map<Long, Object> allCommandes = optimisationService.getCommandes();
//            Map<Long, Object> selectedCommandes = allCommandes.entrySet().stream()
//                    .filter(entry -> selectedCommandeIds.contains(entry.getKey()))
//                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
//
//            Map<String, Object> camions = optimisationService.getCamions();
//
//            // 2. Sauvegarde dans fichiers JSON
//            ObjectMapper mapper = new ObjectMapper();
//            mapper.writeValue(new File("commandes.json"), selectedCommandes);
//            mapper.writeValue(new File("camions.json"), camions);
//
//            // 3. Exécute le script Python
//            String pythonPath = "C:/Users/a/Desktop/optimisation-service/venv/Scripts/python.exe";
//            String scriptPath = "C:/Users/a/Desktop/optimisation-service/optimisation_script.py";
//            ProcessBuilder pb = new ProcessBuilder(pythonPath, scriptPath);
//            pb.redirectErrorStream(true);
//
//            Process process = pb.start();
//            String output = new BufferedReader(new InputStreamReader(process.getInputStream()))
//                    .lines().collect(Collectors.joining("\n"));
//
//            int exitCode = process.waitFor();
//
//            if (exitCode == 0) {
//                return output;
//            } else {
//                return "Erreur durant l'exécution du script (code " + exitCode + ")\n" + output;
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return "Erreur serveur : " + e.getMessage();
//        }
//    }


    @GetMapping("/result")
    public ResponseEntity<?> getOptimisationResult() {
        try {
            File resultFile = new File("resultats_affectation.json");
            if (!resultFile.exists()) {
                return ResponseEntity.status(404).body("Aucun résultat trouvé.");
            }

            ObjectMapper mapper = new ObjectMapper();
            Object result = mapper.readValue(resultFile, Object.class);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur lecture résultats : " + e.getMessage());
        }
    }

    @GetMapping("/transfer")
    public ResponseEntity<String> transferResults() {
        String result = resultTransferService.transferResultsToLivraisonService();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/selected")
    public ResponseEntity<String> optimiserCommandesSelectionnees(@RequestBody List<Long> selectedCommandeIds) {
        try {
            optimisationService.lancerOptimisationAvecCommandes(selectedCommandeIds);
            return ResponseEntity.ok("Optimisation lancée avec succès pour les commandes sélectionnées.");
        } catch (Exception e) {
            e.printStackTrace(); // Important pour le débogage
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur : " + e.getMessage());
        }
    }
}
