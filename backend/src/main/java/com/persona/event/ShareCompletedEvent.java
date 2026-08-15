package com.persona.event;

import org.springframework.context.ApplicationEvent;

public class ShareCompletedEvent extends ApplicationEvent {
    private final Long comparisonId;
    private final String shareCode;

    public ShareCompletedEvent(Object source, Long comparisonId, String shareCode) {
        super(source);
        this.comparisonId = comparisonId;
        this.shareCode = shareCode;
    }
    public Long getComparisonId() { return comparisonId; }
    public String getShareCode() { return shareCode; }
}
