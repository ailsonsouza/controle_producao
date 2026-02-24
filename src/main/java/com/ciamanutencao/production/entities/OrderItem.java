package com.ciamanutencao.production.entities;

import java.io.Serializable;
import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_order_item")
public class OrderItem implements Serializable{
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String itemDescription;
    private String serialNumber;
    private String serviceDescription;
    private BigDecimal itemPrice;
    private BigDecimal servicePrice;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    public OrderItem() {
    }

    public OrderItem(Long id, String itemDescription, String serialNumber, String serviceDescription,
            BigDecimal itemPrice, BigDecimal servicePrice, Order order) {
        this.id = id;
        this.itemDescription = itemDescription;
        this.serialNumber = serialNumber;
        this.serviceDescription = serviceDescription;
        this.itemPrice = itemPrice;
        this.servicePrice = servicePrice;
        this.order = order;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getServiceDescription() {
        return serviceDescription;
    }

    public void setServiceDescription(String serviceDescription) {
        this.serviceDescription = serviceDescription;
    }

    public BigDecimal getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(BigDecimal itemPrice) {
        this.itemPrice = itemPrice;
    }

    public BigDecimal getServicePrice() {
        return servicePrice;
    }

    public void setServicePrice(BigDecimal servicePrice) {
        this.servicePrice = servicePrice;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OrderItem other = (OrderItem) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    public BigDecimal totalValue() {
        BigDecimal item = (itemPrice != null) ? itemPrice : BigDecimal.ZERO;
        BigDecimal service = (servicePrice != null) ? servicePrice : BigDecimal.ZERO;

        return item.add(service);
    }


}
