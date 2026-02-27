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

import com.ciamanutencao.production.dto.TechnicalDTO;
import com.ciamanutencao.production.services.TechnicalService;

import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/technicians")
public class TechnicalController {

    private final TechnicalService technicalService;

    public TechnicalController(TechnicalService technicalService) {
        this.technicalService = technicalService;
    }

    @PostMapping
    public ResponseEntity<TechnicalDTO> createTechnical(@Valid @RequestBody TechnicalDTO dto) {
        TechnicalDTO technicalDto = technicalService.createTechnical(dto);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(technicalDto.id())
                .toUri();
        return ResponseEntity.created(uri).body(technicalDto);
    }

    @GetMapping
    public ResponseEntity<List<TechnicalDTO>> findAllTechnicians() {
        List<TechnicalDTO> list = technicalService.findAllTechnicians();
        return ResponseEntity.ok().body(list);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<TechnicalDTO> findTechnicalById(@PathVariable Long id) {
        TechnicalDTO dto = technicalService.findTechnicalById(id);
        return ResponseEntity.ok().body(dto);
    }

    @PutMapping(value = "/{id}")
    public ResponseEntity<TechnicalDTO> updateTechnical(@PathVariable Long id,
            @Valid @RequestBody TechnicalDTO dto) {
        dto = technicalService.updateTechnical(id, dto);
        return ResponseEntity.ok().body(dto);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> deleteTechnical(@PathVariable Long id) {
        technicalService.deleteTechnical(id);
        return ResponseEntity.noContent().build();
    }

}