package com.carevoice.controller;

import com.carevoice.dto.MedicationDtos;
import com.carevoice.security.SecurityUtils;
import com.carevoice.service.MedicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/medications")
public class MedicationController {

    private final MedicationService medicationService;
    private final SecurityUtils securityUtils;

    public MedicationController(MedicationService medicationService, SecurityUtils securityUtils) {
        this.medicationService = medicationService;
        this.securityUtils = securityUtils;
    }

    @PostMapping
    public ResponseEntity<MedicationDtos.MedicationResponse> createMedication(
            @Valid @RequestBody MedicationDtos.MedicationRequest request) {
        UUID caregiverId = securityUtils.getCurrentUserId();
        MedicationDtos.MedicationResponse response = medicationService.createMedication(caregiverId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<MedicationDtos.MedicationResponse>> getAllMedications() {
        UUID caregiverId = securityUtils.getCurrentUserId();
        List<MedicationDtos.MedicationResponse> list = medicationService.getAllMedicationsByCaregiver(caregiverId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<MedicationDtos.MedicationResponse>> getMedicationsByPatient(@PathVariable UUID patientId) {
        UUID caregiverId = securityUtils.getCurrentUserId();
        List<MedicationDtos.MedicationResponse> list = medicationService.getMedicationsByPatient(patientId, caregiverId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<MedicationDtos.MedicationResponse>> getLowStockMedications() {
        UUID caregiverId = securityUtils.getCurrentUserId();
        List<MedicationDtos.MedicationResponse> list = medicationService.getLowStockMedications(caregiverId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicationDtos.MedicationResponse> getMedicationById(@PathVariable UUID id) {
        UUID caregiverId = securityUtils.getCurrentUserId();
        MedicationDtos.MedicationResponse response = medicationService.getMedicationById(id, caregiverId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MedicationDtos.MedicationResponse> updateMedication(
            @PathVariable UUID id,
            @Valid @RequestBody MedicationDtos.MedicationRequest request) {
        UUID caregiverId = securityUtils.getCurrentUserId();
        MedicationDtos.MedicationResponse updated = medicationService.updateMedication(id, caregiverId, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMedication(@PathVariable UUID id) {
        UUID caregiverId = securityUtils.getCurrentUserId();
        medicationService.deleteMedication(id, caregiverId);
        return ResponseEntity.noContent().build();
    }
}
