package com.persona.config;

import com.persona.model.Question;
import com.persona.repository.QuestionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private final QuestionRepository questionRepository;

    public DataInitializer(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    @Override
    public void run(String... args) {
        if (questionRepository.count() > 0) {
            log.info("题库已存在，跳过初始化。共 {} 题", questionRepository.count());
            return;
        }
        log.info("开始初始化题库...");
        String[][] qs = {
            {"EI","在聚会中，我倾向于结识新朋友而不是只和熟人聊天","1"},
            {"EI","独自一人时我更容易恢复精力","0"},
            {"EI","我乐于成为众人关注的焦点","1"},
            {"EI","与一群人相处太久会让我感到疲惫","0"},
            {"EI","我倾向于先思考再开口说话","0"},
            {"EI","我主动发起社交邀约的频率很高","1"},
            {"EI","在群体讨论中我通常是发言比较多的一方","1"},
            {"EI","比起线下见面我更习惯用文字交流","0"},
            {"EI","一个人度过周末对我来说完全不是问题","0"},
            {"EI","我喜欢用说话来整理自己的思路","1"},
            {"SN","我更信任亲身经历过的事实而非理论推断","1"},
            {"SN","我经常沉浸在对未来的想象和可能性之中","0"},
            {"SN","做事时我习惯遵循已有的流程和步骤","1"},
            {"SN","比起具体细节我更关注整体画面和抽象概念","0"},
            {"SN","我不太喜欢天马行空的假设性问题","1"},
            {"SN","我总是能发现事物之间隐藏的关联","0"},
            {"SN","做决定时我更依赖数据和实际案例","1"},
            {"SN","对我来说灵感和直觉比经验更有指导意义","0"},
            {"SN","我对符号和隐喻的理解常常慢半拍","1"},
            {"SN","我喜欢探索新的做事方式，即使旧方法也能用","0"},
            {"TF","做决定时我会优先考虑逻辑和效率","1"},
            {"TF","我很容易感受到周围人的情绪变化","0"},
            {"TF","我认为指出别人的错误比维护和谐更重要","1"},
            {"TF","做重要决定时我会把对人际关系的影响放在首位","0"},
            {"TF","我更看重一个人的能力而非人品","1"},
            {"TF","当别人向我倾诉时我倾向于提供安慰而非解决方案","0"},
            {"TF","我认为原则比情面重要","1"},
            {"TF","看到他人难过时我也容易感同身受","0"},
            {"TF","我对感性的影视作品不太感冒","1"},
            {"TF","我不太能接受当面批评别人,即使是为对方好","0"},
            {"JP","我喜欢提前做好详细的计划并按计划执行","1"},
            {"JP","我享受随遇而安的自由,不太喜欢被条条框框约束","0"},
            {"JP","我的桌面和工作空间通常保持整洁有序","1"},
            {"JP","截止日期前临时赶工对我来说是常态","0"},
            {"JP","做出决定后我很少回头纠结","1"},
            {"JP","我习惯同时进行多个项目,在它们之间灵活切换","0"},
            {"JP","不确定的事情让我感到焦虑","1"},
            {"JP","我喜欢保留多种选择而非过早锁定一个方案","0"},
            {"JP","每天按固定时间表生活让我感到踏实","1"},
            {"JP","我对突发的变化接受度很高","0"},
            {"EXTRA","面对重大变故时我的情绪能较快恢复平稳","1"},
            {"EXTRA","我对他人的批评非常敏感,容易耿耿于怀","0"},
            {"EXTRA","我乐于尝试完全陌生的领域,即使可能失败","1"},
            {"EXTRA","在陌生环境中我需要较长时间才能放松","0"},
            {"EXTRA","我对自己的优势和局限有清晰的认知","1"},
            {"EXTRA","我经常忍不住拿自己和别人比较","0"},
            {"EXTRA","面对冲突时我更倾向于直接沟通而非回避","1"},
            {"EXTRA","我觉得向别人袒露脆弱是一件很难的事","0"},
            {"EXTRA","我对与自己截然不同的生活方式抱有好奇心","1"},
            {"EXTRA","我容易因为小事陷入反复纠结","0"},
        };
        List<Question> batch = new ArrayList<>();
        int order = 0;
        for (String[] q : qs) {
            Question question = new Question();
            question.setDimension(q[0]);
            question.setQuestionText(q[1]);
            question.setIsPositive("1".equals(q[2]));
            question.setSortOrder(++order);
            batch.add(question);
        }
        questionRepository.saveAll(batch);
        log.info("题库初始化完成，共 {} 题", questionRepository.count());
    }
}