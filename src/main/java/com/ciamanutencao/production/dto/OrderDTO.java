package com.ciamanutencao.production.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.ciamanutencao.production.entities.Order;
import com.ciamanutencao.production.enums.OrderStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OrderDTO(
        Long id,
        String orderNumber,
        Integer sequence,
        Integer year,

        @NotNull(message = "A seção onde será realizado o serviço não pode ser nula") CategoryDTO category,

        @NotNull(message = "O requisitante não pode ser nulo") UserDTO requester,

        @NotNull(message = "Favor informar o técnico responsável pelo serviço") TechnicalDTO technical,

        OrderStatus orderStatus,
        LocalDateTime openingDate,
        LocalDateTime closingDate,

        @NotBlank(message = "O local do serviço é obrigatório") String serviceLocation,

        String observation,
        List<OrderItemDTO> items,
        BigDecimal totalValue) {

    public OrderDTO(Order entity) {
        this(
                entity.getId(),
                entity.getOrderNumber(),
                entity.getSequence(),
                entity.getYear(),
                new CategoryDTO(entity.getCategory()),
                new UserDTO(entity.getRequester()),
                new TechnicalDTO(entity.getTechnical()),
                entity.getOrderStatus(),
                entity.getOpeningDate(),
                entity.getClosingDate(),
                entity.getServiceLocation(),
                entity.getObservation(),
                entity.getItems().stream().map(OrderItemDTO::new).toList(),
                entity.getTotalOrderValue());
    }
}