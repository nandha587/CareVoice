package com.carevoice.service;

import com.carevoice.dto.DoseEventDtos;
import com.carevoice.dto.MedicationDtos;
import com.carevoice.dto.NotificationDtos;
import com.carevoice.dto.ReportDtos;
import com.carevoice.entity.DoseEvent;
import com.carevoice.entity.DoseEventStatus;
import com.carevoice.entity.Patient;
import com.carevoice.exception.ResourceNotFoundException;
import com.carevoice.repository.DoseEventRepository;
import com.carevoice.repository.MedicationRepository;
import com.carevoice.repository.NotificationRepository;
import com.carevoice.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final DoseEventRepository doseEventRepository;
    private final MedicationRepository medicationRepository;
    private final PatientRepository patientRepository;
    private final NotificationRepository notificationRepository;
    private final DoseEventService doseEventService;
    private final MedicationService medicationService;
    private final NotificationService notificationService;

    public ReportService(DoseEventRepository doseEventRepository,
                         MedicationRepository medicationRepository,
                         PatientRepository patientRepository,
                         NotificationRepository notificationRepository,
                         DoseEventService doseEventService,
                         MedicationService medicationService,
                         NotificationService notificationService) {
        this.doseEventRepository = doseEventRepository;
        this.medicationRepository = medicationRepository;
        this.patientRepository = patientRepository;
        this.notificationRepository = notificationRepository;
        this.doseEventService = doseEventService;
        this.medicationService = medicationService;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public ReportDtos.DashboardSummaryResponse getDashboardSummary(UUID caregiverId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        List<DoseEventDtos.DoseEventResponse> todayDoses = doseEventService.getTodayDoses(caregiverId);

        long taken = todayDoses.stream().filter(d -> d.getStatus() == DoseEventStatus.TAKEN).count();
        long missed = todayDoses.stream().filter(d -> d.getStatus() == DoseEventStatus.MISSED).count();
        long noResponse = todayDoses.stream().filter(d -> d.getStatus() == DoseEventStatus.NO_RESPONSE).count();
        long scheduled = todayDoses.stream().filter(d -> d.getStatus() == DoseEventStatus.SCHEDULED || d.getStatus() == DoseEventStatus.CALLING).count();

        // 7-day adherence percentage calculation
        LocalDateTime sevenDaysAgo = LocalDate.now().minusDays(7).atStartOfDay();
        long pastTotal = doseEventRepository.countTotalByCaregiverIdAndDateRange(caregiverId, sevenDaysAgo, endOfDay);
        long pastTaken = doseEventRepository.countByCaregiverIdAndStatusAndDateRange(caregiverId, DoseEventStatus.TAKEN, sevenDaysAgo, endOfDay);

        double adherencePct = (pastTotal > 0) ? ((double) pastTaken / pastTotal) * 100.0 : 100.0;
        adherencePct = Math.round(adherencePct * 10.0) / 10.0;

        List<MedicationDtos.MedicationResponse> lowStock = medicationService.getLowStockMedications(caregiverId);
        List<NotificationDtos.NotificationResponse> recentAlerts = notificationService.getCaregiverNotifications(caregiverId)
                .stream().limit(5).collect(Collectors.toList());

        ReportDtos.DashboardSummaryResponse response = new ReportDtos.DashboardSummaryResponse();
        response.setTodayTotal(todayDoses.size());
        response.setTodayTaken(taken);
        response.setTodayMissed(missed);
        response.setTodayNoResponse(noResponse);
        response.setTodayScheduled(scheduled);
        response.setWeeklyAdherencePercentage(adherencePct);
        response.setLowStockMedications(lowStock);
        response.setTodayDoses(todayDoses);
        response.setRecentAlerts(recentAlerts);

        return response;
    }

    @Transactional(readOnly = true)
    public ReportDtos.WeeklyReportResponse getWeeklyReport(UUID patientId, UUID caregiverId) {
        Patient patient = patientRepository.findByIdAndCaregiverId(patientId, caregiverId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + patientId));

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(7);
        LocalDateTime startDt = startDate.atStartOfDay();
        LocalDateTime endDt = endDate.atTime(LocalTime.MAX);

        List<DoseEvent> events = doseEventRepository.findAllByPatientIdAndDateRange(patientId, startDt, endDt);

        long total = events.size();
        long taken = events.stream().filter(e -> e.getStatus() == DoseEventStatus.TAKEN).count();
        long missed = events.stream().filter(e -> e.getStatus() == DoseEventStatus.MISSED).count();
        long noResponse = events.stream().filter(e -> e.getStatus() == DoseEventStatus.NO_RESPONSE).count();

        double adherencePct = (total > 0) ? ((double) taken / total) * 100.0 : 100.0;
        adherencePct = Math.round(adherencePct * 10.0) / 10.0;

        List<String> lowStockWarnings = new ArrayList<>();
        medicationRepository.findAllByPatientId(patientId).forEach(m -> {
            if (m.getQuantityRemaining() <= m.getLowStockThreshold()) {
                lowStockWarnings.add(String.format("%s: only %d doses remaining (low-stock alert threshold: %d)",
                        m.getName(), m.getQuantityRemaining(), m.getLowStockThreshold()));
            }
        });

        // Strict Safety Rule: Objective factual adherence summary ONLY. NEVER generate medical claims or diagnoses!
        String narrative = String.format("%d of %d scheduled medication reminders were confirmed as taken this week (%.1f%% adherence).",
                taken, total, adherencePct);

        ReportDtos.WeeklyReportResponse report = new ReportDtos.WeeklyReportResponse();
        report.setPatientId(patient.getId());
        report.setPatientName(patient.getFullName());
        report.setPeriodStart(startDate);
        report.setPeriodEnd(endDate);
        report.setTotalScheduledDoses(total);
        report.setDosesTaken(taken);
        report.setDosesMissed(missed);
        report.setDosesNoResponse(noResponse);
        report.setAdherencePercentage(adherencePct);
        report.setLowStockWarnings(lowStockWarnings);
        report.setSummaryNarrative(narrative);

        return report;
    }
}
