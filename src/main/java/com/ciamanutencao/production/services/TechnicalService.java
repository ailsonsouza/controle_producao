package com.ciamanutencao.production.services;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ciamanutencao.production.dto.TechnicalDTO;
import com.ciamanutencao.production.entities.Technical;
import com.ciamanutencao.production.entities.User;
import com.ciamanutencao.production.enums.UserRole;
import com.ciamanutencao.production.exceptions.ResourceNotFoundException;
import com.ciamanutencao.production.repositories.TechnicalRepository;
import com.ciamanutencao.production.services.utils.SecurityUtils;

import jakarta.persistence.EntityNotFoundException;

@Service
public class TechnicalService {

    private final TechnicalRepository technicalRepository;

    private final SecurityUtils securityUtils;

    public TechnicalService(TechnicalRepository technicalRepository, SecurityUtils securityUtils) {
        this.technicalRepository = technicalRepository;
        this.securityUtils = securityUtils;
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CHIEF')")
    @Transactional
    public TechnicalDTO createTechnical(TechnicalDTO dto) {

        securityUtils.checkDepartmentAccess(dto.departments());

        Technical technical = new Technical();
        copyDtoToEntity(dto, technical);
        technical = technicalRepository.save(technical);
        return new TechnicalDTO(technical);
    }

    @Transactional(readOnly = true)
    public List<TechnicalDTO> findAllTechnicians() {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Technical> list;

        if (currentUser.getUserRole() == UserRole.ROLE_ADMIN) {
            list = technicalRepository.findAll();
        } else {
            list = technicalRepository.findByDepartments_Id(currentUser.getDepartment().getId());
        }

        return list.stream().map(TechnicalDTO::new).toList();
    }

    @Transactional(readOnly = true)
    public TechnicalDTO findTechnicalById(Long id) {
        Technical technical = technicalRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(id));
        return new TechnicalDTO(technical);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CHIEF')")
    @Transactional
    public TechnicalDTO updateTechnical(Long id, TechnicalDTO dto) {
        try {

            securityUtils.checkDepartmentAccess(dto.departments());

            Technical entity = technicalRepository.getReferenceById(id);
            copyDtoToEntity(dto, entity);
            entity = technicalRepository.save(entity);
            return new TechnicalDTO(entity);
        } catch (EntityNotFoundException e) {
            throw new ResourceNotFoundException(id);
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CHIEF')")
    @Transactional
    public void deleteTechnical(Long id) {

        if (!technicalRepository.existsById(id)) {
            throw new ResourceNotFoundException(id);
        }

        securityUtils.checkDepartmentAccess(technicalRepository.getReferenceById(id).getDepartments());

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
