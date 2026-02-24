package com.ciamanutencao.production.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.ciamanutencao.production.entities.Order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OrderDTO(
        Long id,

        @NotNull(message = "A seção onde será realizado o serviço não pode ser nula")
        CategoryDTO category, 

        @NotNull(message = "O requisitante não pode ser nulo")
        UserDTO requester, 

        @NotNull(message = "O técnico que irá realizar o serviço não pode ser nulo")
        TechnicalDTO technical, 

        @NotBlank(message = "A data de abertura da O.S não pode ser nula")
        LocalDateTime openingDate, 

        LocalDateTime closingDate, 

        @NotBlank(message = "O local onde o serviço será realizado não pode ser nulo")
        String serviceLocation, 

        String observation,
        
        List<OrderItemDTO> items,
        
        BigDecimal totalValue) {

    public OrderDTO(Order entity) {
        this(
            entity.getId(), 
            new CategoryDTO(entity.getCategory()), 
            new UserDTO(entity.getRequester()),
            entity.getTechnical() != null ? new TechnicalDTO(entity.getTechnical()) : null, 
            entity.getOpeningDate(), 
            entity.getClosingDate(), 
            entity.getServiceLocation(), 
            entity.getObservation(),
            entity.getItems().stream().map(OrderItemDTO::new).toList(),
            entity.getTotalOrderValue()
        );
    }

}