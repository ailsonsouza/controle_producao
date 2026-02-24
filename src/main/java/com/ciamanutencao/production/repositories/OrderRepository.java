package com.ciamanutencao.production.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ciamanutencao.production.entities.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

}
