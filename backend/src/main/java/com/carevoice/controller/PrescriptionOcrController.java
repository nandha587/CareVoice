package com.carevoice.controller;

import com.carevoice.dto.MedicationDtos;
import com.carevoice.dto.PrescriptionDtos;
import com.carevoice.security.SecurityUtils;
import com.carevoice.service.PrescriptionOcrService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/prescriptions")
public class PrescriptionOcrController {

    private final PrescriptionOcrService ocrService;
    private final SecurityUtils securityUtils;

    public PrescriptionOcrController(PrescriptionOcrService ocrService, SecurityUtils securityUtils) {
        this.ocrService = ocrService;
        this.securityUtils = securityUtils;
    }

    /**
     * Uploads prescription image or text for OCR extraction.
     * Output is strictly UNTRUSTED draft awaiting caregiver verification.
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PrescriptionDtos.PrescriptionDraftResponse> uploadPrescription(
            @RequestParam UUID patientId,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "sampleText", required = false) String sampleText) {

        UUID caregiverId = securityUtils.getCurrentUserId();
        PrescriptionDtos.PrescriptionDraftResponse response =
                ocrService.processPrescriptionUpload(caregiverId, patientId, file, sampleText);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/drafts")
    public ResponseEntity<List<PrescriptionDtos.PrescriptionDraftResponse>> getDrafts() {
        UUID caregiverId = securityUtils.getCurrentUserId();
        List<PrescriptionDtos.PrescriptionDraftResponse> drafts = ocrService.getCaregiverDrafts(caregiverId);
        return ResponseEntity.ok(drafts);
    }

    /**
     * CRITICAL HUMAN-IN-THE-LOOP VERIFICATION GATE:
     * Explicit caregiver confirmation required before any reminder can be activated.
     */
    @PostMapping("/confirm")
    public ResponseEntity<MedicationDtos.MedicationResponse> confirmDraft(
            @Valid @RequestBody PrescriptionDtos.ConfirmPrescriptionRequest request) {

        UUID caregiverId = securityUtils.getCurrentUserId();
        MedicationDtos.MedicationResponse response = ocrService.confirmPrescriptionDraft(caregiverId, request);
        return ResponseEntity.ok(response);
    }
}
