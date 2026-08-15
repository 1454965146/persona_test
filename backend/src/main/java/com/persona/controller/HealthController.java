package com.persona.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class HealthController {
    @GetMapping("/api/health")
    public Map<String, Object> health() {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("status", "UP");
        r.put("timestamp", System.currentTimeMillis());
        return r;
    }
}