package com.ciamanutencao.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anySet;
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

import com.ciamanutencao.production.dto.TechnicalDTO;
import com.ciamanutencao.production.entities.Department;
import com.ciamanutencao.production.entities.Technical;
import com.ciamanutencao.production.entities.User;
import com.ciamanutencao.production.enums.UserRole;
import com.ciamanutencao.production.exceptions.ResourceNotFoundException;
import com.ciamanutencao.production.repositories.TechnicalRepository;
import com.ciamanutencao.production.services.TechnicalService;
import com.ciamanutencao.production.services.utils.SecurityUtils;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
public class TechnicalServiceTests {

    @Mock
    private TechnicalRepository technicalRepository;

    @InjectMocks
    private TechnicalService technicalService;

    @Mock
    private SecurityUtils securityUtils;

    private Technical technical;
    private Long existingId;
    private Long nonExistingId;

    @BeforeEach
    void setUp() {
        existingId = 1L;
        nonExistingId = 100L;
        technical = new Technical();
        technical.setId(existingId);
        technical.setName("TECHNICAL TEST");
        technical.setActive(true);

        Department departmentOne = new Department(1L, "Department test 1", true);
        Department departmentTwo = new Department(2L, "Department test 2", true);

        technical.addDepartments(departmentOne);
        technical.addDepartments(departmentTwo);

        new TechnicalDTO(technical);

    }

    @Test
    @DisplayName("createTechnical deve retornar um TechnicalDTO ao salvar com sucesso")
    void createTechnicalShouldCallSaveWhenAccessIsAllowed() {

        TechnicalDTO inpuDto = new TechnicalDTO(existingId, technical.getDepartments(), "TECHNICAL TEST", true);
        Technical savedTechnical = new Technical(existingId, "TECHNICAL TEST", true);

        doNothing().when(securityUtils).checkDepartmentAccess(anySet());

        when(technicalRepository.save(any(Technical.class))).thenReturn(savedTechnical);

        TechnicalDTO result = technicalService.createTechnical(inpuDto);

        assertNotNull(result);
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("TECHNICAL TEST");
        assertThat(result.active()).isTrue();

        verify(securityUtils, times(1)).checkDepartmentAccess(anySet());
        verify(technicalRepository, times(1)).save(any(Technical.class));

    }

    @Test
    void createTechnicalShouldThrowExceptionWhenAccessIsDenied() {

        doThrow(new AccessDeniedException("Access denied"))
                .when(securityUtils).checkDepartmentAccess(anySet());

        assertThrows(AccessDeniedException.class, () -> {
            technicalService.createTechnical(new TechnicalDTO(technical));
        });

        verify(technicalRepository, never()).save(any());
    }

    @Test
    void findAllTechniciansShouldReturnAllWhenUserIsAdmin() {
        User admin = new User();
        admin.setUserRole(UserRole.ROLE_ADMIN);

        try (MockedStatic<SecurityContextHolder> mockedSecurity = mockStatic(SecurityContextHolder.class)) {
            SecurityContext mockContext = mock(SecurityContext.class);
            Authentication mockAuth = mock(Authentication.class);

            mockedSecurity.when(SecurityContextHolder::getContext).thenReturn(mockContext);
            when(mockContext.getAuthentication()).thenReturn(mockAuth);
            when(mockAuth.getPrincipal()).thenReturn(admin);

            when(technicalRepository.findAll()).thenReturn(List.of(new Technical(), new Technical()));

            List<TechnicalDTO> result = technicalService.findAllTechnicians();

            assertEquals(2, result.size());
            verify(technicalRepository, times(1)).findAll();
            verify(technicalRepository, never()).findByDepartments_Id(anyLong());
        }
    }

    @Test
    void findAllTechniciansShouldFilterByDeptWhenUserIsChief() {
        Long deptId = 10L;
        Department dept = new Department();
        dept.setId(deptId);

        User chief = new User();
        chief.setUserRole(UserRole.ROLE_CHIEF);
        chief.setDepartment(dept);

        try (MockedStatic<SecurityContextHolder> mockedSecurity = mockStatic(SecurityContextHolder.class)) {
            SecurityContext mockContext = mock(SecurityContext.class);
            Authentication mockAuth = mock(Authentication.class);

            mockedSecurity.when(SecurityContextHolder::getContext).thenReturn(mockContext);
            when(mockContext.getAuthentication()).thenReturn(mockAuth);
            when(mockAuth.getPrincipal()).thenReturn(chief);

            when(technicalRepository.findByDepartments_Id(deptId)).thenReturn(List.of(new Technical()));

            List<TechnicalDTO> result = technicalService.findAllTechnicians();

            assertEquals(1, result.size());
            verify(technicalRepository, times(1)).findByDepartments_Id(deptId);
            verify(technicalRepository, never()).findAll();
        }
    }

