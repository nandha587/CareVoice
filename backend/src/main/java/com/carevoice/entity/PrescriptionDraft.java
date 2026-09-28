package com.carevoice.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "prescription_drafts")
public class PrescriptionDraft {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caregiver_id", nullable = false)
    private User caregiver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(nullable = false)
    private String originalFilename;

    @Column(columnDefinition = "TEXT")
    private String rawOcrText;

    private String parsedMedicineName;
    private String parsedDosage;
    private String parsedInstructions;
    private String parsedFrequency;
    private Integer parsedQuantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PrescriptionDraftStatus status = PrescriptionDraftStatus.PENDING_VERIFICATION;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public PrescriptionDraft() {}

    public PrescriptionDraft(User caregiver, Patient patient, String originalFilename, String rawOcrText) {
        this.caregiver = caregiver;
        this.patient = patient;
        this.originalFilename = originalFilename;
        this.rawOcrText = rawOcrText;
        this.status = PrescriptionDraftStatus.PENDING_VERIFICATION;
        this.createdAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getCaregiver() {
        return caregiver;
    }

    public void setCaregiver(User caregiver) {
        this.caregiver = caregiver;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getRawOcrText() {
        return rawOcrText;
    }

    public void setRawOcrText(String rawOcrText) {
        this.rawOcrText = rawOcrText;
    }

    public String getParsedMedicineName() {
        return parsedMedicineName;
    }

    public void setParsedMedicineName(String parsedMedicineName) {
        this.parsedMedicineName = parsedMedicineName;
    }

    public String getParsedDosage() {
        return parsedDosage;
    }

    public void setParsedDosage(String parsedDosage) {
        this.parsedDosage = parsedDosage;
    }

    public String getParsedInstructions() {
        return parsedInstructions;
    }

    public void setParsedInstructions(String parsedInstructions) {
        this.parsedInstructions = parsedInstructions;
    }

    public String getParsedFrequency() {
        return parsedFrequency;
    }

    public void setParsedFrequency(String parsedFrequency) {
        this.parsedFrequency = parsedFrequency;
    }

    public Integer getParsedQuantity() {
        return parsedQuantity;
    }

    public void setParsedQuantity(Integer parsedQuantity) {
        this.parsedQuantity = parsedQuantity;
    }

    public PrescriptionDraftStatus getStatus() {
        return status;
    }

    public void setStatus(PrescriptionDraftStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
