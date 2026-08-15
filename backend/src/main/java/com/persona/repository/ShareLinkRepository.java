package com.persona.repository;

import com.persona.model.ShareLink;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ShareLinkRepository extends JpaRepository<ShareLink, Long> {
    Optional<ShareLink> findByShareCode(String shareCode);
    List<ShareLink> findByInviterReportId(Long reportId);
    List<ShareLink> findByInviteeReportId(Long reportId);
    List<ShareLink> findByInviterReportIdIn(List<Long> reportIds);
    List<ShareLink> findByInviteeReportIdIn(List<Long> reportIds);
}
