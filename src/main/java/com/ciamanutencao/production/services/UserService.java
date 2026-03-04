package com.ciamanutencao.production.services;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ciamanutencao.production.dto.PasswordUpdateDTO;
import com.ciamanutencao.production.dto.UserCreateDTO;
import com.ciamanutencao.production.dto.UserDTO;
import com.ciamanutencao.production.entities.Department;
import com.ciamanutencao.production.entities.User;
import com.ciamanutencao.production.enums.UserRole;
import com.ciamanutencao.production.exceptions.PasswordMismatchException;
import com.ciamanutencao.production.exceptions.ResourceNotFoundException;
import com.ciamanutencao.production.repositories.DepartmentRepository;
import com.ciamanutencao.production.repositories.UserRepository;
import com.ciamanutencao.production.services.utils.SecurityUtils;

import jakarta.persistence.EntityNotFoundException;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DepartmentRepository departmentRepository;
    private final SecurityUtils securityUtils;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
            DepartmentRepository departmentRepository, SecurityUtils securityUtils) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.departmentRepository = departmentRepository;
        this.securityUtils = securityUtils;
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CHIEF')")
    @Transactional
    public UserDTO createUser(UserCreateDTO dto) {

        securityUtils.checkUserManagementAccess(dto.department(), dto.userRole());

        User user = new User();

        user.setLogin(dto.login());
        user.setName(dto.name());

        Department department = departmentRepository.findById(dto.department().getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Departamento não encontrado com ID: " + dto.department().getId()));

        user.setDepartment(department);
        user.setUserRole(dto.userRole());
        user.setActive(dto.active() != null ? dto.active() : true);

        user.setPassword(passwordEncoder.encode(dto.password()));

        user = userRepository.save(user);

        return new UserDTO(user);
    }

    @Transactional(readOnly = true)
    public List<UserDTO> findAllUsers() {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<User> list;

        if (currentUser.getUserRole() == UserRole.ROLE_ADMIN) {
            list = userRepository.findAll();
        } else if (currentUser.getUserRole() == UserRole.ROLE_CHIEF) {
            list = userRepository.findByDepartmentId(currentUser.getDepartment().getId());
        } else {
            throw new AccessDeniedException("Você não tem permissão para listar usuários.");
        }

        return list.stream().map(UserDTO::new).toList();
    }

    @Transactional(readOnly = true)
    public UserDTO findUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(id));
        return new UserDTO(user);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CHIEF')")
    @Transactional
    public UserDTO updateUser(Long id, UserDTO updatedUserDto) {
        try {
            User entity = userRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException(id));

            securityUtils.checkUserManagementAccess(entity.getDepartment(), entity.getUserRole());

            securityUtils.checkUserManagementAccess(updatedUserDto.department(), updatedUserDto.userRole());

            entity.setName(updatedUserDto.name());
            entity.setActive(updatedUserDto.active());
            entity.setDepartment(updatedUserDto.department());
            entity.setUserRole(updatedUserDto.userRole());

            entity = userRepository.save(entity);
            return new UserDTO(entity);

        } catch (EntityNotFoundException e) {
            throw new ResourceNotFoundException(id);
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CHIEF')")
    @Transactional
    public void deleteUser(Long id) {
        User entity = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));

        securityUtils.checkDepartmentAccess(entity.getDepartment());

        userRepository.delete(entity);
    }

    @Transactional
    public void updatePassword(Long id, PasswordUpdateDTO dto) {
        if (!dto.newPassword().equals(dto.confirmNewPassword())) {
            throw new PasswordMismatchException("A nova senha e a confirmação não conferem.");
        }

        User entity = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));

        if (!entity.getLogin().equals(dto.login())) {
            throw new PasswordMismatchException("Login informado não confere.");
        }

        if (!passwordEncoder.matches(dto.oldPassword(), entity.getPassword())) {
            throw new PasswordMismatchException("Senha atual incorreta.");
        }

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!user.equals(entity)) {
            throw new AccessDeniedException("Apenas o próprio usuário pode alterar sua senha.");
        }

        entity.setPassword(passwordEncoder.encode(dto.newPassword()));
    }

}
