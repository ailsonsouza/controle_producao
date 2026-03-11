package com.ciamanutencao.services;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ciamanutencao.production.dto.UserCreateDTO;
import com.ciamanutencao.production.dto.UserDTO;
import com.ciamanutencao.production.entities.Department;
import com.ciamanutencao.production.entities.User;
import com.ciamanutencao.production.enums.UserRole;
import com.ciamanutencao.production.exceptions.ResourceNotFoundException;
import com.ciamanutencao.production.repositories.DepartmentRepository;
import com.ciamanutencao.production.repositories.UserRepository;
import com.ciamanutencao.production.services.UserService;
import com.ciamanutencao.production.services.utils.SecurityUtils;

@ExtendWith(MockitoExtension.class)
public class UserServiceTests {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Mock
    private SecurityUtils securityUtils;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private DepartmentRepository departmentRepository;

    private User user;
    private Long existingId;
    private Long nonExistingId;
    private Department dept;

    @BeforeEach
    void setUp() {
        existingId = 1L;
        nonExistingId = 99L;

        dept = new Department(1L, "Department test 1", true);

        user = new User();
        user.setId(existingId);
        user.setName("USER TEST");
        user.setLogin("user.test");
        user.setPassword("hashed_password");
        user.setActive(true);
        user.setUserRole(UserRole.ROLE_ADMIN);
        user.setDepartment(dept);

    }

    @Test
    @DisplayName("createUser deve retornar um UserDTO ao salvar com sucesso")
    void createUserShouldCallSaveWhenAccessIsAllowed() {

        UserCreateDTO userCreateDTO = new UserCreateDTO("user.test", "hashed_password", "USER TEST", dept,
                UserRole.ROLE_ADMIN, true);

        doNothing().when(securityUtils).checkUserManagementAccess(any(Department.class), any(UserRole.class));
        when(departmentRepository.findById(anyLong())).thenReturn(Optional.of(dept));
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_password_encrypted");
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDTO userDTO = userService.createUser(userCreateDTO);

        assertNotNull(userDTO);

        verify(passwordEncoder, times(1)).encode("hashed_password");
        verify(userRepository, times(1)).save(any(User.class));

    }

    @Test
    @DisplayName("createUser deve lançar AccessDeniedException quando a segurança barrar o acesso")
    void createUserShouldThrowAccessDeniedExceptionWhenSecurityUtilsBlocks() {
        UserCreateDTO dto = new UserCreateDTO("login", "senha12345", "Nome", dept, UserRole.ROLE_ADMIN, true);

        doThrow(new AccessDeniedException("Acesso negado"))
                .when(securityUtils).checkUserManagementAccess(any(Department.class), any(UserRole.class));

        assertThrows(AccessDeniedException.class, () -> userService.createUser(dto));

        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("findAllUsers deve retornar todos os usuários quando o papel for ADMIN")
    void findAllUsersShouldReturnAllUsersWhenRoleAdmin() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        try (MockedStatic<SecurityContextHolder> mockedSecurity = mockStatic(SecurityContextHolder.class)) {
            mockSecurityContext(mockedSecurity, user); // <-- A mágica acontece aqui

            List<UserDTO> result = userService.findAllUsers();

            assertNotNull(result);
            verify(userRepository, times(1)).findAll();
            verify(userRepository, never()).findByDepartmentId(anyLong());
        }
    }

    @Test
    @DisplayName("findAllUsers deve filtrar por departamento quando o papel for CHIEF")
    void findAllUsersShouldOnlyReturnUsersFromDepartmentOfChief() {
        user.setUserRole(UserRole.ROLE_CHIEF);
        when(userRepository.findByDepartmentId(anyLong())).thenReturn(List.of(user));

        try (MockedStatic<SecurityContextHolder> mockedSecurity = mockStatic(SecurityContextHolder.class)) {
            mockSecurityContext(mockedSecurity, user);

            List<UserDTO> result = userService.findAllUsers();

            assertNotNull(result);
            verify(userRepository, times(1)).findByDepartmentId(anyLong());
            verify(userRepository, never()).findAll();
        }
    }

    @Test
    @DisplayName("findAllUsers deve lançar AccessDeniedException para usuários comuns")
    void findAllUsersShouldThrowAnAccessDeniedExceptionForUserRole() {
        user.setUserRole(UserRole.ROLE_USER);

        try (MockedStatic<SecurityContextHolder> mockedSecurity = mockStatic(SecurityContextHolder.class)) {
            mockSecurityContext(mockedSecurity, user);

            assertThatThrownBy(() -> userService.findAllUsers())
                    .isInstanceOf(AccessDeniedException.class);

            verify(userRepository, never()).findAll();
        }
    }

    @Test
    @DisplayName("findUserById deve retornar UserDTO quando o ID existir")
    void findUserByIdShouldReturnUserDTOWhenIdExist() {
        when(userRepository.findById(existingId)).thenReturn(Optional.of(user));

        UserDTO result = userService.findUserById(existingId);

        assertNotNull(result);
        verify(userRepository, times(1)).findById(existingId);
    }

    @Test
    @DisplayName("findUserById deve lançar ResourceNotFoundException quando o ID não existir")
    void findUserByIdShouldThrowAnResourceNotFoundExceptionWhenIdDoesNotExist() {
        when(userRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> {
            userService.findUserById(nonExistingId);
        }).isInstanceOf(ResourceNotFoundException.class);

        verify(userRepository, times(1)).findById(anyLong());
    }

    private void mockSecurityContext(MockedStatic<SecurityContextHolder> mockedSecurity, User userToMock) {
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        mockedSecurity.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userToMock);
    }
}
