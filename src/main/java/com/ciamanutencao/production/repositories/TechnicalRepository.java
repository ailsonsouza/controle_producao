package com.ciamanutencao.production.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ciamanutencao.production.entities.Technical;

public interface TechnicalRepository extends JpaRepository<Technical, Long> {

}
