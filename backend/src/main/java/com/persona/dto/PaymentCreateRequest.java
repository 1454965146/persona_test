package com.persona.dto;

import javax.validation.constraints.NotBlank;

public class PaymentCreateRequest {
    @NotBlank
    private String reportCode;

    public String getReportCode() { return reportCode; }
    public void setReportCode(String reportCode) { this.reportCode = reportCode; }
}
