package com.persona.service;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DimensionUtilTest {
    @Test
    void computeTypeUsesThreeAsBoundary() {
        Map<String, Double> scores = new HashMap<>();
        scores.put("EI", 4.0);
        scores.put("SN", 2.0);
        scores.put("TF", 3.0);
        scores.put("JP", 1.0);
        assertEquals("ENTP", DimensionUtil.computeType(scores));
    }

    @Test
    void sanitizeRemovesMarkupAndTemplateCharacters() {
        assertEquals("&lt;A&gt; (&quot;)", DimensionUtil.sanitize("<A> {\"}"));
    }
}
