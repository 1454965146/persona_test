package com.persona.service;

import java.util.Map;

public class DimensionUtil {
    public static String computeType(Map<String, Double> scores) {
        StringBuilder sb = new StringBuilder();
        sb.append(sc(scores, "EI") >= 3.0 ? "E" : "I");
        sb.append(sc(scores, "SN") >= 3.0 ? "S" : "N");
        sb.append(sc(scores, "TF") >= 3.0 ? "T" : "F");
        sb.append(sc(scores, "JP") >= 3.0 ? "J" : "P");
        return sb.toString();
    }
    public static double sc(Map<String, Double> s, String k) { return s.getOrDefault(k, 3.0); }
    public static String sanitize(String input) {
        if (input == null) return "";
        return input.replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;")
                .replace("{", "(").replace("}", ")");
    }
}