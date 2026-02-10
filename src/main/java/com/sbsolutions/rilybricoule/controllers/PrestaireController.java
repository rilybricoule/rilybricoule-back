package com.sbsolutions.rilybricoule.controllers;

import com.sbsolutions.rilybricoule.dto.PrestaireDTO;
import com.sbsolutions.rilybricoule.entity.Prestataire;
import com.sbsolutions.rilybricoule.repository.PrestaireRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/prestataires")
@RequiredArgsConstructor
public class PrestaireController {
    
    private final PrestaireRepository prestaireRepository;
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PrestaireDTO> createPrestataire(@RequestBody PrestaireDTO request) {
        Prestataire prestataire = Prestataire.builder()
            .name(request.getName())
            .description(request.getDescription())
            .phone(request.getPhone())
            .email(request.getEmail())
            .address(request.getAddress())
            .build();
        
        Prestataire saved = prestaireRepository.save(prestataire);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(saved));
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PRESTATAIRE', 'ADMIN')")
    public ResponseEntity<?> getPrestataire(@PathVariable Long id) {
        Optional<Prestataire> prestataire = prestaireRepository.findById(id);
        if (prestataire.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Prestataire not found");
        }
        return ResponseEntity.ok(toDTO(prestataire.get()));
    }
    
    @GetMapping
    @PreAuthorize("hasAnyRole('PRESTATAIRE', 'ADMIN')")
    public ResponseEntity<List<PrestaireDTO>> getAllPrestataires() {
        List<PrestaireDTO> prestataires = prestaireRepository.findAll()
            .stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(prestataires);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updatePrestataire(@PathVariable Long id, @RequestBody PrestaireDTO request) {
        Optional<Prestataire> prestaireOpt = prestaireRepository.findById(id);
        if (prestaireOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Prestataire not found");
        }
        
        Prestataire prestataire = prestaireOpt.get();
        prestataire.setName(request.getName());
        prestataire.setDescription(request.getDescription());
        prestataire.setPhone(request.getPhone());
        prestataire.setEmail(request.getEmail());
        prestataire.setAddress(request.getAddress());
        
        Prestataire updated = prestaireRepository.save(prestataire);
        return ResponseEntity.ok(toDTO(updated));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deletePrestataire(@PathVariable Long id) {
        if (!prestaireRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Prestataire not found");
        }
        prestaireRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    
    private PrestaireDTO toDTO(Prestataire prestataire) {
        return PrestaireDTO.builder()
            .id(prestataire.getId())
            .name(prestataire.getName())
            .description(prestataire.getDescription())
            .phone(prestataire.getPhone())
            .email(prestataire.getEmail())
            .address(prestataire.getAddress())
            .build();
    }
}
