package com.carevoice.controller;

import com.carevoice.entity.DoseEvent;
import com.carevoice.entity.ResponseSource;
import com.carevoice.repository.DoseEventRepository;
import com.carevoice.service.DoseEventService;
import com.carevoice.service.TelephonyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/telephony/webhook")
public class TelephonyWebhookController {

    private static final Logger log = LoggerFactory.getLogger(TelephonyWebhookController.class);

    private final DoseEventRepository doseEventRepository;
    private final DoseEventService doseEventService;
    private final TelephonyService telephonyService;

    public TelephonyWebhookController(DoseEventRepository doseEventRepository,
                                      DoseEventService doseEventService,
                                      TelephonyService telephonyService) {
        this.doseEventRepository = doseEventRepository;
        this.doseEventService = doseEventService;
        this.telephonyService = telephonyService;
    }

    /**
     * Endpoint invoked when Twilio answers the outbound call.
     * Returns TwiML XML instructing the patient to press 1 or 2.
     */
    @RequestMapping(value = "/call-voice", method = {RequestMethod.GET, RequestMethod.POST}, produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> handleVoiceRequest(@RequestParam UUID doseEventId) {
        log.info("Twilio requested voice reminder instructions for dose: {}", doseEventId);

        return doseEventRepository.findById(doseEventId)
                .map(doseEvent -> {
                    String twiml = telephonyService.generateReminderTwiML(doseEvent);
                    return ResponseEntity.ok(twiml);
                })
                .orElseGet(() -> ResponseEntity.badRequest().body("<Response><Say>Dose event not found</Say><Hangup/></Response>"));
    }

    /**
     * Endpoint invoked when patient presses a DTMF keypad digit (1 = taken, 2 = not taken).
     */
    @PostMapping(value = "/dtmf", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> handleDtmfResponse(
            @RequestParam UUID doseEventId,
            @RequestParam(required = false, name = "Digits") String digits) {

        log.info("Received DTMF digit '{}' from patient for dose: {}", digits, doseEventId);

        return doseEventRepository.findById(doseEventId)
                .map(doseEvent -> {
                    if ("1".equals(digits) || "2".equals(digits)) {
                        doseEventService.recordKeypadResponse(doseEventId, digits, ResponseSource.DTMF_PHONE);
                        String twiml = telephonyService.generateResponseTwiML(digits, doseEvent);
                        return ResponseEntity.ok(twiml);
                    } else {
                        return ResponseEntity.ok(
                                "<Response><Say>Invalid key pressed. Goodbye.</Say><Hangup/></Response>"
                        );
                    }
                })
                .orElseGet(() -> ResponseEntity.badRequest().body("<Response><Say>Invalid session</Say><Hangup/></Response>"));
    }

    /**
     * Webhook called by Twilio on call completion / failure / no-answer
     */
    @PostMapping("/call-status")
    public ResponseEntity<Void> handleCallStatus(
            @RequestParam UUID doseEventId,
            @RequestParam(required = false, name = "CallStatus") String callStatus) {

        log.info("Twilio call status update for dose {}: {}", doseEventId, callStatus);

        if ("no-answer".equalsIgnoreCase(callStatus) || "busy".equalsIgnoreCase(callStatus) || "failed".equalsIgnoreCase(callStatus)) {
            doseEventService.recordCallTimeoutOrFailure(doseEventId);
        }

        return ResponseEntity.ok().build();
    }
}
