package com.carevoice;

import com.carevoice.dto.AuthDtos;
import com.carevoice.dto.DoseEventDtos;
import com.carevoice.dto.MedicationDtos;
import com.carevoice.dto.PatientDtos;
import com.carevoice.dto.PrescriptionDtos;
import com.carevoice.entity.DoseEventStatus;
import com.carevoice.entity.PrescriptionDraftStatus;
import com.carevoice.entity.ResponseSource;
import com.carevoice.service.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
class CareVoiceFlowTests {

    @Autowired
    private AuthService authService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private MedicationService medicationService;

    @Autowired
    private DoseEventService doseEventService;

    @Autowired
    private PrescriptionOcrService prescriptionOcrService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private NotificationService notificationService;

    @Test
    @DisplayName("Complete end-to-end caregiver workflow: Auth -> Patient -> Medication -> Keypad -> Adherence")
    void testEndToEndCaregiverFlow() {
        // 1. Caregiver Registration
        AuthDtos.RegisterRequest regReq = new AuthDtos.RegisterRequest(
                "Jonathan Carter",
                "jonathan." + UUID.randomUUID() + "@example.com",
                "Password123!"
        );
        AuthDtos.AuthResponse authRes = authService.register(regReq);
        assertNotNull(authRes.getToken());
        UUID caregiverId = UUID.fromString(authRes.getUserId());

        // 2. Patient Creation
        PatientDtos.PatientRequest patReq = new PatientDtos.PatientRequest();
        patReq.setFullName("Arthur Vance");
        patReq.setPhoneNumber("+15554321098");
        patReq.setPreferredLanguage("en-US");
        patReq.setTimezone("America/New_York");
        patReq.setEmergencyContact("Jonathan Carter - +15551112222");
        patReq.setNotes("Morning medication required at 08:00 AM.");

        PatientDtos.PatientResponse patientRes = patientService.createPatient(caregiverId, patReq);
        assertNotNull(patientRes.getId());
        assertEquals("Arthur Vance", patientRes.getFullName());

        // 3. Medication Creation
        MedicationDtos.MedicationRequest medReq = new MedicationDtos.MedicationRequest();
        medReq.setPatientId(patientRes.getId());
        medReq.setName("Hydrochlorothiazide");
        medReq.setDosage("12.5mg (1 capsule)");
        medReq.setConfirmedInstruction("Take every morning with oatmeal");
        medReq.setQuantityRemaining(30);
        medReq.setLowStockThreshold(5);
        medReq.getSchedules().add(new MedicationDtos.ScheduleDto("08:00", "DAILY", "ALL", "America/New_York"));

        MedicationDtos.MedicationResponse medRes = medicationService.createMedication(caregiverId, medReq);
        assertNotNull(medRes.getId());
        assertEquals(30, medRes.getQuantityRemaining());

        // 4. Dose Event Verification
        List<DoseEventDtos.DoseEventResponse> todayDoses = doseEventService.getTodayDoses(caregiverId);
        assertFalse(todayDoses.isEmpty(), "DoseEvent should be automatically scheduled for Arthur Vance");
        DoseEventDtos.DoseEventResponse targetDose = todayDoses.stream()
                .filter(d -> d.getPatientId().equals(patientRes.getId()))
                .findFirst().orElseThrow();

        // 5. Simulate Keypad 1 (Dose Taken)
        DoseEventDtos.SimulatedCallRequest callReq = new DoseEventDtos.SimulatedCallRequest();
        callReq.setDoseEventId(targetDose.getId());
        callReq.setKeypadInput("1");

        DoseEventDtos.SimulatedCallResponse callRes = doseEventService.simulateCall(callReq);
        assertEquals(DoseEventStatus.TAKEN.name(), callRes.getStatus());
        assertEquals(ResponseSource.SIMULATOR, callRes.getUpdatedEvent().getResponseSource());

        // 6. Verify Inventory Deduction
        MedicationDtos.MedicationResponse updatedMed = medicationService.getMedicationById(medRes.getId(), caregiverId);
        assertEquals(29, updatedMed.getQuantityRemaining(), "Stock must decrease by 1 upon confirmed adherence");

        // 7. Verify Dashboard Summary
        var summary = reportService.getDashboardSummary(caregiverId);
        assertTrue(summary.getTodayTaken() >= 1);
    }

