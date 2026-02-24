package com.ciamanutencao.production.controllers;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.ciamanutencao.production.dto.CategoryDTO;
import com.ciamanutencao.production.services.CategoryService;

import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<CategoryDTO> createCategory(@Valid @RequestBody CategoryDTO dto) {
        dto = categoryService.createCategory(dto);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(dto.id())
                .toUri();
        return ResponseEntity.created(uri).body(dto);
    }

    @GetMapping
    public ResponseEntity<List<CategoryDTO>> findAllCategories() {
        List<CategoryDTO> list = categoryService.findAllCategories();
        return ResponseEntity.ok().body(list);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<CategoryDTO> findCategoryById(@PathVariable Long id) {
        CategoryDTO dto = categoryService.findCategoryById(id);
        return ResponseEntity.ok().body(dto);
    }

    @PutMapping(value = "/{id}")
    public ResponseEntity<CategoryDTO> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryDTO dto) {
        dto = categoryService.updateCategory(id, dto);
        return ResponseEntity.ok().body(dto);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id){
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}