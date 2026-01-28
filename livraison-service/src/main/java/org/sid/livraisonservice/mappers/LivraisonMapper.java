package org.sid.livraisonservice.mappers;

import org.sid.livraisonservice.dtos.DetailLigneLivraisonDTO;
import org.sid.livraisonservice.dtos.LigneLivraisonDTO;
import org.sid.livraisonservice.dtos.LivraisonDTO;
import org.sid.livraisonservice.dtos.TourneeDTO;
import org.sid.livraisonservice.entities.DetailLigneLivraison;
import org.sid.livraisonservice.entities.LigneLivraison;
import org.sid.livraisonservice.entities.Livraison;
import org.sid.livraisonservice.entities.Tournee;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class LivraisonMapper {

    public LivraisonDTO toDTO(Livraison entity) {
        LivraisonDTO dto = new LivraisonDTO();
        BeanUtils.copyProperties(entity, dto);

        if (entity.getLignesLivraison() != null) {
            // Grouper les lignes par codeProduit
            Map<Long, LigneLivraisonDTO> lignesRegroupees = new LinkedHashMap<>();

            for (LigneLivraison ligne : entity.getLignesLivraison()) {
                Long codeProduit = ligne.getCodeProduit();

                // Si c’est la première fois, on initialise la ligne complète
                if (!lignesRegroupees.containsKey(codeProduit)) {
                    LigneLivraisonDTO dtoL = new LigneLivraisonDTO();
                    BeanUtils.copyProperties(ligne, dtoL);
                    dtoL.setDetailsCompartiments(new ArrayList<>());

                    lignesRegroupees.put(codeProduit, dtoL);
                }

                // Ajouter les compartiments de cette ligne
                LigneLivraisonDTO ligneRegroupee = lignesRegroupees.get(codeProduit);
                if (ligne.getDetailsCompartiments() != null) {
                    List<DetailLigneLivraisonDTO> detailDTOs = ligne.getDetailsCompartiments().stream().map(detail -> {
                        DetailLigneLivraisonDTO dtoDetail = new DetailLigneLivraisonDTO();
                        BeanUtils.copyProperties(detail, dtoDetail);
                        return dtoDetail;
                    }).toList();

                    ligneRegroupee.getDetailsCompartiments().addAll(detailDTOs);
                }
            }

            dto.setLignesLivraison(new ArrayList<>(lignesRegroupees.values()));
        }

        if (entity.getTournee() != null) {
            TourneeDTO tourneeDTO = new TourneeDTO();
            BeanUtils.copyProperties(entity.getTournee(), tourneeDTO);
            dto.setTournee(tourneeDTO);
        }

        return dto;
    }

    public Livraison toEntity(LivraisonDTO dto) {
        Livraison entity = new Livraison();
        BeanUtils.copyProperties(dto, entity);

        if (dto.getLignesLivraison() != null) {
            List<LigneLivraison> lignes = dto.getLignesLivraison().stream().map(dtoL -> {
                LigneLivraison ligne = new LigneLivraison();
                BeanUtils.copyProperties(dtoL, ligne);
                ligne.setLivraison(entity);

                if (dtoL.getDetailsCompartiments() != null) {
                    List<DetailLigneLivraison> detailEntities = dtoL.getDetailsCompartiments().stream().map(detailDTO -> {
                        DetailLigneLivraison detail = new DetailLigneLivraison();
                        BeanUtils.copyProperties(detailDTO, detail);
                        detail.setLigneLivraison(ligne);
                        return detail;
                    }).toList();

                    ligne.setDetailsCompartiments(detailEntities);
                }

                return ligne;
            }).toList();

            entity.setLignesLivraison(lignes);
        }

        if (dto.getTournee() != null) {
            Tournee tournee = new Tournee();
            BeanUtils.copyProperties(dto.getTournee(), tournee);
            entity.setTournee(tournee);
        }

        return entity;
    }

    public TourneeDTO toTourneeDTOWithLivraisons(Tournee tournee) {
        TourneeDTO dto = new TourneeDTO();
        BeanUtils.copyProperties(tournee, dto);

        if (tournee.getLivraisons() != null) {
            List<LivraisonDTO> livraisons = tournee.getLivraisons().stream()
                    .map(livraison -> {
                        LivraisonDTO livDto = toDTO(livraison);
                        livDto.setTournee(null); // éviter boucle infinie JSON
                        return livDto;
                    })
                    .toList();

            dto.setLivraisons(livraisons);
        }

        return dto;
    }
}
