package com.ciamanutencao.production.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ciamanutencao.production.entities.Order;

public interface OrderItemRepository extends JpaRepository<Order, Long> {


}
