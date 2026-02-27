package com.ciamanutencao.production.dto;

import java.math.BigDecimal;
import com.ciamanutencao.production.entities.OrderItem;

public record OrderItemDTO(
        Long id,
        Integer itemNumber,
        String itemDescription,
        BigDecimal itemPrice,
        String serialNumber,
        String serviceDescription,
        BigDecimal servicePrice,
        BigDecimal subTotal) {

    public OrderItemDTO(OrderItem entity) {
        this(entity.getId(), entity.getItemNumber(), entity.getItemDescription(), entity.getItemPrice(),
                entity.getSerialNumber(),
                entity.getServiceDescription(), entity.getServicePrice(), entity.totalValue());
    }
}