    @Test
    @DisplayName("Safety Rule: OCR Draft cannot activate reminders until explicitly confirmed by Caregiver")
    void testPrescriptionOcrSafetyVerification() {
        // Create caregiver and patient
        AuthDtos.RegisterRequest regReq = new AuthDtos.RegisterRequest(
                "Maria Gonzalez",
                "maria." + UUID.randomUUID() + "@example.com",
                "Password123!"
        );
        AuthDtos.AuthResponse authRes = authService.register(regReq);
        UUID caregiverId = UUID.fromString(authRes.getUserId());

        PatientDtos.PatientRequest patReq = new PatientDtos.PatientRequest();
        patReq.setFullName("Carlos Gonzalez");
        patReq.setPhoneNumber("+15558889999");
        patReq.setPreferredLanguage("es-ES");
        PatientDtos.PatientResponse patientRes = patientService.createPatient(caregiverId, patReq);

        // 1. Upload OCR prescription sample
        String prescriptionRawText = "Rx: Amlodipine 5mg\nSig: 1 tablet daily in the morning\nQty: #30 tablets";
        PrescriptionDtos.PrescriptionDraftResponse draft = prescriptionOcrService.processPrescriptionUpload(
                caregiverId, patientRes.getId(), null, prescriptionRawText
        );

        // Check draft is PENDING_VERIFICATION and not active yet
        assertEquals(PrescriptionDraftStatus.PENDING_VERIFICATION, draft.getStatus());
        assertEquals("Amlodipine", draft.getParsedMedicineName());
        assertTrue(draft.getSafetyNotice().contains("UNTRUSTED OCR DRAFT"));

        // No medications should exist yet
        List<MedicationDtos.MedicationResponse> medsBefore = medicationService.getMedicationsByPatient(patientRes.getId(), caregiverId);
        assertTrue(medsBefore.isEmpty(), "No active medication must exist prior to explicit confirmation");

        // 2. Explicit Caregiver Confirmation
        PrescriptionDtos.ConfirmPrescriptionRequest confirmReq = new PrescriptionDtos.ConfirmPrescriptionRequest();
        confirmReq.setDraftId(draft.getId());
        confirmReq.setConfirmedName("Amlodipine");
        confirmReq.setConfirmedDosage("5mg (1 tablet)");
        confirmReq.setConfirmedInstructions("Tomar 1 tableta cada mañana con agua");
        confirmReq.setConfirmedQuantity(30);
        confirmReq.setLowStockThreshold(5);
        confirmReq.setReminderTime("09:00");
        confirmReq.setFrequency("DAILY");

        MedicationDtos.MedicationResponse activeMed = prescriptionOcrService.confirmPrescriptionDraft(caregiverId, confirmReq);
        assertNotNull(activeMed.getId());
        assertEquals("Amlodipine", activeMed.getName());

        // Now medication is active!
        List<MedicationDtos.MedicationResponse> medsAfter = medicationService.getMedicationsByPatient(patientRes.getId(), caregiverId);
        assertEquals(1, medsAfter.size());
    }

    @Test
    @DisplayName("Missed Dose Escalation: Keypad 2 triggers immediate caregiver notification")
    void testMissedDoseEscalation() {
        AuthDtos.RegisterRequest regReq = new AuthDtos.RegisterRequest(
                "Helen Keller",
                "helen." + UUID.randomUUID() + "@example.com",
                "Password123!"
        );
        AuthDtos.AuthResponse authRes = authService.register(regReq);
        UUID caregiverId = UUID.fromString(authRes.getUserId());

        PatientDtos.PatientRequest patReq = new PatientDtos.PatientRequest();
        patReq.setFullName("Robert Keller");
        patReq.setPhoneNumber("+15557776666");
        PatientDtos.PatientResponse patientRes = patientService.createPatient(caregiverId, patReq);

        MedicationDtos.MedicationRequest medReq = new MedicationDtos.MedicationRequest();
        medReq.setPatientId(patientRes.getId());
        medReq.setName("Warfarin");
        medReq.setDosage("2mg");
        medReq.setConfirmedInstruction("Take at 6 PM sharp");
        medReq.setQuantityRemaining(15);
        medReq.setLowStockThreshold(3);
        medReq.getSchedules().add(new MedicationDtos.ScheduleDto("18:00", "DAILY", "ALL", "UTC"));

        medicationService.createMedication(caregiverId, medReq);

        List<DoseEventDtos.DoseEventResponse> doses = doseEventService.getTodayDoses(caregiverId);
        DoseEventDtos.DoseEventResponse dose = doses.stream()
                .filter(d -> d.getPatientId().equals(patientRes.getId()))
                .findFirst().orElseThrow();

        // Simulate Keypad 2 (Not Taken)
        DoseEventDtos.SimulatedCallRequest callReq = new DoseEventDtos.SimulatedCallRequest();
        callReq.setDoseEventId(dose.getId());
        callReq.setKeypadInput("2");

        DoseEventDtos.SimulatedCallResponse res = doseEventService.simulateCall(callReq);
        assertEquals(DoseEventStatus.MISSED.name(), res.getStatus());

        // Check that notification was sent to caregiver
        var notifications = notificationService.getCaregiverNotifications(caregiverId);
        assertFalse(notifications.isEmpty(), "Caregiver alert must be recorded for missed dose");
        assertTrue(notifications.get(0).getMessage().contains("ALERT"));
    }
}
