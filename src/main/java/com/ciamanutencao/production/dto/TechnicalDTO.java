package com.ciamanutencao.production.dto;

import java.util.Set;

import com.ciamanutencao.production.entities.Department;
import com.ciamanutencao.production.entities.Technical;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record TechnicalDTO(
    Long id, 
    
    @NotEmpty
    Set<Department> departments, 
    
    @NotBlank(message = "O nome da categoria não pode ser vazio ou nulo")
    String name, 
    
    Boolean active) {
        

    public TechnicalDTO(Technical entity) {
        this(entity.getId(), entity.getDepartments(), entity.getName(), entity.getActive());
    }
}