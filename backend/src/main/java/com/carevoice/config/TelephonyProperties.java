package com.carevoice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "carevoice.telephony")
public class TelephonyProperties {

    /**
     * Provider mode: "simulator" (default for offline demo/dev) or "twilio" (for live outbound cellular calls)
     */
    private String provider = "simulator";

    private String accountSid = "";
    private String authToken = "";
    private String fromNumber = "+15005550006"; // Twilio test number default
    private String webhookBaseUrl = "http://localhost:8080";
    private int maxRetries = 2;
    private int retryDelayMinutes = 10;

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getAccountSid() { return accountSid; }
    public void setAccountSid(String accountSid) { this.accountSid = accountSid; }
    public String getAuthToken() { return authToken; }
    public void setAuthToken(String authToken) { this.authToken = authToken; }
    public String getFromNumber() { return fromNumber; }
    public void setFromNumber(String fromNumber) { this.fromNumber = fromNumber; }
    public String getWebhookBaseUrl() { return webhookBaseUrl; }
    public void setWebhookBaseUrl(String webhookBaseUrl) { this.webhookBaseUrl = webhookBaseUrl; }
    public int getMaxRetries() { return maxRetries; }
    public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }
    public int getRetryDelayMinutes() { return retryDelayMinutes; }
    public void setRetryDelayMinutes(int retryDelayMinutes) { this.retryDelayMinutes = retryDelayMinutes; }
}
