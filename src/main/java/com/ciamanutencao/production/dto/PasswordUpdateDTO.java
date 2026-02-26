package com.ciamanutencao.production.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PasswordUpdateDTO(
    @NotBlank(message = "O login é obrigatório") 
    String login,
    
    @NotBlank(message = "A senha antiga é obrigatória") 
    String oldPassword,
    
    @NotBlank(message = "A nova senha é obrigatória")
    @Size(min = 8, message = "A senha deve ter no mínimo 8 caracteres")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z]).*$", message = "A senha deve conter letras e números")
    String newPassword,

    @NotBlank(message = "A confirmação da senha é obrigatória")
    String confirmNewPassword
) {}