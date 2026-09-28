package com.carevoice.service;

import com.carevoice.entity.DoseEvent;
import com.carevoice.entity.Medication;
import com.carevoice.entity.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("simulatedTelephonyService")
public class SimulatedTelephonyService implements TelephonyService {

    private static final Logger log = LoggerFactory.getLogger(SimulatedTelephonyService.class);

    @Override
    public String initiateCall(DoseEvent doseEvent) {
        String callSid = "SIM-CALL-" + UUID.randomUUID().toString().substring(0, 8);
        Patient patient = doseEvent.getPatient();
        Medication medication = doseEvent.getMedicationSchedule().getMedication();

        log.info("[SIMULATED CALL] Outbound call placed to {} ({}) for dose {} (Medication: {}) | SID: {}",
                patient.getFullName(), patient.getPhoneNumber(), doseEvent.getId(), medication.getName(), callSid);
        log.info("[SIMULATED VOICE SCRIPT] {}", getSpokenScript(doseEvent));

        return callSid;
    }

    @Override
    public String generateReminderTwiML(DoseEvent doseEvent) {
        String script = getSpokenScript(doseEvent);
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
               "<Response>\n" +
               "    <Gather numDigits=\"1\" timeout=\"15\">\n" +
               "        <Say>" + escapeXml(script) + "</Say>\n" +
               "    </Gather>\n" +
               "    <Say>We did not receive your input. We will call you back shortly. Goodbye.</Say>\n" +
               "</Response>";
    }

    @Override
    public String generateResponseTwiML(String digit, DoseEvent doseEvent) {
        String message;
        if ("1".equals(digit)) {
            message = "Thank you. Your medication has been recorded as taken. Stay healthy and have a wonderful day. Goodbye.";
        } else if ("2".equals(digit)) {
            message = "We have recorded that you have not taken your dose. We have notified your caregiver to check in. Please rest and stay safe. Goodbye.";
        } else {
            message = "Invalid selection. Please contact your caregiver if you need assistance. Goodbye.";
        }

        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
               "<Response>\n" +
               "    <Say>" + escapeXml(message) + "</Say>\n" +
               "    <Hangup/>\n" +
               "</Response>";
    }

    @Override
    public String getSpokenScript(DoseEvent doseEvent) {
        Patient patient = doseEvent.getPatient();
        Medication medication = doseEvent.getMedicationSchedule().getMedication();
        String lang = patient.getPreferredLanguage() != null ? patient.getPreferredLanguage() : "en-US";

        if (lang.startsWith("es")) {
            return String.format("Hola %s. Este es su recordatorio de CareVoice. Es hora de tomar su medicina: %s, dosis %s. %s. " +
                                 "Por favor presione 1 después de tomar su medicina. Presione 2 si aún no la ha tomado.",
                    patient.getFullName(),
                    medication.getName(),
                    medication.getDosage(),
                    medication.getConfirmedInstruction());
        } else if (lang.startsWith("hi")) {
            return String.format("Namaste %s ji. Yeh aapka CareVoice dawai reminder hai. Aapki dawai ka samay ho gaya hai: %s, matra %s. %s. " +
                                 "Dawai lene ke baad kripya 1 dabayein. Agar dawai nahi li hai toh 2 dabayein.",
                    patient.getFullName(),
                    medication.getName(),
                    medication.getDosage(),
                    medication.getConfirmedInstruction());
        }

        // Default English
        return String.format("Hello %s. This is your CareVoice medication reminder. It is time for your scheduled medicine: %s, dosage %s. %s. " +
                             "Please press 1 after taking your medicine. Press 2 if you have not taken it yet.",
                patient.getFullName(),
                medication.getName(),
                medication.getDosage(),
                medication.getConfirmedInstruction());
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
