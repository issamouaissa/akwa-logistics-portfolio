package org.sid.livraisonservice.services;


import org.sid.livraisonservice.entities.Livraison;
import org.sid.livraisonservice.entities.Tournee;

public class StatutUtils {

    public static String calculerStatutLivraison(Livraison livraison) {
        boolean allLivrees = livraison.getLignesLivraison().stream()
                .allMatch(l -> l.getQuantiteLivree() != null);

        return allLivrees ? "LIVREE" : "EN_COURS";
    }

    public static String calculerStatutTournee(Tournee tournee) {
        boolean allLivraisonsLivrees = tournee.getLivraisons().stream()
                .allMatch(l -> "LIVREE".equals(calculerStatutLivraison(l)));

        return allLivraisonsLivrees ? "TERMINEE" : "EN_COURS";
    }

}