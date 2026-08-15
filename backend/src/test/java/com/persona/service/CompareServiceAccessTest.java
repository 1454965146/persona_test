package com.persona.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.persona.ai.AiService;
import com.persona.ai.FallbackReportService;
import com.persona.model.Comparison;
import com.persona.model.Report;
import com.persona.model.ShareLink;
import com.persona.model.User;
import com.persona.repository.ComparisonRepository;
import com.persona.repository.ReportRepository;
import com.persona.repository.ShareLinkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompareServiceAccessTest {
    @Mock private ReportRepository reportRepository;
    @Mock private ComparisonRepository comparisonRepository;
    @Mock private ShareLinkRepository shareLinkRepository;
    @Mock private AiService aiService;
    @Mock private FallbackReportService fallback;
    @Mock private ApplicationEventPublisher eventPublisher;

    private CompareService compareService;
    private User owner;
    private User invitee;
    private Comparison comparison;

    @BeforeEach
    void setUp() {
        compareService = new CompareService(
                reportRepository, comparisonRepository, shareLinkRepository,
                aiService, fallback, new ObjectMapper(), eventPublisher);

        owner = new User();
        owner.setId(1L);
        invitee = new User();
        invitee.setId(2L);

        Report reportA = new Report();
        reportA.setId(10L);
        reportA.setReportCode("report-a");
        reportA.setUser(owner);
        reportA.setNickname("Owner");
        reportA.setPersonalityType("ENFP");
        reportA.setDimensionScoresJson("{\"EI\":4.0,\"SN\":3.0,\"TF\":2.5,\"JP\":3.5,\"EXTRA\":4.5}");

        Report reportB = new Report();
        reportB.setId(20L);
        reportB.setReportCode("report-b");
        reportB.setUser(invitee);
        reportB.setNickname("Invitee");
        reportB.setPersonalityType("ISTJ");
        reportB.setDimensionScoresJson("{\"EI\":2.0,\"SN\":4.0,\"TF\":4.5,\"JP\":4.0,\"EXTRA\":2.5}");

        comparison = new Comparison();
        comparison.setId(100L);
        comparison.setReportA(reportA);
        comparison.setReportB(reportB);
        comparison.setOwnerUser(owner);
        comparison.setRelationshipType("COUPLE");
        comparison.setStatus("COMPLETED");
        comparison.setAnalysisContent("relationship analysis");

        when(comparisonRepository.findById(100L)).thenReturn(Optional.of(comparison));
    }

    @Test
    void inviteeCanReadComparisonWhenAllowedButRelationshipIsHidden() {
        ShareLink link = new ShareLink();
        link.setVisibleToInvitee(true);
        when(shareLinkRepository
                .findFirstByInviterReportIdAndInviteeReportIdAndRelationshipType(
                        10L, 20L, "COUPLE"))
                .thenReturn(Optional.of(link));

        Map<String, Object> result = compareService.getComparison(100L, invitee);

        assertFalse(result.containsKey("relationshipType"));
    }

    @Test
    void inviteeCannotReadComparisonWhenNotAllowed() {
        ShareLink link = new ShareLink();
        link.setVisibleToInvitee(false);
        when(shareLinkRepository
                .findFirstByInviterReportIdAndInviteeReportIdAndRelationshipType(
                        10L, 20L, "COUPLE"))
                .thenReturn(Optional.of(link));

        assertThrows(RuntimeException.class, () -> compareService.getComparison(100L, invitee));
    }
}
