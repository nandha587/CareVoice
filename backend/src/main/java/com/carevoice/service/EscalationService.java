package com.carevoice.service;

import com.carevoice.config.TelephonyProperties;
import com.carevoice.entity.*;
import com.carevoice.repository.DoseEventRepository;
import com.carevoice.repository.MedicationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class EscalationService {

    private static final Logger log = LoggerFactory.getLogger(EscalationService.class);

    private final DoseEventRepository doseEventRepository;
    private final MedicationRepository medicationRepository;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;
    private final TelephonyProperties telephonyProperties;

    public EscalationService(DoseEventRepository doseEventRepository,
                             MedicationRepository medicationRepository,
                             NotificationService notificationService,
                             AuditLogService auditLogService,
                             TelephonyProperties telephonyProperties) {
        this.doseEventRepository = doseEventRepository;
        this.medicationRepository = medicationRepository;
        this.notificationService = notificationService;
        this.auditLogService = auditLogService;
        this.telephonyProperties = telephonyProperties;
    }

    /**
     * Handles Keypad 1 (Dose confirmed taken)
     */
    @Transactional
    public DoseEvent handleDoseTaken(DoseEvent doseEvent, ResponseSource source) {
        doseEvent.setStatus(DoseEventStatus.TAKEN);
        doseEvent.setRespondedAt(LocalDateTime.now());
        doseEvent.setResponseSource(source);
        doseEvent.setNotes("Dose confirmed taken via keypad 1 (" + source + ")");

        // Inventory deduction
        Medication medication = doseEvent.getMedicationSchedule().getMedication();
        if (medication.getQuantityRemaining() > 0) {
            medication.setQuantityRemaining(medication.getQuantityRemaining() - 1);
            medicationRepository.save(medication);

            log.info("Medication {} stock decreased to {} for patient {}",
                    medication.getName(), medication.getQuantityRemaining(), doseEvent.getPatient().getFullName());

            // Check low stock threshold
            if (medication.getQuantityRemaining() <= medication.getLowStockThreshold()) {
                notificationService.sendLowStockAlert(doseEvent.getPatient().getCaregiver(), doseEvent.getPatient(), medication);
                auditLogService.log(doseEvent.getPatient().getCaregiver().getId(),
                        "LOW_STOCK_ALERT", "Medication", medication.getId().toString(),
                        "Low stock warning triggered: " + medication.getQuantityRemaining() + " remaining");
            }
        }

        DoseEvent saved = doseEventRepository.save(doseEvent);
        auditLogService.log(doseEvent.getPatient().getCaregiver().getId(),
                "DOSE_CONFIRMED_TAKEN", "DoseEvent", saved.getId().toString(),
                "Patient " + doseEvent.getPatient().getFullName() + " took " + medication.getName());

        return saved;
    }

    /**
     * Handles Keypad 2 (Dose not taken)
     */
    @Transactional
    public DoseEvent handleDoseMissed(DoseEvent doseEvent, ResponseSource source) {
        doseEvent.setStatus(DoseEventStatus.MISSED);
        doseEvent.setRespondedAt(LocalDateTime.now());
        doseEvent.setResponseSource(source);
        doseEvent.setNotes("Patient indicated dose NOT taken via keypad 2 (" + source + ")");

        DoseEvent saved = doseEventRepository.save(doseEvent);

        // Immediate caregiver alert
        notificationService.sendMissedDoseAlert(doseEvent.getPatient().getCaregiver(), doseEvent.getPatient(), saved);

        auditLogService.log(doseEvent.getPatient().getCaregiver().getId(),
                "DOSE_REPORTED_MISSED", "DoseEvent", saved.getId().toString(),
                "Patient " + doseEvent.getPatient().getFullName() + " reported dose NOT taken for " +
                        doseEvent.getMedicationSchedule().getMedication().getName());

        return saved;
    }

    /**
     * Handles Unanswered / Busy / Timeout calls
     */
    @Transactional
    public DoseEvent handleCallTimeoutOrFailure(DoseEvent doseEvent) {
        int maxRetries = telephonyProperties.getMaxRetries();

        if (doseEvent.getCallAttempts() < maxRetries) {
            // Schedule a retry
            LocalDateTime nextAttempt = LocalDateTime.now().plusMinutes(telephonyProperties.getRetryDelayMinutes());
            doseEvent.setScheduledAt(nextAttempt);
            doseEvent.setStatus(DoseEventStatus.SCHEDULED);
            doseEvent.setNotes(String.format("Call attempt %d of %d failed. Rescheduled for %s",
                    doseEvent.getCallAttempts(), maxRetries, nextAttempt.toLocalTime().toString()));

            log.info("Retry scheduled for dose {} at {}", doseEvent.getId(), nextAttempt);
            return doseEventRepository.save(doseEvent);
        } else {
            // Maximum retry attempts exhausted -> NO_RESPONSE
            doseEvent.setStatus(DoseEventStatus.NO_RESPONSE);
            doseEvent.setRespondedAt(LocalDateTime.now());
            doseEvent.setNotes(String.format("All %d reminder call attempts went unanswered.", doseEvent.getCallAttempts()));

            DoseEvent saved = doseEventRepository.save(doseEvent);

            // Escalate to caregiver
            notificationService.sendNoResponseAlert(doseEvent.getPatient().getCaregiver(), doseEvent.getPatient(), saved);

            auditLogService.log(doseEvent.getPatient().getCaregiver().getId(),
                    "DOSE_NO_RESPONSE", "DoseEvent", saved.getId().toString(),
                    "No response alert escalated for " + doseEvent.getPatient().getFullName());

            return saved;
        }
    }
}
