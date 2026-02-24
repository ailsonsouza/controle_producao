package com.ciamanutencao.production.dto;

import com.ciamanutencao.production.entities.Department;
import com.ciamanutencao.production.entities.Technical;

import jakarta.validation.constraints.NotBlank;

public record TechnicalDTO(
    Long id, 
    
    @NotBlank(message = "O departamento não pode ser vazio ou nulo")
    Department department, 
    
    @NotBlank(message = "O nome da categoria não pode ser vazio ou nulo")
    String name, 
    
    Boolean active) {
        

    public TechnicalDTO(Technical entity) {
        this(entity.getId(), entity.getDepartment(), entity.getName(), entity.getActive());
    }
}