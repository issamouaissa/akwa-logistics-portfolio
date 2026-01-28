package org.sid.commandeservice.services;

import org.sid.commandeservice.entities.Commande;
import org.sid.commandeservice.entities.LigneCommande;
import org.sid.commandeservice.repositories.CommandeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import java.io.ByteArrayOutputStream;


import java.util.*;
import java.util.List;
import java.util.stream.Stream;

@Service
public class CommandeService {

    @Autowired
    private CommandeRepository repository;

    public List<Commande> getAll() {
        return repository.findAll();
    }

    private static int nextReferenceCommande = 370000;
    private static int nextCodeProduit = 952300;


    public Commande save(Commande commande) {
        if (commande.getDateCommande() == null) {
            commande.setDateCommande(new Date());
        }

        // Récupérer les infos station (flexibilité + userId)
        if (commande.getStationId() != null) {
            try {
                RestTemplate restTemplate = new RestTemplate();
                String url = "http://localhost:8090/api/stations/" + commande.getStationId();
                StationDTO station = restTemplate.getForObject(url, StationDTO.class);
                if (station != null) {
                    if (Boolean.TRUE.equals(station.getFlexibilite())) {
                        commande.setTdgmin(Optional.ofNullable(commande.getTdgmin()).orElse(-3.0));
                        commande.setTdgmax(Optional.ofNullable(commande.getTdgmax()).orElse(3.0));
                        commande.setTdemin(Optional.ofNullable(commande.getTdemin()).orElse(-1.0));
                        commande.setTdemax(Optional.ofNullable(commande.getTdemax()).orElse(1.0));
                    } else {
                        commande.setTdgmin(0.0);
                        commande.setTdgmax(0.0);
                        commande.setTdemin(0.0);
                        commande.setTdemax(0.0);
                    }
                    commande.setUserId(station.getUserId());
                }
            } catch (Exception e) {
                System.out.println("⚠️ Erreur récupération station : " + e.getMessage());
            }
        }

        // Référence de commande
        if (commande.getReferenceCommande() == null) {
            String lastRef = repository.findLastReferenceCommande();
            int nextRef = (lastRef != null) ? Integer.parseInt(lastRef) + 1 : 370000;
            commande.setReferenceCommande(String.format("%06d", nextRef));
        }

        // Génération des lignes
        List<LigneCommande> lignes = new ArrayList<>();
        if (commande.getGasoil() != null && commande.getGasoil() > 0) {
            LigneCommande ligneGasoil = new LigneCommande();
            ligneGasoil.setQuantiteDemandee(commande.getGasoil());
            ligneGasoil.setPrixUnitaire(9.9);
            ligneGasoil.setCodeProduit((long) ++nextCodeProduit);
            ligneGasoil.setCommande(commande);
            lignes.add(ligneGasoil);
        }
        if (commande.getEssence() != null && commande.getEssence() > 0) {
            LigneCommande ligneEssence = new LigneCommande();
            ligneEssence.setQuantiteDemandee(commande.getEssence());
            ligneEssence.setPrixUnitaire(10.2);
            ligneEssence.setCodeProduit((long) ++nextCodeProduit);
            ligneEssence.setCommande(commande);
            lignes.add(ligneEssence);
        }
        commande.setLignes(lignes);

        // Calcul TTC estimé
        double densiteGasoil = 1183;
        double densiteEssence = 1351;
        double litresGasoil = Optional.ofNullable(commande.getGasoil()).orElse(0.0) * densiteGasoil;
        double litresEssence = Optional.ofNullable(commande.getEssence()).orElse(0.0) * densiteEssence;
        double ttcEstime = (litresGasoil * 9.9) + (litresEssence * 10.2);
        commande.setTtcEstime(ttcEstime);

        // Total
        commande.setQuantitetotale(
                Optional.ofNullable(commande.getGasoil()).orElse(0.0) +
                        Optional.ofNullable(commande.getEssence()).orElse(0.0)
        );

        // Déterminer le statut initial
        String statut = "EN_COURS";
        boolean anyProgrammee = lignes.stream().anyMatch(l -> l.getQuantiteProgrammee() != null);
        boolean anyLivree = lignes.stream().anyMatch(l -> l.getQuantiteLivree() != null);
        if (anyLivree) {
            statut = "LIVREE";
        } else if (anyProgrammee) {
            statut = "EN_LIVRAISON";
        }
        commande.setStatut(statut);

        return repository.save(commande);
    }




