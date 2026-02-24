package com.ciamanutencao.production.entities;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name="tb_order")
public class Order implements Serializable{
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User requester;

    @ManyToOne
    @JoinColumn(name = "technical_id")
    private Technical technical;

    private LocalDateTime openingDate;
    private LocalDateTime closingDate;
    private String serviceLocation;
    private String observation;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();
    
    public Order() {
    }

    public Order(Long id, Category category, User requester, Technical technical, LocalDateTime openingDate, LocalDateTime closingDate, String serviceLocation, String observation) {
        this.id = id;
        this.category = category;
        this.requester = requester;
        this.technical = technical;
        this.openingDate = openingDate;
        this.closingDate = closingDate;
        this.serviceLocation = serviceLocation;
        this.observation = observation;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public User getRequester() {
        return requester;
    }

    public void setRequester(User requester) {
        this.requester = requester;
    }

    public Technical getTechnical() {
        return technical;
    }

    public void setTechnical(Technical technical) {
        this.technical = technical;
    }

    public LocalDateTime getOpeningDate() {
        return openingDate;
    }

    public void setOpeningDate(LocalDateTime openingDate) {
        this.openingDate = openingDate;
    }

    public LocalDateTime getClosingDate() {
        return closingDate;
    }

    public void setClosingDate(LocalDateTime closingDate) {
        this.closingDate = closingDate;
    }
    public String getServiceLocation() {
        return serviceLocation;
    }

    public void setServiceLocation(String serviceLocation) {
        this.serviceLocation = serviceLocation;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    public List<OrderItem> getItems() {
        return items;   
    }

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
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
        Order other = (Order) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    
    public BigDecimal getTotalOrderValue(){
        return items.stream().map(OrderItem::totalValue).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    

}
