package com.ciamanutencao.production.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ciamanutencao.production.dto.CategoryDTO;
import com.ciamanutencao.production.entities.Category;
import com.ciamanutencao.production.exceptions.ResourceNotFoundException;
import com.ciamanutencao.production.repositories.CategoryRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public CategoryDTO createCategory(CategoryDTO categoryDto) {
        Category category = new Category();
        category.setName(categoryDto.name());
        category.setActive(categoryDto.active());
        category = categoryRepository.save(category);
        return new CategoryDTO(category);

    }

    @Transactional(readOnly = true)
    public List<CategoryDTO> findAllCategories() {
        List<Category> list = categoryRepository.findAll();
        return list.stream().map(CategoryDTO::new).toList();
    }

    @Transactional(readOnly = true)
    public CategoryDTO findCategoryById(Long id) {
        Category category = categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(id));
        return new CategoryDTO(category);
    }

    // @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public CategoryDTO updateCategory(Long id, CategoryDTO updatedCategory) {
        try {
            Category entity = categoryRepository.getReferenceById(id);
            entity.setName(updatedCategory.name());
            entity.setActive(updatedCategory.active());

            entity = categoryRepository.save(entity);

            return new CategoryDTO(entity);
        } catch (EntityNotFoundException e) {
            throw new ResourceNotFoundException(id);
        }
    }

    // @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException(id);
        }
        categoryRepository.deleteById(id);
    }

}
