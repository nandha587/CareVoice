package com.carevoice.service;

import com.carevoice.dto.NotificationDtos;
import com.carevoice.entity.*;
import com.carevoice.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public void sendMissedDoseAlert(User caregiver, Patient patient, DoseEvent doseEvent) {
        String medicineName = doseEvent.getMedicationSchedule().getMedication().getName();
        String message = String.format("ALERT: %s did not take their scheduled dose of %s at %s. Please check in with them.",
                patient.getFullName(),
                medicineName,
                doseEvent.getScheduledAt().toLocalTime().toString());

        saveAndDispatch(caregiver, patient, doseEvent, NotificationType.MISSED_DOSE, message);
    }

    @Transactional
    public void sendNoResponseAlert(User caregiver, Patient patient, DoseEvent doseEvent) {
        String medicineName = doseEvent.getMedicationSchedule().getMedication().getName();
        String message = String.format("URGENT: Automated reminder calls to %s (%s) for %s were unanswered after %d attempts. Please reach out directly.",
                patient.getFullName(),
                patient.getPhoneNumber(),
                medicineName,
                doseEvent.getCallAttempts());

        saveAndDispatch(caregiver, patient, doseEvent, NotificationType.NO_RESPONSE, message);
    }

    @Transactional
    public void sendLowStockAlert(User caregiver, Patient patient, Medication medication) {
        String message = String.format("LOW STOCK WARNING: %s has only %d doses remaining of %s (threshold is %d). Please arrange a refill.",
                patient.getFullName(),
                medication.getQuantityRemaining(),
                medication.getName(),
                medication.getLowStockThreshold());

        saveAndDispatch(caregiver, patient, null, NotificationType.LOW_STOCK_WARNING, message);
    }

    private void saveAndDispatch(User caregiver, Patient patient, DoseEvent doseEvent, NotificationType type, String message) {
        Notification notification = new Notification(
                caregiver,
                patient,
                doseEvent,
                type,
                NotificationChannel.CONSOLE,
                message
        );

        // Try dispatching email if mail sender is available
        if (mailSender != null && caregiver.getEmail() != null) {
            try {
                SimpleMailMessage mailMessage = new SimpleMailMessage();
                mailMessage.setTo(caregiver.getEmail());
                mailMessage.setSubject("[CareVoice Alert] " + type.name().replace('_', ' '));
                mailMessage.setText(message + "\n\nLog in to your CareVoice portal for live adherence tracking.");
                mailSender.send(mailMessage);
                notification.setChannel(NotificationChannel.EMAIL);
                notification.setStatus(NotificationStatus.SENT);
                notification.setSentAt(LocalDateTime.now());
            } catch (Exception e) {
                log.warn("Email dispatch failed, falling back to console log: {}", e.getMessage());
                notification.setStatus(NotificationStatus.FAILED);
            }
        } else {
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
        }

        log.warn("[CAREGIVER ALERT DISPATCHED] To: {} | Type: {} | Message: {}",
                caregiver.getEmail(), type, message);

        notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public List<NotificationDtos.NotificationResponse> getCaregiverNotifications(UUID caregiverId) {
        return notificationRepository.findAllByCaregiverIdOrderByCreatedAtDesc(caregiverId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private NotificationDtos.NotificationResponse mapToDto(Notification n) {
        return new NotificationDtos.NotificationResponse(
                n.getId(),
                n.getPatient() != null ? n.getPatient().getId() : null,
                n.getPatient() != null ? n.getPatient().getFullName() : "N/A",
                n.getDoseEvent() != null ? n.getDoseEvent().getId() : null,
                n.getType(),
                n.getChannel(),
                n.getStatus(),
                n.getMessage(),
                n.getSentAt(),
                n.getCreatedAt()
        );
    }
}
