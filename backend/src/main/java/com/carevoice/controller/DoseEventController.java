package com.carevoice.controller;

import com.carevoice.dto.DoseEventDtos;
import com.carevoice.security.SecurityUtils;
import com.carevoice.service.DoseEventService;
import com.carevoice.service.ReminderSchedulerService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/doses")
public class DoseEventController {

    private final DoseEventService doseEventService;
    private final ReminderSchedulerService reminderSchedulerService;
    private final SecurityUtils securityUtils;

    public DoseEventController(DoseEventService doseEventService,
                               ReminderSchedulerService reminderSchedulerService,
                               SecurityUtils securityUtils) {
        this.doseEventService = doseEventService;
        this.reminderSchedulerService = reminderSchedulerService;
        this.securityUtils = securityUtils;
    }

    @GetMapping("/today")
    public ResponseEntity<List<DoseEventDtos.DoseEventResponse>> getTodayDoses() {
        UUID caregiverId = securityUtils.getCurrentUserId();
        List<DoseEventDtos.DoseEventResponse> doses = doseEventService.getTodayDoses(caregiverId);
        return ResponseEntity.ok(doses);
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<DoseEventDtos.DoseEventResponse>> getDosesByPatient(
            @PathVariable UUID patientId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        UUID caregiverId = securityUtils.getCurrentUserId();
        List<DoseEventDtos.DoseEventResponse> doses = doseEventService.getDosesByPatient(patientId, caregiverId, date);
        return ResponseEntity.ok(doses);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<DoseEventDtos.DoseEventResponse> updateDoseStatus(
            @PathVariable UUID id,
            @Valid @RequestBody DoseEventDtos.DoseStatusUpdateRequest request) {
        UUID caregiverId = securityUtils.getCurrentUserId();
        DoseEventDtos.DoseEventResponse response = doseEventService.manualStatusUpdate(id, caregiverId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/simulate-call")
    public ResponseEntity<DoseEventDtos.SimulatedCallResponse> simulateCall(
            @Valid @RequestBody DoseEventDtos.SimulatedCallRequest request) {
        DoseEventDtos.SimulatedCallResponse response = doseEventService.simulateCall(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/trigger-scheduler")
    public ResponseEntity<Map<String, Object>> triggerScheduler() {
        int remainingDue = reminderSchedulerService.triggerDueChecksNow();
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Heartbeat scheduler executed successfully");
        result.put("remainingDueDoses", remainingDue);
        return ResponseEntity.ok(result);
    }
}
