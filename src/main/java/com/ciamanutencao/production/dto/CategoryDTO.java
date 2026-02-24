package com.ciamanutencao.production.dto;

import com.ciamanutencao.production.entities.Category;

import jakarta.validation.constraints.NotBlank;

public record CategoryDTO(
    Long id, 

    @NotBlank(message = "O nome da categoria não pode ser vazio ou nulo")
    String name, 
    
    Boolean active) {

    public CategoryDTO(Category entity) {
        this(entity.getId(), entity.getName(), entity.getActive());
    }
}