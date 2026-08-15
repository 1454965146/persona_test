package com.persona.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.persona.model.Question;
import com.persona.model.TestSession;
import com.persona.model.User;
import com.persona.repository.QuestionRepository;
import com.persona.repository.TestSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TestService {
    private static final Logger log = LoggerFactory.getLogger(TestService.class);
    private static final String[] DIMS = {"EI", "SN", "TF", "JP", "EXTRA"};
    private static final int SCORE_MIN = 1;
    private static final int SCORE_MAX = 5;

    private final QuestionRepository questionRepository;
    private final TestSessionRepository sessionRepository;
    private final ObjectMapper objectMapper;

    public TestService(QuestionRepository questionRepository, TestSessionRepository sessionRepository, ObjectMapper objectMapper) {
        this.questionRepository = questionRepository;
        this.sessionRepository = sessionRepository;
        this.objectMapper = objectMapper;
    }

    public List<Map<String, Object>> getQuestions() {
        List<Question> questions = questionRepository.findAllByOrderBySortOrderAsc();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Question q : questions) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", q.getId());
            item.put("dimension", q.getDimension());
            item.put("text", q.getQuestionText());
            result.add(item);
        }
        Collections.shuffle(result);
        return result;
    }

    public Map<String, Object> submitAnswers(Map<Long, Integer> answers, User user) {
        // 校验答案有效性
        if (answers == null || answers.isEmpty()) {
            throw new RuntimeException("答案不能为空");
        }
        for (Map.Entry<Long, Integer> entry : answers.entrySet()) {
            int val = entry.getValue();
            if (val < SCORE_MIN || val > SCORE_MAX) {
                throw new RuntimeException("答案值无效: 题目" + entry.getKey() + "的分值" + val + "不在1-5范围内");
            }
        }

        Map<Long, Question> questionMap = questionRepository.findAll()
                .stream().collect(Collectors.toMap(Question::getId, q -> q));
        if (answers.size() != questionMap.size() || !questionMap.keySet().containsAll(answers.keySet())) {
            throw new RuntimeException("请完成全部题目后再提交");
        }

        // 计算各维度加权分 (1-5 反向计分时 = 6 - answer)
        Map<String, Double> dimSum = new LinkedHashMap<>();
        Map<String, Integer> dimCount = new LinkedHashMap<>();
        for (String d : DIMS) {
            dimSum.put(d, 0.0);
            dimCount.put(d, 0);
        }

        for (Map.Entry<Long, Integer> entry : answers.entrySet()) {
            Question q = questionMap.get(entry.getKey());
            if (q == null) {
                throw new RuntimeException("提交了未知题目ID: " + entry.getKey());
            }
            int rawScore = entry.getValue();
            int adjustedScore = q.getIsPositive() ? rawScore : (SCORE_MAX + 1 - rawScore);
            dimSum.put(q.getDimension(), dimSum.get(q.getDimension()) + adjustedScore);
            dimCount.put(q.getDimension(), dimCount.get(q.getDimension()) + 1);
        }

        Map<String, Double> dimensionScores = new LinkedHashMap<>();
        for (String d : DIMS) {
            int cnt = dimCount.get(d);
            dimensionScores.put(d, cnt > 0 ? Math.round(dimSum.get(d) / cnt * 10.0) / 10.0 : 3.0);
        }

        TestSession session = new TestSession();
        session.setSessionCode(UUID.randomUUID().toString().replace("-", ""));
        try {
            session.setAnswersJson(objectMapper.writeValueAsString(answers));
            session.setDimensionScoresJson(objectMapper.writeValueAsString(dimensionScores));
        } catch (Exception e) {
            throw new RuntimeException("JSON序列化失败", e);
        }
        session.setStatus("COMPLETED");
        session.setUser(user);
        session = sessionRepository.save(session);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("sessionCode", session.getSessionCode());
        result.put("dimensionScores", dimensionScores);
        return result;
    }
}
