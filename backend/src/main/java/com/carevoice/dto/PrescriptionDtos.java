package com.carevoice.dto;

import com.carevoice.entity.PrescriptionDraftStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

public class PrescriptionDtos {

    public static class PrescriptionDraftResponse {
        private UUID id;
        private UUID patientId;
        private String patientName;
        private String originalFilename;
        private String rawOcrText;
        private String parsedMedicineName;
        private String parsedDosage;
        private String parsedInstructions;
        private String parsedFrequency;
        private Integer parsedQuantity;
        private PrescriptionDraftStatus status;
        private String safetyNotice = "UNTRUSTED OCR DRAFT: Review and confirm all details before activating reminders.";
        private LocalDateTime createdAt;

        public PrescriptionDraftResponse() {}

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        public UUID getPatientId() { return patientId; }
        public void setPatientId(UUID patientId) { this.patientId = patientId; }
        public String getPatientName() { return patientName; }
        public void setPatientName(String patientName) { this.patientName = patientName; }
        public String getOriginalFilename() { return originalFilename; }
        public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }
        public String getRawOcrText() { return rawOcrText; }
        public void setRawOcrText(String rawOcrText) { this.rawOcrText = rawOcrText; }
        public String getParsedMedicineName() { return parsedMedicineName; }
        public void setParsedMedicineName(String parsedMedicineName) { this.parsedMedicineName = parsedMedicineName; }
        public String getParsedDosage() { return parsedDosage; }
        public void setParsedDosage(String parsedDosage) { this.parsedDosage = parsedDosage; }
        public String getParsedInstructions() { return parsedInstructions; }
        public void setParsedInstructions(String parsedInstructions) { this.parsedInstructions = parsedInstructions; }
        public String getParsedFrequency() { return parsedFrequency; }
        public void setParsedFrequency(String parsedFrequency) { this.parsedFrequency = parsedFrequency; }
        public Integer getParsedQuantity() { return parsedQuantity; }
        public void setParsedQuantity(Integer parsedQuantity) { this.parsedQuantity = parsedQuantity; }
        public PrescriptionDraftStatus getStatus() { return status; }
        public void setStatus(PrescriptionDraftStatus status) { this.status = status; }
        public String getSafetyNotice() { return safetyNotice; }
        public void setSafetyNotice(String safetyNotice) { this.safetyNotice = safetyNotice; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    public static class ConfirmPrescriptionRequest {
        @NotNull(message = "Draft ID is required")
        private UUID draftId;

        @NotBlank(message = "Confirmed medicine name is required")
        private String confirmedName;

        @NotBlank(message = "Confirmed dosage is required")
        private String confirmedDosage;

        @NotBlank(message = "Confirmed instruction is required")
        private String confirmedInstructions;

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer confirmedQuantity;

        @NotNull(message = "Low stock threshold is required")
        @Min(value = 1, message = "Low stock threshold must be at least 1")
        private Integer lowStockThreshold = 5;

        @NotBlank(message = "Reminder time is required (e.g. 08:30)")
        private String reminderTime = "09:00";

        private String frequency = "DAILY";
        private String daysOfWeek = "ALL";

        public UUID getDraftId() { return draftId; }
        public void setDraftId(UUID draftId) { this.draftId = draftId; }
        public String getConfirmedName() { return confirmedName; }
        public void setConfirmedName(String confirmedName) { this.confirmedName = confirmedName; }
        public String getConfirmedDosage() { return confirmedDosage; }
        public void setConfirmedDosage(String confirmedDosage) { this.confirmedDosage = confirmedDosage; }
        public String getConfirmedInstructions() { return confirmedInstructions; }
        public void setConfirmedInstructions(String confirmedInstructions) { this.confirmedInstructions = confirmedInstructions; }
        public Integer getConfirmedQuantity() { return confirmedQuantity; }
        public void setConfirmedQuantity(Integer confirmedQuantity) { this.confirmedQuantity = confirmedQuantity; }
        public Integer getLowStockThreshold() { return lowStockThreshold; }
        public void setLowStockThreshold(Integer lowStockThreshold) { this.lowStockThreshold = lowStockThreshold; }
        public String getReminderTime() { return reminderTime; }
        public void setReminderTime(String reminderTime) { this.reminderTime = reminderTime; }
        public String getFrequency() { return frequency; }
        public void setFrequency(String frequency) { this.frequency = frequency; }
        public String getDaysOfWeek() { return daysOfWeek; }
        public void setDaysOfWeek(String daysOfWeek) { this.daysOfWeek = daysOfWeek; }
    }
}
