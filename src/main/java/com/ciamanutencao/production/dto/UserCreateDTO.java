package com.ciamanutencao.production.dto;

import com.ciamanutencao.production.entities.Department;
import com.ciamanutencao.production.enums.UserRole;

import jakarta.validation.constraints.NotBlank;

public record UserCreateDTO(
    @NotBlank String login,
    @NotBlank String password,
    @NotBlank String name,
    Department department,
    UserRole userRole,
    Boolean active
) {}