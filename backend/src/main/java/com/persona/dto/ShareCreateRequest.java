package com.persona.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

public class ShareCreateRequest {
    @NotBlank
    private String reportCode;

    @NotBlank
    @Pattern(regexp = "BROTHER|COUPLE|FRIEND|COLLEAGUE|FAMILY", message = "关系类型不合法")
    private String relationshipType;

    private Boolean allowInviteeView;

    public String getReportCode() { return reportCode; }
    public void setReportCode(String reportCode) { this.reportCode = reportCode; }
    public String getRelationshipType() { return relationshipType; }
    public void setRelationshipType(String relationshipType) { this.relationshipType = relationshipType; }
    public Boolean getAllowInviteeView() { return allowInviteeView; }
    public void setAllowInviteeView(Boolean allowInviteeView) { this.allowInviteeView = allowInviteeView; }
}
