package com.persona.config;

import com.persona.repository.AuthTokenRepository;
import com.persona.repository.ShareLinkRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class MaintenanceTask {
    private static final Logger log = LoggerFactory.getLogger(MaintenanceTask.class);

    private final AuthTokenRepository authTokenRepository;
    private final ShareLinkRepository shareLinkRepository;

    public MaintenanceTask(AuthTokenRepository authTokenRepository, ShareLinkRepository shareLinkRepository) {
        this.authTokenRepository = authTokenRepository;
        this.shareLinkRepository = shareLinkRepository;
    }

    @Scheduled(fixedDelayString = "${app.maintenance.interval-ms:3600000}")
    @Transactional
    public void cleanupExpiredData() {
        LocalDateTime now = LocalDateTime.now();
        int tokens = authTokenRepository.deleteRevokedOrExpired(now);
        int links = shareLinkRepository.expireLinksBefore(now);
        if (tokens > 0 || links > 0) {
            log.info("定时清理完成: 过期/失效Token={}, 过期分享链接={}", tokens, links);
        }
    }
}
