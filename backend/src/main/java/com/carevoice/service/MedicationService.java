package com.carevoice.service;

import com.carevoice.dto.MedicationDtos;
import com.carevoice.entity.*;
import com.carevoice.exception.ResourceNotFoundException;
import com.carevoice.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MedicationService {

    private final MedicationRepository medicationRepository;
    private final MedicationScheduleRepository scheduleRepository;
    private final PatientRepository patientRepository;
    private final DoseEventRepository doseEventRepository;
    private final AuditLogService auditLogService;

    public MedicationService(MedicationRepository medicationRepository,
                             MedicationScheduleRepository scheduleRepository,
                             PatientRepository patientRepository,
                             DoseEventRepository doseEventRepository,
                             AuditLogService auditLogService) {
        this.medicationRepository = medicationRepository;
        this.scheduleRepository = scheduleRepository;
        this.patientRepository = patientRepository;
        this.doseEventRepository = doseEventRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public MedicationDtos.MedicationResponse createMedication(UUID caregiverId, MedicationDtos.MedicationRequest request) {
        Patient patient = patientRepository.findByIdAndCaregiverId(request.getPatientId(), caregiverId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + request.getPatientId()));

        Medication medication = new Medication(
                patient,
                request.getName().trim(),
                request.getDosage().trim(),
                request.getConfirmedInstruction().trim(),
                request.getQuantityRemaining(),
                request.getLowStockThreshold()
        );

        Medication savedMedication = medicationRepository.save(medication);

        // Process schedules
        List<MedicationSchedule> savedSchedules = new ArrayList<>();
        if (request.getSchedules() != null && !request.getSchedules().isEmpty()) {
            for (MedicationDtos.ScheduleDto sDto : request.getSchedules()) {
                LocalTime time = LocalTime.parse(sDto.getReminderTime().trim(), DateTimeFormatter.ofPattern("HH:mm"));
                MedicationSchedule schedule = new MedicationSchedule(
                        savedMedication,
                        time,
                        sDto.getFrequency(),
                        sDto.getDaysOfWeek(),
                        patient.getTimezone()
                );
                savedSchedules.add(scheduleRepository.save(schedule));

                // Immediately seed a dose event for today/tomorrow so it's live in dashboard and scheduler
                seedInitialDoseEvents(schedule, patient);
            }
        }
        savedMedication.getSchedules().addAll(savedSchedules);

        auditLogService.log(caregiverId, "MEDICATION_CREATED", "Medication", savedMedication.getId().toString(),
                "Added medication: " + savedMedication.getName() + " for patient: " + patient.getFullName());

        return mapToDto(savedMedication);
    }

    private void seedInitialDoseEvents(MedicationSchedule schedule, Patient patient) {
        LocalDate today = LocalDate.now();

        // Seed today's dose event
        LocalDateTime scheduledToday = LocalDateTime.of(today, schedule.getReminderTime());
        if (!doseEventRepository.existsByMedicationScheduleIdAndScheduledAt(schedule.getId(), scheduledToday)) {
            DoseEvent event = new DoseEvent(schedule, patient, scheduledToday);
            doseEventRepository.save(event);
        }

        // Also schedule tomorrow's dose
        LocalDateTime scheduledTomorrow = LocalDateTime.of(today.plusDays(1), schedule.getReminderTime());
        if (!doseEventRepository.existsByMedicationScheduleIdAndScheduledAt(schedule.getId(), scheduledTomorrow)) {
            DoseEvent event = new DoseEvent(schedule, patient, scheduledTomorrow);
            doseEventRepository.save(event);
        }
    }

    @Transactional(readOnly = true)
    public List<MedicationDtos.MedicationResponse> getMedicationsByPatient(UUID patientId, UUID caregiverId) {
        patientRepository.findByIdAndCaregiverId(patientId, caregiverId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + patientId));

        return medicationRepository.findAllByPatientId(patientId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MedicationDtos.MedicationResponse> getAllMedicationsByCaregiver(UUID caregiverId) {
        return medicationRepository.findAllByCaregiverId(caregiverId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MedicationDtos.MedicationResponse> getLowStockMedications(UUID caregiverId) {
        return medicationRepository.findLowStockByCaregiverId(caregiverId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MedicationDtos.MedicationResponse getMedicationById(UUID medicationId, UUID caregiverId) {
        Medication medication = medicationRepository.findByIdAndCaregiverId(medicationId, caregiverId)
                .orElseThrow(() -> new ResourceNotFoundException("Medication not found with id: " + medicationId));
        return mapToDto(medication);
    }

    @Transactional
    public MedicationDtos.MedicationResponse updateMedication(UUID medicationId, UUID caregiverId, MedicationDtos.MedicationRequest request) {
        Medication medication = medicationRepository.findByIdAndCaregiverId(medicationId, caregiverId)
                .orElseThrow(() -> new ResourceNotFoundException("Medication not found with id: " + medicationId));

        medication.setName(request.getName().trim());
        medication.setDosage(request.getDosage().trim());
        medication.setConfirmedInstruction(request.getConfirmedInstruction().trim());
        medication.setQuantityRemaining(request.getQuantityRemaining());
        medication.setLowStockThreshold(request.getLowStockThreshold());

        Medication updated = medicationRepository.save(medication);
        auditLogService.log(caregiverId, "MEDICATION_UPDATED", "Medication", medicationId.toString(),
                "Updated medication: " + updated.getName());

        return mapToDto(updated);
    }

    @Transactional
    public void deleteMedication(UUID medicationId, UUID caregiverId) {
        Medication medication = medicationRepository.findByIdAndCaregiverId(medicationId, caregiverId)
                .orElseThrow(() -> new ResourceNotFoundException("Medication not found with id: " + medicationId));

        medication.setActive(false);
        medicationRepository.save(medication);
        auditLogService.log(caregiverId, "MEDICATION_DEACTIVATED", "Medication", medicationId.toString(),
                "Deactivated medication: " + medication.getName());
    }

    public MedicationDtos.MedicationResponse mapToDto(Medication m) {
        MedicationDtos.MedicationResponse dto = new MedicationDtos.MedicationResponse();
        dto.setId(m.getId());
        dto.setPatientId(m.getPatient().getId());
        dto.setPatientName(m.getPatient().getFullName());
        dto.setName(m.getName());
        dto.setDosage(m.getDosage());
        dto.setConfirmedInstruction(m.getConfirmedInstruction());
        dto.setQuantityRemaining(m.getQuantityRemaining());
        dto.setLowStockThreshold(m.getLowStockThreshold());
        dto.setActive(m.getActive());
        dto.setCreatedAt(m.getCreatedAt());

        if (m.getSchedules() != null) {
            dto.setSchedules(m.getSchedules().stream().map(s -> {
                MedicationDtos.ScheduleDto sDto = new MedicationDtos.ScheduleDto();
                sDto.setId(s.getId());
                sDto.setReminderTime(s.getReminderTime().format(DateTimeFormatter.ofPattern("HH:mm")));
                sDto.setFrequency(s.getFrequency());
                sDto.setDaysOfWeek(s.getDaysOfWeek());
                sDto.setTimezone(s.getTimezone());
                sDto.setActive(s.getActive());
                return sDto;
            }).collect(Collectors.toList()));
        }

        return dto;
    }
}
