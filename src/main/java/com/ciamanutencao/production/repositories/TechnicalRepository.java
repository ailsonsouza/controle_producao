package com.ciamanutencao.production.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ciamanutencao.production.entities.Technical;

public interface TechnicalRepository extends JpaRepository<Technical, Long> {

    List<Technical> findByDepartments_Id(Long departmentId);

}
