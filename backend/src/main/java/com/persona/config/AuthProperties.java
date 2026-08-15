package com.persona.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "auth")
public class AuthProperties {
    private int tokenTtlHours = 168;
    private boolean devLoginEnabled = true;

    public int getTokenTtlHours() { return tokenTtlHours; }
    public void setTokenTtlHours(int tokenTtlHours) { this.tokenTtlHours = tokenTtlHours; }
    public boolean isDevLoginEnabled() { return devLoginEnabled; }
    public void setDevLoginEnabled(boolean devLoginEnabled) { this.devLoginEnabled = devLoginEnabled; }
}
