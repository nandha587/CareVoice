package com.carevoice.controller;

import com.carevoice.dto.NotificationDtos;
import com.carevoice.dto.ReportDtos;
import com.carevoice.entity.AuditLog;
import com.carevoice.repository.AuditLogRepository;
import com.carevoice.security.SecurityUtils;
import com.carevoice.service.NotificationService;
import com.carevoice.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ReportService reportService;
    private final NotificationService notificationService;
    private final AuditLogRepository auditLogRepository;
    private final SecurityUtils securityUtils;

    public ReportController(ReportService reportService,
                            NotificationService notificationService,
                            AuditLogRepository auditLogRepository,
                            SecurityUtils securityUtils) {
        this.reportService = reportService;
        this.notificationService = notificationService;
        this.auditLogRepository = auditLogRepository;
        this.securityUtils = securityUtils;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ReportDtos.DashboardSummaryResponse> getDashboardSummary() {
        UUID caregiverId = securityUtils.getCurrentUserId();
        ReportDtos.DashboardSummaryResponse response = reportService.getDashboardSummary(caregiverId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/weekly/{patientId}")
    public ResponseEntity<ReportDtos.WeeklyReportResponse> getWeeklyReport(@PathVariable UUID patientId) {
        UUID caregiverId = securityUtils.getCurrentUserId();
        ReportDtos.WeeklyReportResponse report = reportService.getWeeklyReport(patientId, caregiverId);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/notifications")
    public ResponseEntity<List<NotificationDtos.NotificationResponse>> getNotifications() {
        UUID caregiverId = securityUtils.getCurrentUserId();
        List<NotificationDtos.NotificationResponse> notifications = notificationService.getCaregiverNotifications(caregiverId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<List<AuditLog>> getAuditLogs() {
        UUID caregiverId = securityUtils.getCurrentUserId();
        List<AuditLog> logs = auditLogRepository.findAllByUserIdOrderByTimestampDesc(caregiverId);
        return ResponseEntity.ok(logs);
    }
}
