package com.ciamanutencao.production.services;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ciamanutencao.production.dto.DepartmentDTO;
import com.ciamanutencao.production.entities.Department;
import com.ciamanutencao.production.exceptions.ResourceNotFoundException;
import com.ciamanutencao.production.repositories.DepartmentRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class DepartmentService {

    private final DepartmentRepository DepartmentRepository;

    public DepartmentService(DepartmentRepository DepartmentRepository) {
        this.DepartmentRepository = DepartmentRepository;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public DepartmentDTO createDepartment(DepartmentDTO DepartmentDto) {
        Department Department = new Department();
        Department.setName(DepartmentDto.name());
        Department.setActive(DepartmentDto.active());
        Department = DepartmentRepository.save(Department);
        return new DepartmentDTO(Department);

    }

    @Transactional(readOnly = true)
    public List<DepartmentDTO> findAllDepartments() {
        List<Department> list = DepartmentRepository.findAll();
        return list.stream().map(DepartmentDTO::new).toList();
    }

    @Transactional(readOnly = true)
    public DepartmentDTO findDepartmentById(Long id) {
        Department Department = DepartmentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(id));
        return new DepartmentDTO(Department);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public DepartmentDTO updateDepartment(Long id, DepartmentDTO updatedDepartment) {
        try {
            Department entity = DepartmentRepository.getReferenceById(id);
            entity.setName(updatedDepartment.name());
            entity.setActive(updatedDepartment.active());

            entity = DepartmentRepository.save(entity);

            return new DepartmentDTO(entity);
        } catch (EntityNotFoundException e) {
            throw new ResourceNotFoundException(id);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void deleteDepartment(Long id) {
        if (!DepartmentRepository.existsById(id)) {
            throw new ResourceNotFoundException(id);
        }
        DepartmentRepository.deleteById(id);
    }

}
