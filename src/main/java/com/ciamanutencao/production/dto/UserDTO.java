package com.ciamanutencao.production.dto;

import com.ciamanutencao.production.entities.Department;
import com.ciamanutencao.production.entities.User;
import com.ciamanutencao.production.enums.UserRole;

import jakarta.validation.constraints.NotBlank;

public record UserDTO(
    Long id, 
    
    @NotBlank(message = "O departamento não pode ser vazio ou nulo")
    Department department, 
    
    @NotBlank(message = "O nome da categoria não pode ser vazio ou nulo")
    String name, 
    
    @NotBlank(message = "O nível de acesso do usuário não pode ser vazio")
    UserRole userRole, 
    
    Boolean active) {

    public UserDTO(User entity) {
        this(entity.getId(), entity.getDepartment(), entity.getName(), entity.getUserRole(), entity.getActive());
    }
}