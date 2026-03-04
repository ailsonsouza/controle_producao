package com.ciamanutencao.production.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ciamanutencao.production.entities.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNumber(String orderNumber);

    List<Order> findByRequesterDepartmentId(Long departmentId);
    
    @Query("SELECT MAX(o.sequence) FROM Order o WHERE o.year = :year")
    Integer findMaxSequenceByYear(@Param("year") int year);

}