    public List<Map<String, Object>> getAllCommandesEnriched() {
        List<Commande> commandes = repository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        RestTemplate restTemplate = new RestTemplate();

        for (Commande c : commandes) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", c.getId());
            map.put("gasoil", c.getGasoil());
            map.put("essence", c.getEssence());
            map.put("quantitetotale", c.getQuantitetotale());
            map.put("tdgmin", c.getTdgmin());
            map.put("tdgmax", c.getTdgmax());
            map.put("tdemin", c.getTdemin());
            map.put("tdemax", c.getTdemax());
            map.put("stationId", c.getStationId());
            map.put("dateCommande", c.getDateCommande());
            map.put("ttcEstime", c.getTtcEstime());
            map.put("referenceCommande", c.getReferenceCommande());
            map.put("userId", c.getUserId());
            map.put("status", c.getStatut());



            // ✅ lignes de commande
            map.put("lignes", c.getLignes().stream().map(ligne -> {
                Map<String, Object> ligneMap = new HashMap<>();
                ligneMap.put("codeProduit", ligne.getCodeProduit());
                ligneMap.put("quantiteDemandee", ligne.getQuantiteDemandee());
                ligneMap.put("quantiteProgrammee", ligne.getQuantiteProgrammee());
                ligneMap.put("quantiteLivree", ligne.getQuantiteLivree());
                ligneMap.put("prixUnitaire", ligne.getPrixUnitaire());
                return ligneMap;
            }).toList());



            // 🔄 Station
            try {
                String url = "http://localhost:8090/api/stations/" + c.getStationId();
                Object station = restTemplate.getForObject(url, Object.class);
                if (station instanceof Map<?, ?> stationMap) {
                    map.put("libelle", stationMap.get("libelle"));
                    map.put("code", stationMap.get("code"));
                    map.put("adresse", stationMap.get("adresse"));
                    map.put("ville", stationMap.get("ville"));
                    map.put("contact", stationMap.get("contact"));
                    map.put("telephone", stationMap.get("telephone"));
                    map.put("posilat", stationMap.get("posilat"));
                    map.put("posilong", stationMap.get("posilong"));
                    map.put("pompe", stationMap.get("pompe"));
                    map.put("solo", stationMap.get("solo"));
                    map.put("normal", stationMap.get("normal"));
                    map.put("flexibilite", stationMap.get("flexibilite"));
                }

            } catch (Exception e) {
                map.put("posilat", null);
                map.put("posilong", null);
            }

            result.add(map);
        }
        return result;
    }

    public Optional<Commande> getById(Long id) {
        return repository.findById(id);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    // DTO pour lecture API
    public static class StationDTO {
        private Boolean flexibilite;
        private Long userId;

        public Boolean getFlexibilite() {
            return flexibilite;
        }

        public void setFlexibilite(Boolean flexibilite) {
            this.flexibilite = flexibilite;
        }

        public Long getUserId() {
            return userId;
        }
        public void setUserId(Long userId) {
            this.userId = userId;
        }
    }

    public List<Commande> getByUserId(Long userId) {
        return repository.findByUserId(userId);
    }

    public List<Map<String, Object>> getCommandesEnrichedByUser(Long userId) {
        List<Commande> commandes = repository.findByUserId(userId);
        List<Map<String, Object>> result = new ArrayList<>();
        RestTemplate restTemplate = new RestTemplate();

        for (Commande c : commandes) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", c.getId());
            map.put("gasoil", c.getGasoil());
            map.put("essence", c.getEssence());
            map.put("quantitetotale", c.getQuantitetotale());
            map.put("tdgmin", c.getTdgmin());
            map.put("tdgmax", c.getTdgmax());
            map.put("tdemin", c.getTdemin());
            map.put("tdemax", c.getTdemax());
            map.put("stationId", c.getStationId());
            map.put("dateCommande", c.getDateCommande());
            map.put("ttcEstime", c.getTtcEstime());
            map.put("referenceCommande", c.getReferenceCommande());
            map.put("userId", c.getUserId());
            map.put("status", c.getStatut());



            // ✅ lignes de commande
            map.put("lignes", c.getLignes().stream().map(ligne -> {
                Map<String, Object> ligneMap = new HashMap<>();
                ligneMap.put("codeProduit", ligne.getCodeProduit());
                ligneMap.put("quantiteDemandee", ligne.getQuantiteDemandee());
                ligneMap.put("quantiteProgrammee", ligne.getQuantiteProgrammee());
                ligneMap.put("quantiteLivree", ligne.getQuantiteLivree());
                ligneMap.put("prixUnitaire", ligne.getPrixUnitaire());
                return ligneMap;
            }).toList());



            // 🔄 Station
            try {
                String url = "http://localhost:8090/api/stations/" + c.getStationId();
                Object station = restTemplate.getForObject(url, Object.class);
                if (station instanceof Map<?, ?> stationMap) {
                    map.put("posilat", stationMap.get("posilat"));
                    map.put("posilong", stationMap.get("posilong"));
                    map.put("pompe", stationMap.get("pompe"));
                    map.put("solo", stationMap.get("solo"));
                    map.put("normal", stationMap.get("normal"));
                }
            } catch (Exception e) {
                map.put("posilat", null);
                map.put("posilong", null);
            }

            result.add(map);
        }
        return result;
    }
    public ResponseEntity<byte[]> generateFacture(Long id, String format) {
        Optional<Commande> opt = repository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        Commande cmd = opt.get();

        if ("excel".equalsIgnoreCase(format)) {
            // Format CSV (Excel)
            StringBuilder sb = new StringBuilder();
            sb.append("ID,Date,Gasoil (T),Essence (T),Total (T),TTC Estimé (MAD)\n");
            sb.append(cmd.getId()).append(",")
                    .append(cmd.getDateCommande()).append(",")
                    .append(cmd.getGasoil()).append(",")
                    .append(cmd.getEssence()).append(",")
                    .append(cmd.getQuantitetotale()).append(",")
                    .append(cmd.getTtcEstime());

            byte[] fileBytes = sb.toString().getBytes();
            String filename = "facture-" + cmd.getReferenceCommande() + ".csv";
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=" + filename)
                    .header("Content-Type", "text/csv")
                    .body(fileBytes);
        }

        // Format PDF
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Font normalFont = new Font(Font.FontFamily.HELVETICA, 12);

            document.add(new Paragraph("Facture de Commande", titleFont));
            document.add(new Paragraph("Référence : " + cmd.getReferenceCommande(), normalFont));
            document.add(new Paragraph("Date : " + cmd.getDateCommande(), normalFont));
            document.add(new Paragraph("Station ID : " + cmd.getStationId(), normalFont));
            document.add(new Paragraph(" ")); // Ligne vide

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new int[]{2, 2, 2, 2, 2});

            Stream.of("Code Produit", "Qté Demandée", "Qté Programmée", "Qté Livrée", "Prix U.")
                    .forEach(header -> {
                        PdfPCell cell = new PdfPCell(new Phrase(header));
                        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                        table.addCell(cell);
                    });

            for (LigneCommande ligne : cmd.getLignes()) {
                table.addCell(ligne.getCodeProduit().toString());
                table.addCell(String.valueOf(ligne.getQuantiteDemandee()));
                table.addCell(String.valueOf(ligne.getQuantiteProgrammee()));
                table.addCell(String.valueOf(ligne.getQuantiteLivree()));
                table.addCell(String.valueOf(ligne.getPrixUnitaire()));
            }

            document.add(table);
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Total (T) : " + cmd.getQuantitetotale(), normalFont));
            document.add(new Paragraph("TTC estimé : " + cmd.getTtcEstime() + " MAD", normalFont));

            document.close();

            byte[] pdfBytes = out.toByteArray();
            String filename = "facture-" + cmd.getReferenceCommande() + ".pdf";
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=" + filename)
                    .header("Content-Type", "application/pdf")
                    .body(pdfBytes);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(("Erreur PDF: " + e.getMessage()).getBytes());
        }
    }


    public Optional<Commande> getByReference(String ref) {
        return repository.findByReferenceCommande(ref);
    }


    public Commande updateQuantites(Long id, Double gasoil, Double essence, Long stationId) {
        Commande commande = repository.findById(id).orElseThrow();

        commande.setGasoil(gasoil);
        commande.setEssence(essence);
        commande.setStationId(stationId);

        //  Recalculer les quantités demandées dans les lignes existantes
        for (LigneCommande ligne : commande.getLignes()) {
            if (ligne.getPrixUnitaire() == 9.9) {
                ligne.setQuantiteDemandee(gasoil);
            } else if (ligne.getPrixUnitaire() == 10.2) {
                ligne.setQuantiteDemandee(essence);
            }
        }

        //  Recalculer la quantité totale (T)
        double total = Optional.ofNullable(gasoil).orElse(0.0) + Optional.ofNullable(essence).orElse(0.0);
        commande.setQuantitetotale(total);

        //  Recalculer TTC
        double densiteGasoil = 1183;
        double densiteEssence = 1351;

        double litresGasoil = Optional.ofNullable(gasoil).orElse(0.0) * densiteGasoil;
        double litresEssence = Optional.ofNullable(essence).orElse(0.0) * densiteEssence;

        double ttcEstime = (litresGasoil * 9.9) + (litresEssence * 10.2);
        commande.setTtcEstime(ttcEstime);

        return repository.save(commande);
    }


}
