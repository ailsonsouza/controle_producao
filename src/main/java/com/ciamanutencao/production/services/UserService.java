package com.ciamanutencao.production.services;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ciamanutencao.production.dto.PasswordUpdateDTO;
import com.ciamanutencao.production.dto.UserCreateDTO;
import com.ciamanutencao.production.dto.UserDTO;
import com.ciamanutencao.production.entities.User;
import com.ciamanutencao.production.exceptions.PasswordMismatchException;
import com.ciamanutencao.production.exceptions.ResourceNotFoundException;
import com.ciamanutencao.production.repositories.UserRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // O Spring injeta automaticamente aqui
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserDTO createUser(UserCreateDTO dto) {
        User user = new User();

        user.setLogin(dto.login());
        user.setName(dto.name());
        user.setDepartment(dto.department());
        user.setUserRole(dto.userRole());
        user.setActive(dto.active() != null ? dto.active() : true);

        user.setPassword(passwordEncoder.encode(dto.password()));

        user = userRepository.save(user);

        return new UserDTO(user);
    }

    @Transactional(readOnly = true)
    public List<UserDTO> findAllUsers() {
        List<User> list = userRepository.findAll();
        return list.stream().map(UserDTO::new).toList();
    }

    @Transactional(readOnly = true)
    public UserDTO findUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(id));
        return new UserDTO(user);
    }

    @Transactional
    public UserDTO updateUser(Long id, UserDTO updatedUser) {
        try {
            User entity = userRepository.getReferenceById(id);
            entity.setName(updatedUser.name());
            entity.setActive(updatedUser.active());
            entity.setDepartment(updatedUser.department());
            entity.setUserRole(updatedUser.userRole());

            return new UserDTO(entity);
        } catch (EntityNotFoundException e) {
            throw new ResourceNotFoundException(id);
        }
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException(id);
        }
        userRepository.deleteById(id);
    }

    @Transactional
    public void updatePassword(Long id, PasswordUpdateDTO dto) {
        // 1. Validar se a nova senha e a confirmação são iguais (Fail-fast)
        if (!dto.newPassword().equals(dto.confirmNewPassword())) {
            throw new PasswordMismatchException("A nova senha e a confirmação não conferem.");
        }

        User entity = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));

        // 2. Validar o login
        if (!entity.getLogin().equals(dto.login())) {
            throw new PasswordMismatchException("Login informado não confere.");
        }

        // 3. Validar a senha atual
        if (!passwordEncoder.matches(dto.oldPassword(), entity.getPassword())) {
            throw new PasswordMismatchException("Senha atual incorreta.");
        }

        // 4. Salvar nova senha criptografada
        entity.setPassword(passwordEncoder.encode(dto.newPassword()));
    }

}
