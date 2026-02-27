package com.ciamanutencao.production.services;

import java.util.List;

//import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ciamanutencao.production.dto.TechnicalDTO;
import com.ciamanutencao.production.entities.Technical;
import com.ciamanutencao.production.exceptions.ResourceNotFoundException;
import com.ciamanutencao.production.repositories.TechnicalRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class TechnicalService {

    private final TechnicalRepository technicalRepository;

    public TechnicalService(TechnicalRepository technicalRepository) {
        this.technicalRepository = technicalRepository;
    }

    @Transactional
    public TechnicalDTO createTechnical(TechnicalDTO dto) {
        Technical technical = new Technical();
        copyDtoToEntity(dto, technical);
        technical = technicalRepository.save(technical);
        return new TechnicalDTO(technical);
    }

    @Transactional(readOnly = true)
    public List<TechnicalDTO> findAllTechnicians() {
        List<Technical> list = technicalRepository.findAll();
        return list.stream().map(TechnicalDTO::new).toList();
    }

    @Transactional(readOnly = true)
    public TechnicalDTO findTechnicalById(Long id) {
        Technical technical = technicalRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(id));
        return new TechnicalDTO(technical);
    }

    @Transactional
    public TechnicalDTO updateTechnical(Long id, TechnicalDTO dto) {
        try {
            Technical entity = technicalRepository.getReferenceById(id);
            copyDtoToEntity(dto, entity);
            entity = technicalRepository.save(entity);
            return new TechnicalDTO(entity);
        } catch (EntityNotFoundException e) {
            throw new ResourceNotFoundException(id);
        }
    }

    @Transactional
    public void deleteTechnical(Long id) {
        if (!technicalRepository.existsById(id)) {
            throw new ResourceNotFoundException(id);
        }
        technicalRepository.deleteById(id);
    }

    private void copyDtoToEntity(TechnicalDTO dto, Technical entity) {
        entity.setName(dto.name());
        entity.setActive(dto.active() != null ? dto.active() : true);

        entity.getDepartments().clear();
        dto.departments().forEach(deptDto -> {
            entity.getDepartments().add(deptDto);
        });
    }

}
