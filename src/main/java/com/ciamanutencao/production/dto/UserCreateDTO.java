package com.ciamanutencao.production.dto;

import com.ciamanutencao.production.entities.Department;
import com.ciamanutencao.production.enums.UserRole;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserCreateDTO(
    @NotBlank(message = "O login é obrigatório") 
    String login,

    @NotBlank(message = "A senha é obrigatória")
    @Size(min = 8, message = "A senha deve ter no mínimo 8 caracteres")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z]).*$", message = "A senha deve conter letras e números")
    String password,

    @NotBlank(message = "O nome é obrigatório") 
    String name,

    Department department,
    UserRole userRole,
    Boolean active
) {}