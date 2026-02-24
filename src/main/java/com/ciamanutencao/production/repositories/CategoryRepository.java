package com.ciamanutencao.production.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ciamanutencao.production.entities.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
