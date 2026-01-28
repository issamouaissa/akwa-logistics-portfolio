/*
package org.sid.commandeservice.services;


import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.sid.commandeservice.entities.Commande;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class FactureService {

    public byte[] genererFacturePDF(Commande commande) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // 📌 Ajout du titre
            document.add(new Paragraph("Facture de Commande")
                    .setBold().setFontSize(18));

            // 📋 Ajout des détails de la commande
            Table table = new Table(2);
            table.addCell("ID Commande:");
            table.addCell(String.valueOf(commande.getId()));

            table.addCell("Date:");
            table.addCell(commande.getDateHeureLivraison().toString());

            table.addCell("Poids livré:");
            table.addCell(commande.getQuantite() + "T");

            table.addCell("Type de Carburant:");
            table.addCell(String.valueOf(commande.getTypeCarburant())); // Ajoutez `String.valueOf()`


            table.addCell("Prix Total:");
            table.addCell(commande.getPrixTotal() + " MAD");

            document.add(table);
            document.close();

            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        }
    }

        public byte[] genererFactureExcel(Commande commande) {
            try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                Sheet sheet = workbook.createSheet("Facture");

                // Style pour les titres
                CellStyle headerStyle = workbook.createCellStyle();
                Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                headerStyle.setFont(headerFont);

                // Créer les lignes et cellules
                int rowIdx = 0;

                // Titre
                Row titleRow = sheet.createRow(rowIdx++);
                Cell titleCell = titleRow.createCell(0);
                titleCell.setCellValue("Facture de Commande");
                titleCell.setCellStyle(headerStyle);
                sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 1));

                // Détails commande
                Object[][] data = {
                        {"ID Commande:", String.valueOf(commande.getId())},
                        {"Date:", commande.getDateHeureLivraison().toString()},
                        {"Poids livré:", commande.getQuantite() + "T"},
                        {"Type de Carburant:", String.valueOf(commande.getTypeCarburant())},
                        {"Prix Total:", commande.getPrixTotal() + " MAD"}
                };

                for (Object[] entry : data) {
                    Row row = sheet.createRow(rowIdx++);
                    row.createCell(0).setCellValue((String) entry[0]);
                    row.createCell(1).setCellValue((String) entry[1]);
                }

                workbook.write(out);
                return out.toByteArray();
            } catch (Exception e) {
                throw new RuntimeException("Erreur lors de la génération du fichier Excel", e);
            }
        }

}
*/
