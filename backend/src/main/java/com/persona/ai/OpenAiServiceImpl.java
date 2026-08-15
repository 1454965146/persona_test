package com.persona.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.persona.config.AiConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.*;

@Service
public class OpenAiServiceImpl implements AiService {
    private static final Logger log = LoggerFactory.getLogger(OpenAiServiceImpl.class);
    private final RestTemplate restTemplate;
    private final AiConfig aiConfig;
    private final ObjectMapper objectMapper;

    public OpenAiServiceImpl(RestTemplate restTemplate, AiConfig aiConfig, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.aiConfig = aiConfig;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        log.info("AI Service initialized: endpoint={}, model={}", aiConfig.getEndpoint(), aiConfig.getModel());
    }

    @Override
    public String generateReport(Map<String, Double> dimensionScores, String nickname) {
        return callAi(buildReportSystemPrompt(), buildReportUserPrompt(dimensionScores, nickname));
    }

    @Override
    public String generateComparison(
            Map<String, Double> scoresA, String typeA, String nameA,
            Map<String, Double> scoresB, String typeB, String nameB,
            String relationshipType) {
        return callAi(buildCompareSystemPrompt(),
                buildCompareUserPrompt(scoresA, typeA, nameA, scoresB, typeB, nameB, relationshipType));
    }

    private String callAi(String systemPrompt, String userPrompt) {
        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", aiConfig.getModel());
        List<Map<String, String>> messages = new ArrayList<>();

        Map<String, String> sysMsg = new LinkedHashMap<>();
        sysMsg.put("role", "system");
        sysMsg.put("content", systemPrompt);
        messages.add(sysMsg);

        Map<String, String> userMsg = new LinkedHashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", userPrompt);
        messages.add(userMsg);

        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 2000);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(aiConfig.getApiKey());

            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);
            String url = aiConfig.getEndpoint() + "/chat/completions";

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
            Map<String, Object> body = response.getBody();
            if (body != null && body.containsKey("choices")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> choices = (List<Map<String, Object>>) body.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return (String) message.get("content");
                }
            }
            throw new RuntimeException("AI response has no valid choices");
        } catch (Exception e) {
            log.error("AI call failed: {}", e.getMessage());
            throw new RuntimeException("AI", e);
        }
    }

    // --- Prompt builders (使用 replace 防注入) ---

    private String buildReportSystemPrompt() {
        return "你是一位资深性格分析师。用中文回复，严格遵循下方Markdown格式输出。";
    }

    private String buildReportUserPrompt(Map<String, Double> scores, String nickname) {
        String safe = safe(nickname);
        String mbti = mbtiType(scores);
        String tpl = "请为用户「{name}」生成性格报告。\n\n"
                + "## 测试数据\n- MBTI推测：{mbti}\n"
                + "- 外向/内向(EI)：{ei}/5（>3偏外向，<3偏内向）\n"
                + "- 感觉/直觉(SN)：{sn}/5（>3偏感觉，<3偏直觉）\n"
                + "- 思考/情感(TF)：{tf}/5（>3偏思考，<3偏情感）\n"
                + "- 判断/感知(JP)：{jp}/5（>3偏判断，<3偏感知）\n"
                + "- 开放度(EXTRA)：{extra}/5\n\n"
                + "## 输出格式（1000-1300字）\n"
                + "### 🎭 性格本色：{name}\n> 一句话概括（10-20字）\n\n"
                + "### 📊 维度解读\n各维度1-2句解读\n\n"
                + "### 💎 性格本色洞察\n2-3段深入分析\n\n"
                + "### 🌱 优势与成长空间\n- **优势**：3-4条\n- **成长空间**：2-3条\n\n"
                + "### 🤝 社交关系画像\n1段描述{name}的社交角色";
        return tpl.replace("{name}", safe).replace("{mbti}", mbti)
                .replace("{ei}", fmt(scores.get("EI")))
                .replace("{sn}", fmt(scores.get("SN")))
                .replace("{tf}", fmt(scores.get("TF")))
                .replace("{jp}", fmt(scores.get("JP")))
                .replace("{extra}", fmt(scores.get("EXTRA")));
    }

    private String buildCompareSystemPrompt() {
        return "你是资深关系分析师。用中文回复，严格遵循下方Markdown格式。";
    }

    private String buildCompareUserPrompt(
            Map<String, Double> scoresA, String typeA, String nameA,
            Map<String, Double> scoresB, String typeB, String nameB,
            String relationshipType) {
        String rel = relLabel(relationshipType);
        String relationExtra = "COUPLE".equals(relationshipType)
                ? "\n- 如为情侣/好感关系，请增加一段追求建议或矛盾预防建议"
                : "";
        String tpl = "请分析「{nameA}」和「{nameB}」的{rel}。\n\n"
                + "## {nameA}的数据\n- 类型：{typeA} | EI:{aEI} SN:{aSN} TF:{aTF} JP:{aJP} EXTRA:{aEX}\n\n"
                + "## {nameB}的数据\n- 类型：{typeB} | EI:{bEI} SN:{bSN} TF:{bTF} JP:{bJP} EXTRA:{bEX}\n\n"
                + "## 输出格式（800-1100字）\n"
                + "### 💞 {nameA}与{nameB}的{rel}分析\n> 一句话描述\n\n"
                + "### 👥 双方性格速写\n| | {nameA} | {nameB} |\n|---|---|---|\n| 类型 | {typeA} | {typeB} |\n\n"
                + "### 🔮 维度契合分析\n各维度2-3句\n\n"
                + "### 📈 综合评分\n- **{rel}**：X/10\n\n"
                + "### 💡 相处指南\n3-4条建议\n\n"
                + "### ⚠️ 注意地带\n2条摩擦点和预防建议"
                + relationExtra;
        return tpl.replace("{nameA}", safe(nameA)).replace("{nameB}", safe(nameB))
                .replace("{typeA}", typeA).replace("{typeB}", typeB)
                .replace("{rel}", rel)
                .replace("{aEI}", fmt(scoresA.get("EI"))).replace("{aSN}", fmt(scoresA.get("SN")))
                .replace("{aTF}", fmt(scoresA.get("TF"))).replace("{aJP}", fmt(scoresA.get("JP")))
                .replace("{aEX}", fmt(scoresA.get("EXTRA")))
                .replace("{bEI}", fmt(scoresB.get("EI"))).replace("{bSN}", fmt(scoresB.get("SN")))
                .replace("{bTF}", fmt(scoresB.get("TF"))).replace("{bJP}", fmt(scoresB.get("JP")))
                .replace("{bEX}", fmt(scoresB.get("EXTRA")));
    }

    private String safe(String s) { return s == null ? "" : s.replace("{", "(").replace("}", ")"); }
    private String fmt(Double d) { return d == null ? "3.0" : String.format("%.1f", d); }
    private String mbtiType(Map<String, Double> s) {
        return (sc(s,"EI")>=3?"E":"I")+(sc(s,"SN")>=3?"S":"N")+(sc(s,"TF")>=3?"T":"F")+(sc(s,"JP")>=3?"J":"P");
    }
    private double sc(Map<String, Double> s, String k) { return s.getOrDefault(k, 3.0); }
    private String relLabel(String t) {
        switch(t){case"BROTHER":return"兄弟默契度";case"COUPLE":return"情侣契合度";case"FRIEND":return"朋友合拍度";case"COLLEAGUE":return"同事协作度";case"FAMILY":return"亲子亲密度";default:return"关系分析";}
    }
}