    @Test
    @DisplayName("Deve retornar um TechnicalDTO quando o ID existe")
    void findTechnicalByIdShouldReturnTechnicalDTOWhenIdExists() {
        when(technicalRepository.findById(existingId)).thenReturn(Optional.of(technical));
        TechnicalDTO result = technicalService.findTechnicalById(existingId);

        assertNotNull(result);
        assertThat(result.name()).isEqualTo("TECHNICAL TEST");

        verify(technicalRepository, times(1)).findById(existingId);
    }

    @Test
    @DisplayName("Deve lançar exceção quando o ID não existe")
    void findTechnicalByIdShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        when(technicalRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> {
            technicalService.findTechnicalById(nonExistingId);
        }).isInstanceOf(ResourceNotFoundException.class);

        verify(technicalRepository, times(1)).findById(nonExistingId);
    }

    @Test
    @DisplayName("Deve retornar um TechnicalDTO quando o Id existir")
    void updateTechnicalShouldReturnTechnicalDTOWhenIdExist() {
        Long id = 1L;

        doNothing().when(securityUtils).checkDepartmentAccess(anySet());

        Technical entityNoBanco = new Technical(id, "Nome antigo", true);
        TechnicalDTO dadosNovosDto = new TechnicalDTO(id, technical.getDepartments(), "Nome atualizado", false);
        Technical entityAtualizada = new Technical(id, "Nome atualizado", false);

        when(technicalRepository.getReferenceById(id)).thenReturn(entityNoBanco);
        when(technicalRepository.save(any(Technical.class))).thenReturn(entityAtualizada);

        TechnicalDTO result = technicalService.updateTechnical(id, dadosNovosDto);

        assertNotNull(result);
        assertThat(result.id()).isEqualTo(id);
        assertThat(result.name()).isEqualTo("Nome atualizado");
        assertThat(result.active()).isFalse();

        verify(technicalRepository, times(1)).save(any(Technical.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando o ID não existe")
    void updateTechnicalShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        Long id = 99L;

        doNothing().when(securityUtils).checkDepartmentAccess(anySet());

        TechnicalDTO entity = new TechnicalDTO(id, technical.getDepartments(), "failure test", true);

        when(technicalRepository.getReferenceById(id)).thenThrow(EntityNotFoundException.class);

        assertThatThrownBy(() -> {
            technicalService.updateTechnical(id, entity);
        }).isInstanceOf(ResourceNotFoundException.class);

        verify(technicalRepository, never()).save(any());

    }

    @Test
    @DisplayName("Deve deletar o técnico quando o usuário tem acesso ao departamento")
    void deleteTechnical_ShouldDelete_WhenAccessIsAllowed() {

        Long id = 1L;
        Technical technical = new Technical();
        Department departmentOne = new Department(1L, "Department test 1", true);
        Department departmentTwo = new Department(1L, "Department test 2", true);

        technical.addDepartments(departmentOne);
        technical.addDepartments(departmentTwo);

        when(technicalRepository.existsById(id)).thenReturn(true);
        when(technicalRepository.getReferenceById(id)).thenReturn(technical);

        doNothing().when(securityUtils).checkDepartmentAccess(anySet());

        assertDoesNotThrow(() -> technicalService.deleteTechnical(id));

        verify(technicalRepository, times(1)).deleteById(id);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando o ID não existe")
    void deleteTechnical_ShouldThrowNotFound_WhenIdDoesNotExist() {

        Long id = 99L;
        when(technicalRepository.existsById(id)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> technicalService.deleteTechnical(id));

        verify(securityUtils, never()).checkDepartmentAccess(anySet());
        verify(technicalRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Deve impedir o delete quando o SecurityUtils lançar AccessDenied")
    void deleteTechnical_ShouldThrowAccessDenied_WhenUserHasNoAccess() {

        Long id = 1L;
        Technical technical = new Technical();

        when(technicalRepository.existsById(id)).thenReturn(true);
        when(technicalRepository.getReferenceById(id)).thenReturn(technical);

        doThrow(new AccessDeniedException("Acesso negado"))
                .when(securityUtils).checkDepartmentAccess(anySet());

        assertThrows(AccessDeniedException.class, () -> technicalService.deleteTechnical(id));

        verify(technicalRepository, never()).deleteById(anyLong());
    }

}
