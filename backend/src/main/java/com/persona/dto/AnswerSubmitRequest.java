package com.persona.dto;

import javax.validation.constraints.NotNull;
import java.util.Map;

public class AnswerSubmitRequest {
    @NotNull
    private Map<Long, Integer> answers;

    private String nickname;

    public Map<Long, Integer> getAnswers() { return answers; }
    public void setAnswers(Map<Long, Integer> answers) { this.answers = answers; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
}
