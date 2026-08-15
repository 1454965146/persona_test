package com.persona.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.persona.model.Question;
import com.persona.model.TestSession;
import com.persona.model.User;
import com.persona.repository.QuestionRepository;
import com.persona.repository.TestSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestServiceTest {
    @Mock
    private QuestionRepository questionRepository;
    @Mock
    private TestSessionRepository sessionRepository;

    private TestService testService;

    @BeforeEach
    void setUp() {
        testService = new TestService(questionRepository, sessionRepository, new ObjectMapper());
    }

    @Test
    void submitAnswersScoresPositiveAndReverseQuestions() {
        Question positive = new Question();
        positive.setId(1L);
        positive.setDimension("EI");
        positive.setIsPositive(true);

        Question reverse = new Question();
        reverse.setId(2L);
        reverse.setDimension("EI");
        reverse.setIsPositive(false);

        when(questionRepository.findAll()).thenReturn(Arrays.asList(positive, reverse));
        when(sessionRepository.save(any(TestSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User user = new User();
        user.setId(7L);
        user.setNickname("tester");

        Map<Long, Integer> answers = new HashMap<>();
        answers.put(1L, 5);
        answers.put(2L, 4);

        Map<String, Object> result = testService.submitAnswers(answers, user);
        @SuppressWarnings("unchecked")
        Map<String, Double> scores = (Map<String, Double>) result.get("dimensionScores");

        assertEquals(3.5, scores.get("EI"), 0.01);
        ArgumentCaptor<TestSession> captor = ArgumentCaptor.forClass(TestSession.class);
        verify(sessionRepository).save(captor.capture());
        assertSame(user, captor.getValue().getUser());
    }
}
