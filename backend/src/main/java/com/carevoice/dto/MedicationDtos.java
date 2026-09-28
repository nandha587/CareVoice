package com.carevoice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MedicationDtos {

    public static class ScheduleDto {
        private UUID id;

        @NotBlank(message = "Reminder time is required (e.g. 08:30)")
        private String reminderTime; // e.g. "08:30"

        private String frequency = "DAILY";
        private String daysOfWeek = "ALL";
        private String timezone = "UTC";
        private Boolean active = true;

        public ScheduleDto() {}

        public ScheduleDto(String reminderTime, String frequency, String daysOfWeek, String timezone) {
            this.reminderTime = reminderTime;
            this.frequency = frequency;
            this.daysOfWeek = daysOfWeek;
            this.timezone = timezone;
            this.active = true;
        }

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        public String getReminderTime() { return reminderTime; }
        public void setReminderTime(String reminderTime) { this.reminderTime = reminderTime; }
        public String getFrequency() { return frequency; }
        public void setFrequency(String frequency) { this.frequency = frequency; }
        public String getDaysOfWeek() { return daysOfWeek; }
        public void setDaysOfWeek(String daysOfWeek) { this.daysOfWeek = daysOfWeek; }
        public String getTimezone() { return timezone; }
        public void setTimezone(String timezone) { this.timezone = timezone; }
        public Boolean getActive() { return active; }
        public void setActive(Boolean active) { this.active = active; }
    }

    public static class MedicationRequest {
        @NotNull(message = "Patient ID is required")
        private UUID patientId;

        @NotBlank(message = "Medication name is required")
        private String name;

        @NotBlank(message = "Dosage is required (e.g. 1 tablet, 10mg)")
        private String dosage;

        @NotBlank(message = "Caregiver confirmed instruction is required")
        private String confirmedInstruction;

        @NotNull(message = "Quantity remaining is required")
        @Min(value = 0, message = "Quantity cannot be negative")
        private Integer quantityRemaining = 30;

        @NotNull(message = "Low stock threshold is required")
        @Min(value = 1, message = "Low stock threshold must be at least 1")
        private Integer lowStockThreshold = 5;

        private List<ScheduleDto> schedules = new ArrayList<>();

        public UUID getPatientId() { return patientId; }
        public void setPatientId(UUID patientId) { this.patientId = patientId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDosage() { return dosage; }
        public void setDosage(String dosage) { this.dosage = dosage; }
        public String getConfirmedInstruction() { return confirmedInstruction; }
        public void setConfirmedInstruction(String confirmedInstruction) { this.confirmedInstruction = confirmedInstruction; }
        public Integer getQuantityRemaining() { return quantityRemaining; }
        public void setQuantityRemaining(Integer quantityRemaining) { this.quantityRemaining = quantityRemaining; }
        public Integer getLowStockThreshold() { return lowStockThreshold; }
        public void setLowStockThreshold(Integer lowStockThreshold) { this.lowStockThreshold = lowStockThreshold; }
        public List<ScheduleDto> getSchedules() { return schedules; }
        public void setSchedules(List<ScheduleDto> schedules) { this.schedules = schedules; }
    }

    public static class MedicationResponse {
        private UUID id;
        private UUID patientId;
        private String patientName;
        private String name;
        private String dosage;
        private String confirmedInstruction;
        private Integer quantityRemaining;
        private Integer lowStockThreshold;
        private Boolean active;
        private boolean isLowStock;
        private List<ScheduleDto> schedules = new ArrayList<>();
        private LocalDateTime createdAt;

        public MedicationResponse() {}

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        public UUID getPatientId() { return patientId; }
        public void setPatientId(UUID patientId) { this.patientId = patientId; }
        public String getPatientName() { return patientName; }
        public void setPatientName(String patientName) { this.patientName = patientName; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDosage() { return dosage; }
        public void setDosage(String dosage) { this.dosage = dosage; }
        public String getConfirmedInstruction() { return confirmedInstruction; }
        public void setConfirmedInstruction(String confirmedInstruction) { this.confirmedInstruction = confirmedInstruction; }
        public Integer getQuantityRemaining() { return quantityRemaining; }
        public void setQuantityRemaining(Integer quantityRemaining) { 
            this.quantityRemaining = quantityRemaining;
            checkLowStock();
        }
        public Integer getLowStockThreshold() { return lowStockThreshold; }
        public void setLowStockThreshold(Integer lowStockThreshold) { 
            this.lowStockThreshold = lowStockThreshold;
            checkLowStock();
        }
        public Boolean getActive() { return active; }
        public void setActive(Boolean active) { this.active = active; }
        public boolean isLowStock() { return isLowStock; }
        public void setLowStock(boolean lowStock) { isLowStock = lowStock; }
        public List<ScheduleDto> getSchedules() { return schedules; }
        public void setSchedules(List<ScheduleDto> schedules) { this.schedules = schedules; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

        private void checkLowStock() {
            if (this.quantityRemaining != null && this.lowStockThreshold != null) {
                this.isLowStock = this.quantityRemaining <= this.lowStockThreshold;
            }
        }
    }
}
