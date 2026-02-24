package com.ciamanutencao.production.dto;

import com.ciamanutencao.production.entities.Department;

import jakarta.validation.constraints.NotBlank;

public record DepartmentDTO(
    Long id, 

    @NotBlank(message = "O nome da categoria não pode ser vazio ou nulo")
    String name,
    
    Boolean active) {

    public DepartmentDTO(Department entity) {
        this(entity.getId(), entity.getName(), entity.getActive());
    }
}