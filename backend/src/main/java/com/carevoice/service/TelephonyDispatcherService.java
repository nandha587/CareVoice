package com.carevoice.service;

import com.carevoice.config.TelephonyProperties;
import com.carevoice.entity.DoseEvent;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class TelephonyDispatcherService implements TelephonyService {

    private final TelephonyProperties properties;
    private final TwilioTelephonyService twilioService;
    private final SimulatedTelephonyService simulatedService;

    public TelephonyDispatcherService(TelephonyProperties properties,
                                     TwilioTelephonyService twilioService,
                                     SimulatedTelephonyService simulatedService) {
        this.properties = properties;
        this.twilioService = twilioService;
        this.simulatedService = simulatedService;
    }

    private TelephonyService getActiveService() {
        if ("twilio".equalsIgnoreCase(properties.getProvider())) {
            return twilioService;
        }
        return simulatedService;
    }

    @Override
    public String initiateCall(DoseEvent doseEvent) {
        return getActiveService().initiateCall(doseEvent);
    }

    @Override
    public String generateReminderTwiML(DoseEvent doseEvent) {
        return getActiveService().generateReminderTwiML(doseEvent);
    }

    @Override
    public String generateResponseTwiML(String digit, DoseEvent doseEvent) {
        return getActiveService().generateResponseTwiML(digit, doseEvent);
    }

    @Override
    public String getSpokenScript(DoseEvent doseEvent) {
        return getActiveService().getSpokenScript(doseEvent);
    }
}
