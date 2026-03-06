package com.ciamanutencao.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ciamanutencao.production.dto.DepartmentDTO;
import com.ciamanutencao.production.entities.Department;
import com.ciamanutencao.production.entities.Technical;
import com.ciamanutencao.production.exceptions.ResourceNotFoundException;
import com.ciamanutencao.production.repositories.DepartmentRepository;
import com.ciamanutencao.production.services.DepartmentService;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
public class DepartmentServiceTests {

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private DepartmentService departmentService;

    private Department department;
    private Long existingId;
    private Long nonExistingId;

    @BeforeEach
    void setUp() {
        existingId = 1L;
        nonExistingId = 100L;
        department = new Department();
        department.setId(existingId);
        department.setName("GRCP TEST");
        department.setActive(true);

        Technical technicalOne = new Technical(1L, "Technical test 1", true);
        Technical technicalTwo = new Technical(2L, "Technical test 2", true);

        department.addTechnical(technicalOne);
        department.addTechnical(technicalTwo);

        new DepartmentDTO(department);

    }

    @Test
    @DisplayName("createDepartment deve retornar um DepartmentDTO ao salvar com sucesso")
    void createDepartmentShouldReturnDepartmentDTOWhenDataIsValid() {

        DepartmentDTO inpuDto = new DepartmentDTO(existingId, "GRCP TEST", true);
        Department savedDepartment = new Department(existingId, "GRCP TEST", true);

        when(departmentRepository.save(any(Department.class))).thenReturn(savedDepartment);

        DepartmentDTO result = departmentService.createDepartment(inpuDto);

        assertNotNull(result);
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("GRCP TEST");
        assertThat(result.active()).isTrue();

        verify(departmentRepository, times(1)).save(any(Department.class));

    }

    @Test
    @DisplayName("findAllDepartments deve retornar uma lista de DepartmentDTO")
    void findAllDepartmentsShouldReturnListOfDepartmentDTO() {
        List<Department> departmentsInput = new ArrayList<>();

        for (int x = 1; x <= 10; x++) {
            Department department = new Department();
            department.setName("department" + x);
            departmentsInput.add(department);
        }

        when(departmentRepository.findAll()).thenReturn(departmentsInput);

        List<DepartmentDTO> result = departmentService.findAllDepartments();

        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(10);
        assertThat(result.get(3).name()).isEqualTo("department4");
        assertThat(result.get(7).name()).isEqualTo("department8");

        verify(departmentRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve retornar um DepartementDTO quando o ID existe")
    void findDepartmentByIdShouldReturnDepartmentDTOWhenIdExists() {
        when(departmentRepository.findById(existingId)).thenReturn(Optional.of(department));
        DepartmentDTO result = departmentService.findDepartmentById(existingId);

        assertNotNull(result);
        assertThat(result.name()).isEqualTo("GRCP TEST");

        verify(departmentRepository, times(1)).findById(existingId);
    }

    @Test
    @DisplayName("Deve lançar exceção quando o ID não existe")
    void findDepartmentByIdShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        when(departmentRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> {
            departmentService.findDepartmentById(nonExistingId);
        }).isInstanceOf(ResourceNotFoundException.class);

        verify(departmentRepository, times(1)).findById(nonExistingId);
    }

    @Test
    @DisplayName("Deve retornar um DepartmentDTO quando o Id existir")
    void updateDepartmentShouldReturnDepartmentDTOWhenIdExist() {
        Long id = 1L;

        Department entityNoBanco = new Department(id, "Nome antigo", true);
        DepartmentDTO dadosNovosDto = new DepartmentDTO(id, "Nome atualizado", false);
        Department entityAtualizada = new Department(id, "Nome atualizado", false);

        when(departmentRepository.getReferenceById(id)).thenReturn(entityNoBanco);
        when(departmentRepository.save(any(Department.class))).thenReturn(entityAtualizada);

        DepartmentDTO result = departmentService.updateDepartment(id, dadosNovosDto);

        assertNotNull(result);
        assertThat(result.id()).isEqualTo(id);
        assertThat(result.name()).isEqualTo("Nome atualizado");
        assertThat(result.active()).isFalse();

        verify(departmentRepository, times(1)).save(any(Department.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando o ID não existe")
    void updateDepartmentShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        Long id = 99L;
        DepartmentDTO entity = new DepartmentDTO(id, "failure test", true);

        when(departmentRepository.getReferenceById(id)).thenThrow(EntityNotFoundException.class);

        assertThatThrownBy(() -> {
            departmentService.updateDepartment(id, entity);
        }).isInstanceOf(ResourceNotFoundException.class);

        verify(departmentRepository, never()).save(any());

    }

    @Test
    @DisplayName("deleteDepartment deve deletar o registro quando o ID existe")
    void deleteDepartmentShouldDeleteWhenIdExists() {
        Long id = 1L;

        when(departmentRepository.existsById(id)).thenReturn(true);
        departmentService.deleteDepartment(id);

        verify(departmentRepository, times(1)).existsById(id);
        verify(departmentRepository, times(1)).deleteById(id);
    }

    @Test
    @DisplayName("deleteDepartment deve lançar ResourceNotFoundException quando o ID não existe")
    void deleteDepartmentShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        Long id = 99L;
        when(departmentRepository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> {
            departmentService.deleteDepartment(id);
        }).isInstanceOf(ResourceNotFoundException.class);

        verify(departmentRepository, never()).deleteById(id);
    }

}
