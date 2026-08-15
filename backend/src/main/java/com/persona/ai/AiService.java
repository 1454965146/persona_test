package com.persona.ai;

import java.util.Map;

public interface AiService {
    String generateReport(Map<String, Double> dimensionScores, String nickname);
    String generateComparison(
        Map<String, Double> scoresA, String typeA, String nameA,
        Map<String, Double> scoresB, String typeB, String nameB,
        String relationshipType);
}
