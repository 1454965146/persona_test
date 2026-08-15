package com.persona.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

public class CompareGenerateRequest {
    @NotBlank
    private String reportCodeA;

    @NotBlank
    private String reportCodeB;

    @NotBlank
    @Pattern(regexp = "BROTHER|COUPLE|FRIEND|COLLEAGUE|FAMILY", message = "关系类型不合法")
    private String relationshipType;

    public String getReportCodeA() { return reportCodeA; }
    public void setReportCodeA(String reportCodeA) { this.reportCodeA = reportCodeA; }
    public String getReportCodeB() { return reportCodeB; }
    public void setReportCodeB(String reportCodeB) { this.reportCodeB = reportCodeB; }
    public String getRelationshipType() { return relationshipType; }
    public void setRelationshipType(String relationshipType) { this.relationshipType = relationshipType; }
}
