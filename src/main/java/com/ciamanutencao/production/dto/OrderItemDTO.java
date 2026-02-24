package com.ciamanutencao.production.dto;

import java.math.BigDecimal;
import com.ciamanutencao.production.entities.OrderItem;

public record OrderItemDTO(
        Long id,
        String itemDescription,
        BigDecimal itemPrice,
        BigDecimal servicePrice,
        BigDecimal subTotal) {

    public OrderItemDTO(OrderItem entity) {
        this(entity.getId(), entity.getItemDescription(), entity.getItemPrice(),
                entity.getServicePrice(), entity.totalValue());
    }
}