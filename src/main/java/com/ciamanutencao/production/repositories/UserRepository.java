package com.ciamanutencao.production.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;

import com.ciamanutencao.production.entities.User;

public interface UserRepository extends JpaRepository<User, Long> {

    UserDetails findByLogin(String login);

    List<User> findByDepartmentId(Long departmentId);

}
