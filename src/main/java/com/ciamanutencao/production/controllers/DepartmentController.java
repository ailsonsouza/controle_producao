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

import com.ciamanutencao.production.dto.DepartmentDTO;
import com.ciamanutencao.production.services.DepartmentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @PostMapping
    public ResponseEntity<DepartmentDTO> createDepartment(@Valid @RequestBody DepartmentDTO dto) {
        dto = departmentService.createDepartment(dto);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(dto.id())
                .toUri();
        return ResponseEntity.created(uri).body(dto);
    }

    @GetMapping
    public ResponseEntity<List<DepartmentDTO>> findAllCategories() {
        List<DepartmentDTO> list = departmentService.findAllCategories();
        return ResponseEntity.ok().body(list);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<DepartmentDTO> findDepartmentById(@PathVariable Long id) {
        DepartmentDTO dto = departmentService.findDepartmentById(id);
        return ResponseEntity.ok().body(dto);
    }

    @PutMapping(value = "/{id}")
    public ResponseEntity<DepartmentDTO> updateDepartment(@PathVariable Long id,
            @Valid @RequestBody DepartmentDTO dto) {
        dto = departmentService.updateDepartment(id, dto);
        return ResponseEntity.ok().body(dto);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }
}