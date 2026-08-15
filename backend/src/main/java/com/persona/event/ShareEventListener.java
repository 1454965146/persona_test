package com.persona.event;

import com.persona.service.CompareService;
import com.persona.event.ComparisonRetryEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ShareEventListener {
    private static final Logger log = LoggerFactory.getLogger(ShareEventListener.class);
    private final CompareService compareService;

    public ShareEventListener(CompareService compareService) {
        this.compareService = compareService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("comparisonExecutor")
    public void onShareCompleted(ShareCompletedEvent event) {
        try {
            compareService.completeComparison(event.getComparisonId());
            log.info("自动生成对比完成: share={}", event.getShareCode());
        } catch (Exception e) {
            log.error("自动生成对比失败: share={}, {}", event.getShareCode(), e.getMessage());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("comparisonExecutor")
    public void onComparisonRetry(ComparisonRetryEvent event) {
        try {
            compareService.completeComparison(event.getComparisonId());
            log.info("重新生成对比完成: comparisonId={}", event.getComparisonId());
        } catch (Exception e) {
            log.error("重新生成对比失败: comparisonId={}, {}", event.getComparisonId(), e.getMessage());
        }
    }
}
