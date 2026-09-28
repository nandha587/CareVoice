package com.carevoice.controller;

import com.carevoice.dto.PatientDtos;
import com.carevoice.security.SecurityUtils;
import com.carevoice.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/patients")
public class PatientController {

    private final PatientService patientService;
    private final SecurityUtils securityUtils;

    public PatientController(PatientService patientService, SecurityUtils securityUtils) {
        this.patientService = patientService;
        this.securityUtils = securityUtils;
    }

    @PostMapping
    public ResponseEntity<PatientDtos.PatientResponse> createPatient(@Valid @RequestBody PatientDtos.PatientRequest request) {
        UUID caregiverId = securityUtils.getCurrentUserId();
        PatientDtos.PatientResponse response = patientService.createPatient(caregiverId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<PatientDtos.PatientResponse>> getPatients() {
        UUID caregiverId = securityUtils.getCurrentUserId();
        List<PatientDtos.PatientResponse> patients = patientService.getPatients(caregiverId);
        return ResponseEntity.ok(patients);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PatientDtos.PatientResponse> getPatientById(@PathVariable UUID id) {
        UUID caregiverId = securityUtils.getCurrentUserId();
        PatientDtos.PatientResponse patient = patientService.getPatientById(id, caregiverId);
        return ResponseEntity.ok(patient);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PatientDtos.PatientResponse> updatePatient(
            @PathVariable UUID id,
            @Valid @RequestBody PatientDtos.PatientRequest request) {
        UUID caregiverId = securityUtils.getCurrentUserId();
        PatientDtos.PatientResponse updated = patientService.updatePatient(id, caregiverId, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable UUID id) {
        UUID caregiverId = securityUtils.getCurrentUserId();
        patientService.deletePatient(id, caregiverId);
        return ResponseEntity.noContent().build();
    }
}
