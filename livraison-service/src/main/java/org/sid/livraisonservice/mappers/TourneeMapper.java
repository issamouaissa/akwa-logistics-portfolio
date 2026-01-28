//package org.sid.livraisonservice.mappers;
//
//import org.sid.livraisonservice.dtos.TourneeDTO;
//import org.sid.livraisonservice.entities.Tournee;
//import org.springframework.beans.BeanUtils;
//import org.springframework.stereotype.Component;
//
//@Component
//public class TourneeMapper {
//    public TourneeDTO toDTO(Tournee entity) {
//        TourneeDTO dto = new TourneeDTO();
//        BeanUtils.copyProperties(entity, dto);
//        return dto;
//    }
//
//    public Tournee toEntity(TourneeDTO dto) {
//        Tournee entity = new Tournee();
//        BeanUtils.copyProperties(dto, entity);
//        return entity;
//    }
//}
