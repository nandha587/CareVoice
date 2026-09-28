package com.carevoice.service;

import com.carevoice.dto.PatientDtos;
import com.carevoice.entity.Patient;
import com.carevoice.entity.User;
import com.carevoice.exception.ResourceNotFoundException;
import com.carevoice.repository.PatientRepository;
import com.carevoice.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public PatientService(PatientRepository patientRepository,
                          UserRepository userRepository,
                          AuditLogService auditLogService) {
        this.patientRepository = patientRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public PatientDtos.PatientResponse createPatient(UUID caregiverId, PatientDtos.PatientRequest request) {
        User caregiver = userRepository.findById(caregiverId)
                .orElseThrow(() -> new ResourceNotFoundException("Caregiver not found with id: " + caregiverId));

        Patient patient = new Patient(
                caregiver,
                request.getFullName().trim(),
                request.getPhoneNumber().trim(),
                request.getPreferredLanguage() != null ? request.getPreferredLanguage() : "en-US",
                request.getTimezone() != null ? request.getTimezone() : "UTC"
        );
        patient.setEmergencyContact(request.getEmergencyContact());
        patient.setNotes(request.getNotes());

        Patient saved = patientRepository.save(patient);
        auditLogService.log(caregiverId, "PATIENT_CREATED", "Patient", saved.getId().toString(),
                "Created patient: " + saved.getFullName());

        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public List<PatientDtos.PatientResponse> getPatients(UUID caregiverId) {
        return patientRepository.findAllByCaregiverId(caregiverId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PatientDtos.PatientResponse getPatientById(UUID patientId, UUID caregiverId) {
        Patient patient = patientRepository.findByIdAndCaregiverId(patientId, caregiverId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + patientId));
        return mapToDto(patient);
    }

    @Transactional
    public PatientDtos.PatientResponse updatePatient(UUID patientId, UUID caregiverId, PatientDtos.PatientRequest request) {
        Patient patient = patientRepository.findByIdAndCaregiverId(patientId, caregiverId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + patientId));

        patient.setFullName(request.getFullName().trim());
        patient.setPhoneNumber(request.getPhoneNumber().trim());
        patient.setPreferredLanguage(request.getPreferredLanguage() != null ? request.getPreferredLanguage() : patient.getPreferredLanguage());
        patient.setTimezone(request.getTimezone() != null ? request.getTimezone() : patient.getTimezone());
        patient.setEmergencyContact(request.getEmergencyContact());
        patient.setNotes(request.getNotes());

        Patient updated = patientRepository.save(patient);
        auditLogService.log(caregiverId, "PATIENT_UPDATED", "Patient", updated.getId().toString(),
                "Updated patient: " + updated.getFullName());

        return mapToDto(updated);
    }

    @Transactional
    public void deletePatient(UUID patientId, UUID caregiverId) {
        Patient patient = patientRepository.findByIdAndCaregiverId(patientId, caregiverId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + patientId));

        patientRepository.delete(patient);
        auditLogService.log(caregiverId, "PATIENT_DELETED", "Patient", patientId.toString(),
                "Deleted patient: " + patient.getFullName());
    }

    public PatientDtos.PatientResponse mapToDto(Patient p) {
        return new PatientDtos.PatientResponse(
                p.getId(),
                p.getFullName(),
                p.getPhoneNumber(),
                p.getPreferredLanguage(),
                p.getTimezone(),
                p.getEmergencyContact(),
                p.getNotes(),
                p.getCreatedAt()
        );
    }
}
