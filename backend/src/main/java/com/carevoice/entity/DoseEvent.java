package com.carevoice.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "dose_events", indexes = {
    @Index(name = "idx_dose_status_sched", columnList = "status, scheduledAt"),
    @Index(name = "idx_dose_patient", columnList = "patient_id")
})
public class DoseEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medication_schedule_id", nullable = false)
    private MedicationSchedule medicationSchedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(nullable = false)
    private LocalDateTime scheduledAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DoseEventStatus status = DoseEventStatus.SCHEDULED;

    @Column(nullable = false)
    private int callAttempts = 0;

    private LocalDateTime lastCalledAt;

    private LocalDateTime respondedAt;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private ResponseSource responseSource;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public DoseEvent() {}

    public DoseEvent(MedicationSchedule medicationSchedule, Patient patient, LocalDateTime scheduledAt) {
        this.medicationSchedule = medicationSchedule;
        this.patient = patient;
        this.scheduledAt = scheduledAt;
        this.status = DoseEventStatus.SCHEDULED;
        this.callAttempts = 0;
        this.createdAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public MedicationSchedule getMedicationSchedule() {
        return medicationSchedule;
    }

    public void setMedicationSchedule(MedicationSchedule medicationSchedule) {
        this.medicationSchedule = medicationSchedule;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public DoseEventStatus getStatus() {
        return status;
    }

    public void setStatus(DoseEventStatus status) {
        this.status = status;
    }

    public int getCallAttempts() {
        return callAttempts;
    }

    public void setCallAttempts(int callAttempts) {
        this.callAttempts = callAttempts;
    }

    public LocalDateTime getLastCalledAt() {
        return lastCalledAt;
    }

    public void setLastCalledAt(LocalDateTime lastCalledAt) {
        this.lastCalledAt = lastCalledAt;
    }

    public LocalDateTime getRespondedAt() {
        return respondedAt;
    }

    public void setRespondedAt(LocalDateTime respondedAt) {
        this.respondedAt = respondedAt;
    }

    public ResponseSource getResponseSource() {
        return responseSource;
    }

    public void setResponseSource(ResponseSource responseSource) {
        this.responseSource = responseSource;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
