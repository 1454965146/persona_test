package com.persona.event;

import org.springframework.context.ApplicationEvent;

public class ComparisonRetryEvent extends ApplicationEvent {
    private final Long comparisonId;

    public ComparisonRetryEvent(Object source, Long comparisonId) {
        super(source);
        this.comparisonId = comparisonId;
    }

    public Long getComparisonId() { return comparisonId; }
}
