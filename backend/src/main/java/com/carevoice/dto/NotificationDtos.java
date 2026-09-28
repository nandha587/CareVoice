package com.carevoice.dto;

import com.carevoice.entity.NotificationChannel;
import com.carevoice.entity.NotificationStatus;
import com.carevoice.entity.NotificationType;
import java.time.LocalDateTime;
import java.util.UUID;

public class NotificationDtos {

    public static class NotificationResponse {
        private UUID id;
        private UUID patientId;
        private String patientName;
        private UUID doseEventId;
        private NotificationType type;
        private NotificationChannel channel;
        private NotificationStatus status;
        private String message;
        private LocalDateTime sentAt;
        private LocalDateTime createdAt;

        public NotificationResponse() {}

        public NotificationResponse(UUID id, UUID patientId, String patientName, UUID doseEventId, NotificationType type, NotificationChannel channel, NotificationStatus status, String message, LocalDateTime sentAt, LocalDateTime createdAt) {
            this.id = id;
            this.patientId = patientId;
            this.patientName = patientName;
            this.doseEventId = doseEventId;
            this.type = type;
            this.channel = channel;
            this.status = status;
            this.message = message;
            this.sentAt = sentAt;
            this.createdAt = createdAt;
        }

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        public UUID getPatientId() { return patientId; }
        public void setPatientId(UUID patientId) { this.patientId = patientId; }
        public String getPatientName() { return patientName; }
        public void setPatientName(String patientName) { this.patientName = patientName; }
        public UUID getDoseEventId() { return doseEventId; }
        public void setDoseEventId(UUID doseEventId) { this.doseEventId = doseEventId; }
        public NotificationType getType() { return type; }
        public void setType(NotificationType type) { this.type = type; }
        public NotificationChannel getChannel() { return channel; }
        public void setChannel(NotificationChannel channel) { this.channel = channel; }
        public NotificationStatus getStatus() { return status; }
        public void setStatus(NotificationStatus status) { this.status = status; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public LocalDateTime getSentAt() { return sentAt; }
        public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }
}
