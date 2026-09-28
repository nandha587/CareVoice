package com.carevoice.service;

import com.carevoice.config.TelephonyProperties;
import com.carevoice.entity.DoseEvent;
import com.carevoice.entity.Medication;
import com.carevoice.entity.Patient;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.type.PhoneNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;

@Service("twilioTelephonyService")
public class TwilioTelephonyService implements TelephonyService {

    private static final Logger log = LoggerFactory.getLogger(TwilioTelephonyService.class);

    private final TelephonyProperties properties;
    private final SimulatedTelephonyService simulatedService;
    private boolean initialized = false;

    public TwilioTelephonyService(TelephonyProperties properties, SimulatedTelephonyService simulatedService) {
        this.properties = properties;
        this.simulatedService = simulatedService;

        if (properties.getAccountSid() != null && !properties.getAccountSid().isBlank()
                && properties.getAuthToken() != null && !properties.getAuthToken().isBlank()) {
            try {
                Twilio.init(properties.getAccountSid(), properties.getAuthToken());
                this.initialized = true;
                log.info("Twilio Telephony successfully initialized with Account SID: {}", properties.getAccountSid());
            } catch (Exception e) {
                log.error("Failed to initialize Twilio client: {}", e.getMessage());
            }
        } else {
            log.info("Twilio credentials not configured. Telephony will fall back to simulated engine.");
        }
    }

    @Override
    public String initiateCall(DoseEvent doseEvent) {
        if (!initialized) {
            log.warn("Twilio not initialized. Falling back to simulated call.");
            return simulatedService.initiateCall(doseEvent);
        }

        try {
            Patient patient = doseEvent.getPatient();
            PhoneNumber to = new PhoneNumber(patient.getPhoneNumber());
            PhoneNumber from = new PhoneNumber(properties.getFromNumber());

            String callbackUrl = properties.getWebhookBaseUrl() + "/api/v1/telephony/webhook/call-voice?doseEventId=" + doseEvent.getId();
            String statusCallback = properties.getWebhookBaseUrl() + "/api/v1/telephony/webhook/call-status?doseEventId=" + doseEvent.getId();

            Call call = Call.creator(to, from, URI.create(callbackUrl))
                    .setStatusCallback(URI.create(statusCallback))
                    .create();

            log.info("Twilio outbound call placed successfully! SID: {}", call.getSid());
            return call.getSid();
        } catch (Exception e) {
            log.error("Twilio call failed: {}. Falling back to simulation.", e.getMessage());
            return simulatedService.initiateCall(doseEvent);
        }
    }

    @Override
    public String generateReminderTwiML(DoseEvent doseEvent) {
        Patient patient = doseEvent.getPatient();
        String script = getSpokenScript(doseEvent);
        String actionUrl = properties.getWebhookBaseUrl() + "/api/v1/telephony/webhook/dtmf?doseEventId=" + doseEvent.getId();

        String voice = "Polly.Joanna";
        String language = "en-US";
        if (patient.getPreferredLanguage() != null && patient.getPreferredLanguage().startsWith("es")) {
            voice = "Polly.Conchita";
            language = "es-ES";
        } else if (patient.getPreferredLanguage() != null && patient.getPreferredLanguage().startsWith("hi")) {
            voice = "Polly.Aditi";
            language = "hi-IN";
        }

        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
               "<Response>\n" +
               "    <Gather numDigits=\"1\" action=\"" + actionUrl + "\" method=\"POST\" timeout=\"15\">\n" +
               "        <Say voice=\"" + voice + "\" language=\"" + language + "\">" + escapeXml(script) + "</Say>\n" +
               "    </Gather>\n" +
               "    <Say voice=\"" + voice + "\" language=\"" + language + "\">We did not receive any input. We will call back shortly. Goodbye.</Say>\n" +
               "    <Hangup/>\n" +
               "</Response>";
    }

    @Override
    public String generateResponseTwiML(String digit, DoseEvent doseEvent) {
        return simulatedService.generateResponseTwiML(digit, doseEvent);
    }

    @Override
    public String getSpokenScript(DoseEvent doseEvent) {
        return simulatedService.getSpokenScript(doseEvent);
    }

    private String escapeXml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&apos;");
    }
}
