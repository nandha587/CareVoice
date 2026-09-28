package com.carevoice.service;

import com.carevoice.dto.DoseEventDtos;
import com.carevoice.entity.*;
import com.carevoice.exception.BadRequestException;
import com.carevoice.exception.ResourceNotFoundException;
import com.carevoice.repository.DoseEventRepository;
import com.carevoice.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DoseEventService {

    private final DoseEventRepository doseEventRepository;
    private final PatientRepository patientRepository;
    private final EscalationService escalationService;
    private final TelephonyService telephonyService;
    private final AuditLogService auditLogService;

    public DoseEventService(DoseEventRepository doseEventRepository,
                            PatientRepository patientRepository,
                            EscalationService escalationService,
                            TelephonyService telephonyService,
                            AuditLogService auditLogService) {
        this.doseEventRepository = doseEventRepository;
        this.patientRepository = patientRepository;
        this.escalationService = escalationService;
        this.telephonyService = telephonyService;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public DoseEvent recordKeypadResponse(UUID doseEventId, String digit, ResponseSource source) {
        DoseEvent doseEvent = doseEventRepository.findById(doseEventId)
                .orElseThrow(() -> new ResourceNotFoundException("Dose event not found with id: " + doseEventId));

        if ("1".equals(digit)) {
            return escalationService.handleDoseTaken(doseEvent, source);
        } else if ("2".equals(digit)) {
            return escalationService.handleDoseMissed(doseEvent, source);
        } else {
            throw new BadRequestException("Invalid keypad response. Must be 1 (Taken) or 2 (Not Taken).");
        }
    }

    @Transactional
    public DoseEvent recordCallTimeoutOrFailure(UUID doseEventId) {
        DoseEvent doseEvent = doseEventRepository.findById(doseEventId)
                .orElseThrow(() -> new ResourceNotFoundException("Dose event not found with id: " + doseEventId));

        return escalationService.handleCallTimeoutOrFailure(doseEvent);
    }

    @Transactional
    public DoseEventDtos.DoseEventResponse manualStatusUpdate(UUID doseEventId, UUID caregiverId, DoseEventDtos.DoseStatusUpdateRequest request) {
        DoseEvent doseEvent = doseEventRepository.findByIdAndCaregiverId(doseEventId, caregiverId)
                .orElseThrow(() -> new ResourceNotFoundException("Dose event not found with id: " + doseEventId));

        DoseEvent updated;
        if (request.getStatus() == DoseEventStatus.TAKEN) {
            updated = escalationService.handleDoseTaken(doseEvent, ResponseSource.CAREGIVER_MANUAL);
        } else if (request.getStatus() == DoseEventStatus.MISSED) {
            updated = escalationService.handleDoseMissed(doseEvent, ResponseSource.CAREGIVER_MANUAL);
        } else {
            doseEvent.setStatus(request.getStatus());
            doseEvent.setRespondedAt(LocalDateTime.now());
            doseEvent.setResponseSource(ResponseSource.CAREGIVER_MANUAL);
            if (request.getNotes() != null) {
                doseEvent.setNotes(request.getNotes());
            }
            updated = doseEventRepository.save(doseEvent);
        }

        auditLogService.log(caregiverId, "MANUAL_DOSE_UPDATE", "DoseEvent", doseEventId.toString(),
                "Caregiver manually updated status to: " + request.getStatus());

        return mapToDto(updated);
    }

    @Transactional
    public DoseEventDtos.SimulatedCallResponse simulateCall(DoseEventDtos.SimulatedCallRequest request) {
        DoseEvent doseEvent = doseEventRepository.findById(request.getDoseEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Dose event not found with id: " + request.getDoseEventId()));

        String spokenScript = telephonyService.getSpokenScript(doseEvent);
        doseEvent.setLastCalledAt(LocalDateTime.now());
        doseEvent.setCallAttempts(doseEvent.getCallAttempts() + 1);
        doseEvent.setStatus(DoseEventStatus.CALLING);
        doseEventRepository.save(doseEvent);

        DoseEvent finalEvent;
        String responseResult;

        if ("1".equals(request.getKeypadInput())) {
            finalEvent = escalationService.handleDoseTaken(doseEvent, ResponseSource.SIMULATOR);
            responseResult = "Keypad 1 pressed: Dose recorded as TAKEN. Medication quantity decremented.";
        } else if ("2".equals(request.getKeypadInput())) {
            finalEvent = escalationService.handleDoseMissed(doseEvent, ResponseSource.SIMULATOR);
            responseResult = "Keypad 2 pressed: Dose recorded as MISSED. Caregiver alert triggered.";
        } else {
            // Simulated timeout / no-response
            finalEvent = escalationService.handleCallTimeoutOrFailure(doseEvent);
            responseResult = "No response / Timeout: Attempt incremented (" + finalEvent.getCallAttempts() + "). Status: " + finalEvent.getStatus();
        }

        return new DoseEventDtos.SimulatedCallResponse(
                finalEvent.getStatus().name(),
                spokenScript,
                responseResult,
                mapToDto(finalEvent)
        );
    }

    @Transactional(readOnly = true)
    public List<DoseEventDtos.DoseEventResponse> getTodayDoses(UUID caregiverId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        return doseEventRepository.findAllByCaregiverIdAndDateRange(caregiverId, startOfDay, endOfDay)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DoseEventDtos.DoseEventResponse> getDosesByPatient(UUID patientId, UUID caregiverId, LocalDate date) {
        patientRepository.findByIdAndCaregiverId(patientId, caregiverId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + patientId));

        LocalDate queryDate = (date != null) ? date : LocalDate.now();
        LocalDateTime start = queryDate.atStartOfDay();
        LocalDateTime end = queryDate.atTime(LocalTime.MAX);

        return doseEventRepository.findAllByPatientIdAndDateRange(patientId, start, end)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public DoseEventDtos.DoseEventResponse mapToDto(DoseEvent d) {
        DoseEventDtos.DoseEventResponse dto = new DoseEventDtos.DoseEventResponse();
        dto.setId(d.getId());
        dto.setPatientId(d.getPatient().getId());
        dto.setPatientName(d.getPatient().getFullName());
        dto.setPatientPhone(d.getPatient().getPhoneNumber());
        dto.setPreferredLanguage(d.getPatient().getPreferredLanguage());

        if (d.getMedicationSchedule() != null && d.getMedicationSchedule().getMedication() != null) {
            Medication m = d.getMedicationSchedule().getMedication();
            dto.setMedicationId(m.getId());
            dto.setMedicationName(m.getName());
            dto.setDosage(m.getDosage());
            dto.setConfirmedInstruction(m.getConfirmedInstruction());
        }

        dto.setScheduledAt(d.getScheduledAt());
        dto.setStatus(d.getStatus());
        dto.setCallAttempts(d.getCallAttempts());
        dto.setLastCalledAt(d.getLastCalledAt());
        dto.setRespondedAt(d.getRespondedAt());
        dto.setResponseSource(d.getResponseSource());
        dto.setNotes(d.getNotes());
        dto.setCreatedAt(d.getCreatedAt());

        return dto;
    }
}
