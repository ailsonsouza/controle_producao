package com.ciamanutencao.production.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ciamanutencao.production.entities.User;

public interface UserRepository extends JpaRepository<User, Long> {

}
