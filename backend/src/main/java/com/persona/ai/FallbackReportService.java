package com.persona.ai;

import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class FallbackReportService {

    public String generateReport(Map<String, Double> scores, String nickname) {
        String mbti = computeType(scores);
        StringBuilder sb = new StringBuilder();
        sb.append("### 🎭 性格本色：").append(nickname).append("\n");
        sb.append("> ").append(getTypeDesc(mbti)).append("\n\n");
        sb.append("### 📊 维度解读\n");
        String[][] dims = {{"EI","外向/内向"},{"SN","感觉/直觉"},{"TF","思考/情感"},{"JP","判断/感知"},{"EXTRA","开放度"}};
        for (String[] d : dims) sb.append(formatDim(d[1], sc(scores,d[0])));
        sb.append("\n### 💎 性格本色洞察\n").append(getInsight(mbti, nickname)).append("\n");
        sb.append("### 🌱 优势与成长空间\n");
        for (String s : getStrengths(mbti)) sb.append("- ").append(s).append("\n");
        sb.append("\n### 🤝 社交关系画像\n").append(getSocial(mbti, nickname));
        sb.append("\n\n---\n> ⚡ 本报告由本地模板引擎生成");
        return sb.toString();
    }

    public String generateComparison(Map<String, Double> sA, String tA, String nA,
                                     Map<String, Double> sB, String tB, String nB, String relType) {
        StringBuilder sb = new StringBuilder();
        String rl = relLabel(relType);
        sb.append("### 💞 ").append(nA).append("与").append(nB).append("的").append(rl).append("分析\n");
        sb.append("> ").append(relSummary(relType, nA, nB)).append("\n\n");

        sb.append("### 👥 双方性格速写\n| | ").append(nA).append(" | ").append(nB).append(" |\n|---|---|---|\n");
        sb.append("| 类型 | ").append(tA).append(" | ").append(tB).append(" |\n");
        sb.append("| 风格 | ").append(getTypeDesc(tA)).append(" | ").append(getTypeDesc(tB)).append(" |\n\n");

        sb.append("### 🔮 维度契合分析\n");
        String[][] dims = {{"EI","外向/内向"},{"SN","感觉/直觉"},{"TF","思考/情感"},{"JP","判断/感知"},{"EXTRA","开放度"}};
        double match = 0;
        for (String[] d : dims) {
            double a = sc(sA,d[0]), b = sc(sB,d[0]);
            double diff = Math.abs(a-b);
            String st = diff < 1 ? "高度契合" : diff < 1.5 ? "良好互补" : "需要磨合";
            if (diff < 1.2) match++;
            sb.append("- **").append(d[1]).append("**→").append(st).append(" (").append(f(a)).append(" vs ").append(f(b)).append(")\n");
        }
        sb.append("\n### 📈 综合评分\n- **").append(rl).append("**：").append(Math.round(match/5*8+2)).append("/10\n\n");

        sb.append(getRelAdvice(relType, nA, nB, tA, tB, sA, sB));
        sb.append("\n\n---\n> ⚡ 本对比由本地模板引擎生成");
        return sb.toString();
    }

    private String getRelAdvice(String type, String na, String nb, String ta, String tb, Map<String,Double> sa, Map<String,Double> sb) {
        StringBuilder b = new StringBuilder();
        switch(type) {
            case "COUPLE": b.append(coupleAdvice(na,nb,ta,tb,sa,sb)); break;
            case "FRIEND": b.append(friendAdvice(na,nb)); break;
            case "BROTHER": b.append("### 🤜 兄弟关系\n真正的兄弟靠信任而非性格相似。坦诚沟通、在对方需要时挺身而出，是最好的相处之道。\n"); break;
            case "COLLEAGUE": b.append("### 💼 同事协作\n明确分工（一人创意一人执行）、重要决定留书面记录、尊重彼此的工作风格差异。\n"); break;
            case "FAMILY": b.append("### 👨‍👩‍👧 亲子关系\n接纳彼此的性格差异、创造固定的亲子仪式时间、倾听对方的情绪再回应问题。\n"); break;
            default: b.append("### 💡 相处指南\n").append(na).append("和").append(nb).append("请保持真诚沟通，珍惜彼此的关系。\n");
        }
        return b.toString();
    }

    private String coupleAdvice(String na, String nb, String ta, String tb, Map<String,Double> sa, Map<String,Double> sb) {
        StringBuilder b = new StringBuilder();
        b.append("### 💕 情侣关系分析\n\n#### 🌹 契合点\n");
        b.append("在亲密关系中，理解彼此需求是第一位的。");
        if (ta.charAt(0)==tb.charAt(0)) b.append("你们的社交能量同频，相处节奏自然舒适。");
        if (ta.charAt(2)==tb.charAt(2)) b.append("价值观判断高度一致，这是深层信任的基础。");
        b.append("\n\n#### ⚡ 可能的摩擦与化解\n");
        if (Math.abs(sc(sa,"EI")-sc(sb,"EI"))>1.5) b.append("社交能量差异明显时，约定独处与约会时间的边界，尊重彼此的能量节奏。");
        if (Math.abs(sc(sa,"TF")-sc(sb,"TF"))>1.5) b.append("分歧时先共情再讲理：「我理解你的感受，我们一起看看怎么解决」。");
        b.append("\n\n#### 💌 追求建议\n");
        if (tb.startsWith("I")) b.append(na).append("可从深度对话开始，在安静私密的环境中建立连接——").append(nb).append("作为内向型会被真诚而非热闹打动。\n");
        else b.append(na).append("可邀请").append(nb).append("参加有趣的社交活动，在互动中自然展现魅力。\n");
        if (tb.charAt(2)=='F') b.append(nb).append("重视情感体验，用细节展现用心比华丽言辞更重要。\n");
        else b.append(nb).append("偏向理性，用行动证明你的可靠和务实会赢得欣赏。\n");
        return b.toString();
    }

    private String friendAdvice(String na, String nb) {
        return "### 🤝 友谊关系\n\n#### 🎯 合拍之处\n朋友之间的默契在于理解与尊重。你们的性格差异让每次交流都充满新鲜感。\n\n#### 📋 相处建议\n- 定期安排高质量相处时间\n- 在对方需要空间时给予舒适边界\n- 做不评判的倾听者\n- 真实的友谊不需要刻意维护\n";
    }

    // Helpers
    private String computeType(Map<String,Double> s){return(sc(s,"EI")>=3?"E":"I")+(sc(s,"SN")>=3?"S":"N")+(sc(s,"TF")>=3?"T":"F")+(sc(s,"JP")>=3?"J":"P");}
    private double sc(Map<String,Double> s,String k){return s.getOrDefault(k,3.0);}
    private String f(double d){return String.format("%.1f",d);}
    private String formatDim(String l,double s){String t=s>3?"偏高":s<3?"偏低":"均衡";return "- **"+l+"**："+f(s)+"/5 ("+t+")\n";}
    private String getTypeDesc(String t){switch(t){case"INTJ":return"战略型思考者";case"INTP":return"逻辑型探索者";case"ENTJ":return"果断领导者";case"ENTP":return"机智辩论家";case"INFJ":return"理想主义倡导者";case"INFP":return"理想主义调停者";case"ENFJ":return"富有魅力引导者";case"ENFP":return"热情自由灵魂";case"ISTJ":return"务实组织者";case"ISFJ":return"专注保护者";case"ESTJ":return"高效管理者";case"ESFJ":return"热心照顾者";case"ISTP":return"冷静实干家";case"ISFP":return"温和艺术家";case"ESTP":return"灵活冒险家";case"ESFP":return"天生表演者";default:return"独特个性";}}
    private String getInsight(String m,String n){return n+"的核心驱动力来自"+(m.charAt(2)=='T'?"逻辑分析":"情感共鸣")+"，面对挑战时倾向"+(m.charAt(3)=='J'?"制定计划推进":"灵活随机应变")+"。";}
    private String[] getStrengths(String m){String e=m.substring(0,1),j=m.substring(3,4);if(e.equals("E")&&j.equals("J"))return new String[]{"出色的执行力","自然展露领导力","想法转化为方案"};if(e.equals("E"))return new String[]{"社交适应力和感染力","创意尝试新事物","乐观保持能量"};if(j.equals("J"))return new String[]{"专注力和持久力","严谨追求可靠结果","独立工作能力"};return new String[]{"深度思考分析力","丰富内心世界","善于倾听深度沟通"};}
    private String getSocial(String m,String n){return m.substring(0,1).equals("E")?n+"在社交圈中是能量输出者，乐于组织活动。":n+"看重关系质量，拥有少数深度联结。";}
    private String relLabel(String t){switch(t){case"BROTHER":return"兄弟默契度";case"COUPLE":return"情侣契合度";case"FRIEND":return"朋友合拍度";case"COLLEAGUE":return"同事协作度";case"FAMILY":return"亲子亲密度";default:return"关系分析";}}
    private String relSummary(String t,String na,String nb){switch(t){case"COUPLE":return na+"和"+nb+"的亲密关系——彼此接纳而非彼此改变";case"FRIEND":return na+"和"+nb+"的友谊——有的朋友是镜子，有的朋友是窗户";case"BROTHER":return na+"和"+nb+"的兄弟情谊——不在朝夕相处，而在关键时刻";case"COLLEAGUE":return na+"和"+nb+"的协作风格对比";case"FAMILY":return na+"和"+nb+"的亲子纽带——理解是连接的桥梁";default:return"每段关系都有独特的色彩";}}
}