package com.carevoice.dto;

import com.carevoice.entity.DoseEventStatus;
import com.carevoice.entity.ResponseSource;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

public class DoseEventDtos {

    public static class DoseEventResponse {
        private UUID id;
        private UUID patientId;
        private String patientName;
        private String patientPhone;
        private String preferredLanguage;
        private UUID medicationId;
        private String medicationName;
        private String dosage;
        private String confirmedInstruction;
        private LocalDateTime scheduledAt;
        private DoseEventStatus status;
        private int callAttempts;
        private LocalDateTime lastCalledAt;
        private LocalDateTime respondedAt;
        private ResponseSource responseSource;
        private String notes;
        private LocalDateTime createdAt;

        public DoseEventResponse() {}

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        public UUID getPatientId() { return patientId; }
        public void setPatientId(UUID patientId) { this.patientId = patientId; }
        public String getPatientName() { return patientName; }
        public void setPatientName(String patientName) { this.patientName = patientName; }
        public String getPatientPhone() { return patientPhone; }
        public void setPatientPhone(String patientPhone) { this.patientPhone = patientPhone; }
        public String getPreferredLanguage() { return preferredLanguage; }
        public void setPreferredLanguage(String preferredLanguage) { this.preferredLanguage = preferredLanguage; }
        public UUID getMedicationId() { return medicationId; }
        public void setMedicationId(UUID medicationId) { this.medicationId = medicationId; }
        public String getMedicationName() { return medicationName; }
        public void setMedicationName(String medicationName) { this.medicationName = medicationName; }
        public String getDosage() { return dosage; }
        public void setDosage(String dosage) { this.dosage = dosage; }
        public String getConfirmedInstruction() { return confirmedInstruction; }
        public void setConfirmedInstruction(String confirmedInstruction) { this.confirmedInstruction = confirmedInstruction; }
        public LocalDateTime getScheduledAt() { return scheduledAt; }
        public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }
        public DoseEventStatus getStatus() { return status; }
        public void setStatus(DoseEventStatus status) { this.status = status; }
        public int getCallAttempts() { return callAttempts; }
        public void setCallAttempts(int callAttempts) { this.callAttempts = callAttempts; }
        public LocalDateTime getLastCalledAt() { return lastCalledAt; }
        public void setLastCalledAt(LocalDateTime lastCalledAt) { this.lastCalledAt = lastCalledAt; }
        public LocalDateTime getRespondedAt() { return respondedAt; }
        public void setRespondedAt(LocalDateTime respondedAt) { this.respondedAt = respondedAt; }
        public ResponseSource getResponseSource() { return responseSource; }
        public void setResponseSource(ResponseSource responseSource) { this.responseSource = responseSource; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    public static class DoseStatusUpdateRequest {
        @NotNull(message = "Status is required")
        private DoseEventStatus status;

        private ResponseSource responseSource = ResponseSource.CAREGIVER_MANUAL;
        private String notes;

        public DoseEventStatus getStatus() { return status; }
        public void setStatus(DoseEventStatus status) { this.status = status; }
        public ResponseSource getResponseSource() { return responseSource; }
        public void setResponseSource(ResponseSource responseSource) { this.responseSource = responseSource; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    public static class SimulatedCallRequest {
        @NotNull(message = "Dose event ID is required")
        private UUID doseEventId;

        // "1" (TAKEN), "2" (MISSED/NOT TAKEN), "TIMEOUT" (NO_RESPONSE)
        private String keypadInput;

        public UUID getDoseEventId() { return doseEventId; }
        public void setDoseEventId(UUID doseEventId) { this.doseEventId = doseEventId; }
        public String getKeypadInput() { return keypadInput; }
        public void setKeypadInput(String keypadInput) { this.keypadInput = keypadInput; }
    }

    public static class SimulatedCallResponse {
        private String status;
        private String spokenScript;
        private String patientResponse;
        private DoseEventResponse updatedEvent;

        public SimulatedCallResponse(String status, String spokenScript, String patientResponse, DoseEventResponse updatedEvent) {
            this.status = status;
            this.spokenScript = spokenScript;
            this.patientResponse = patientResponse;
            this.updatedEvent = updatedEvent;
        }

        public String getStatus() { return status; }
        public String getSpokenScript() { return spokenScript; }
        public String getPatientResponse() { return patientResponse; }
        public DoseEventResponse getUpdatedEvent() { return updatedEvent; }
    }
}
