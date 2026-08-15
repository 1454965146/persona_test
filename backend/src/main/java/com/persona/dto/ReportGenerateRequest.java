package com.persona.dto;

import javax.validation.constraints.NotBlank;

public class ReportGenerateRequest {
    @NotBlank
    private String sessionCode;

    @NotBlank
    private String nickname;

    public String getSessionCode() { return sessionCode; }
    public void setSessionCode(String sessionCode) { this.sessionCode = sessionCode; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
}
