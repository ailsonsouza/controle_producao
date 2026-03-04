package com.ciamanutencao.services;

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

import com.ciamanutencao.production.dto.CategoryDTO;
import com.ciamanutencao.production.entities.Category;
import com.ciamanutencao.production.exceptions.ResourceNotFoundException;
import com.ciamanutencao.production.repositories.CategoryRepository;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.ciamanutencao.production.services.CategoryService;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTests {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category category;
    private Long existingId;
    private Long nonExistingId;

    @BeforeEach
    void setUp() {
        existingId = 1L;
        nonExistingId = 100L;
        category = new Category();
        category.setId(existingId);
        category.setName("Electronics Tests");
        category.setActive(true);

        new CategoryDTO(category);

    }

    @Test
    @DisplayName("createCategory deve retornar um CategoryDTO ao salvar com sucesso")
    void createCategoryShouldReturnCategoryDTOWhenDataIsValid() {

        CategoryDTO inpuDto = new CategoryDTO(null, "new category", true);
        Category savedCategory = new Category();
        savedCategory.setId(1L);
        savedCategory.setName("new category");
        savedCategory.setActive(true);

        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

        CategoryDTO result = categoryService.createCategory(inpuDto);

        assertNotNull(result);
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("new category");
        assertThat(result.active()).isTrue();

        verify(categoryRepository, times(1)).save(any(Category.class));

    }

    @Test
    @DisplayName("findAllCategories deve retornar uma lista de CategoryDTO")
    void findAllCategoriesShouldReturnListOfCategoryDTO() {
        List<Category> categoriesInput = new ArrayList<>();

        for (int x = 1; x <= 10; x++) {
            Category category = new Category();
            category.setName("category" + x);
            categoriesInput.add(category);
        }

        when(categoryRepository.findAll()).thenReturn(categoriesInput);

        List<CategoryDTO> result = categoryService.findAllCategories();

        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(10);
        assertThat(result.get(3).name()).isEqualTo("category4");
        assertThat(result.get(7).name()).isEqualTo("category8");

        verify(categoryRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve retornar um CategoryDTO quando o ID existe")
    void findCategoryByIdShouldReturnCategoryDTOWhenIdExists() {
        when(categoryRepository.findById(existingId)).thenReturn(Optional.of(category));
        CategoryDTO result = categoryService.findCategoryById(existingId);

        assertNotNull(result);
        assertThat(result.name()).isEqualTo("Electronics Tests");

        verify(categoryRepository, times(1)).findById(existingId);
    }

    @Test
    @DisplayName("Deve lançar exceção quando o ID não existe")
    void findCategoryByIdShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        when(categoryRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> {
            categoryService.findCategoryById(nonExistingId);
        }).isInstanceOf(ResourceNotFoundException.class);
        verify(categoryRepository, times(1)).findById(nonExistingId);
    }

    @Test
    @DisplayName("Deve retornar um CategoryDTO quando o Id existir")
    void updateCategoryShouldReturnCategoryDTOWhenIdExist() {
        Long id = 1L;

        Category entityNoBanco = new Category(id, "Nome antigo", true);
        CategoryDTO dadosNovosDto = new CategoryDTO(id, "Nome atualizado", false);
        Category entityAtualizada = new Category(id, "Nome atualizado", false);

        when(categoryRepository.getReferenceById(id)).thenReturn(entityNoBanco);
        when(categoryRepository.save(any(Category.class))).thenReturn(entityAtualizada);

        CategoryDTO result = categoryService.updateCategory(id, dadosNovosDto);

        assertNotNull(result);
        assertThat(result.id()).isEqualTo(id);
        assertThat(result.name()).isEqualTo("Nome atualizado");
        assertThat(result.active()).isFalse();

        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    @DisplayName("")
    void updateCategoryShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        Long id = 99L;
        CategoryDTO entity = new CategoryDTO(id, "failure test", true);

        when(categoryRepository.getReferenceById(id)).thenThrow(EntityNotFoundException.class);

        assertThatThrownBy(() -> {
            categoryService.updateCategory(id, entity);
        }).isInstanceOf(ResourceNotFoundException.class);

        verify(categoryRepository, never()).save(any());

    }

    @Test
    @DisplayName("deleteCategory deve deletar o registro quando o ID existe")
    void deleteCategoryShouldDeleteWhenIdExists() {
        Long id = 1L;

        when(categoryRepository.existsById(id)).thenReturn(true);
        categoryService.deleteCategory(id);

        verify(categoryRepository, times(1)).existsById(id);
        verify(categoryRepository, times(1)).deleteById(id);
    }

    @Test
    @DisplayName("deleteCategory deve lançar ResourceNotFoundException quando o ID não existe")
    void deleteCategoryShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        Long id = 99L;
        when(categoryRepository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> {
            categoryService.deleteCategory(id);
        }).isInstanceOf(ResourceNotFoundException.class);

        verify(categoryRepository, never()).deleteById(id);
    }

}
