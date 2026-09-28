package com.carevoice.service;

import com.carevoice.entity.DoseEvent;

public interface TelephonyService {

    /**
     * Initiates an outbound telephone call to the elderly patient.
     * @param doseEvent the dose event triggering this reminder call
     * @return call identifier / SID
     */
    String initiateCall(DoseEvent doseEvent);

    /**
     * Generates TwiML / voice script instructing the patient to press 1 (Taken) or 2 (Not Taken).
     * @param doseEvent the dose event
     * @return TwiML XML or speech script
     */
    String generateReminderTwiML(DoseEvent doseEvent);

    /**
     * Generates TwiML / voice response after the patient presses a keypad digit.
     * @param digit "1" or "2"
     * @param doseEvent the dose event
     * @return response TwiML XML or speech script
     */
    String generateResponseTwiML(String digit, DoseEvent doseEvent);

    /**
     * Generates a spoken prompt script for the given dose event in the patient's language.
     * @param doseEvent the dose event
     * @return text script
     */
    String getSpokenScript(DoseEvent doseEvent);
}
