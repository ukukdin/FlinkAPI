package com.flinkapi.cep.engine;

import com.flinkapi.cep.model.Rule;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 자연어 룰 파서 서비스 (라이브러리 버전)
 * 사용자가 입력한 텍스트를 Rule 객체로 변환해주는 똑똑한 파서!
 */
public class RuleParserService {
    
    /**
     * 텍스트로 입력된 룰을 파싱해서 Rule 객체로 변환
     * 
     * 예시 입력:
     * - "중국에서 30만원 이하 이체 3건에 1분간 3회"
     * - "미국에서 50만원 이상 거래시 즉시 알림"
     * - "모바일에서 10만원 이상 거래 5분에 2회 이상"
     * - "한국에서 거래 이후 중국에서 로그인"
     */
    public Rule parseRule(String ruleText, String ruleName) {
        try {
            System.out.println("Parsing rule text: " + ruleText);
            
            // 룰 ID 생성
            String ruleId = "rule-" + System.currentTimeMillis();
            
            // 정규표현식 패턴들
            String normalizedText = ruleText.toLowerCase().replaceAll("\\s+", " ").trim();
            
            // 시퀀스 패턴 체크 (이후, 후, 다음에 등의 키워드)
            if (isSequencePattern(normalizedText)) {
                System.out.println("Detected sequence pattern, parsing as SEQUENCE rule");
                return parseSequenceRule(ruleId, ruleName, normalizedText);
            }
            
            // 단일 이벤트 룰 파싱
            return parseSingleEventRule(ruleId, ruleName, normalizedText);
            
        } catch (Exception e) {
            System.err.println("Error parsing rule: " + e.getMessage());
            return createDefaultRule(ruleText, ruleName);
        }
    }

    /**
     * 시퀀스 패턴인지 체크
     */
    private boolean isSequencePattern(String text) {
        return text.contains("이후") || text.contains("후") || text.contains("다음에") || text.contains("한뒤에") || text.contains("한뒤") || 
               text.contains("그리고") || text.contains("->") || text.contains("→");
    }

    /**
     * 시퀀스 룰 파싱
     */
    private Rule parseSequenceRule(String ruleId, String ruleName, String text) {
        System.out.println("Parsing sequence rule from: " + text);
        
        List<Rule.SequenceStep> sequenceSteps = new ArrayList<>();
        Rule.Severity severity = determineSeverity(text);
        
        // "이후"로 분리하여 각 단계 파싱
        String[] parts = text.split("\\s*(이후|후|다음에|그리고|한뒤에|한뒤)\\s*");
        
        for (int i = 0; i < parts.length; i++) {
            String stepText = parts[i].trim();
            if (stepText.isEmpty()) continue;
            
            System.out.println("Parsing step " + (i + 1) + ": " + stepText);
            
            String stepName = "step_" + (i + 1);
            String eventType = detectEventType(stepText);
            List<Rule.Condition> stepConditions = new ArrayList<>();
            
            // 각 단계별 조건 파싱
            parseRegionCondition(stepText, stepConditions);
            parseAmountCondition(stepText, stepConditions);
            parseDeviceCondition(stepText, stepConditions);
            parseNewDeviceCondition(stepText, stepConditions);
            parseAgeCondition(stepText, stepConditions);
            parseGenderCondition(stepText, stepConditions);
            parseTimeRangeCondition(stepText, stepConditions);
            parseAccountTypeCondition(stepText, stepConditions);
            parseJobCondition(stepText, stepConditions);
            
            // 이벤트 타입을 명시적으로 추가
            if (eventType != null) {
                stepConditions.add(new Rule.Condition("eventType", Rule.Operator.EQUALS, eventType));
            }
            
            Rule.SequenceStep step = new Rule.SequenceStep(stepName, eventType)
                    .withConditions(stepConditions);
            
            sequenceSteps.add(step);
            System.out.println("Added sequence step: " + step.toString());
        }
        
        // 전체 시간 윈도우 파싱 (전체 시퀀스에 적용)
        Rule.TimeWindow timeWindow = parseTimeWindow(text);
        
        Rule rule = new Rule(ruleId, ruleName, Rule.RuleType.SEQUENCE)
                .withSequenceSteps(sequenceSteps)
                .withSeverity(severity)
                .withAction("ALERT_SEQUENCE_PATTERN");
        
        if (timeWindow != null) {
            rule = rule.withTimeWindow(timeWindow);
        }
        
        System.out.println(" Successfully parsed sequence rule with " + sequenceSteps.size() + " steps");
        
        return rule;
    }

    /**
     * 단일 이벤트 룰 파싱
     */
    private Rule parseSingleEventRule(String ruleId, String ruleName, String normalizedText) {
        Rule.RuleType ruleType = Rule.RuleType.SINGLE_EVENT;
        List<Rule.Condition> conditions = new ArrayList<>();
        Rule.Severity severity = Rule.Severity.MEDIUM;
        String action = "ALERT_PARSED_RULE";
        Rule.TimeWindow timeWindow = null;
        
        // 각종 조건 파싱
        parseRegionCondition(normalizedText, conditions);
        parseAmountCondition(normalizedText, conditions);
        parseDeviceCondition(normalizedText, conditions);
        parseNewDeviceCondition(normalizedText, conditions);
        parseEventTypeCondition(normalizedText, conditions);
        parseAgeCondition(normalizedText, conditions);
        parseGenderCondition(normalizedText, conditions);
        parseTimeRangeCondition(normalizedText, conditions);
        parseAccountTypeCondition(normalizedText, conditions);
        parseJobCondition(normalizedText, conditions);
        
        // 시간 윈도우 및 빈도 파싱
        timeWindow = parseTimeWindow(normalizedText);
        Integer frequencyCount = parseFrequencyCount(normalizedText);
        
        if (timeWindow != null && frequencyCount != null) {
            ruleType = Rule.RuleType.FREQUENCY;
        }
        
        severity = determineSeverity(normalizedText);
        
        Rule rule = new Rule(ruleId, ruleName, ruleType)
                .withConditions(conditions)
                .withSeverity(severity)
                .withAction(action);
        
        if (timeWindow != null) {
            rule = rule.withTimeWindow(timeWindow);
        }
        
        if (frequencyCount != null) {
            rule = rule.withFrequencyCount(frequencyCount);
        }
        
        System.out.println("Successfully parsed single event rule: " + rule.getRuleName());
        return rule;
    }

    // 나머지 파싱 메서드들...
    private String detectEventType(String text) {
        if (text.contains("거래") || text.contains("이체") || text.contains("송금") || text.contains("결제")) {
            return "TRANSACTION";
        } else if (text.contains("로그인") || text.contains("접속") || text.contains("인증")) {
            return "LOGIN";
        } else if (text.contains("조회") || text.contains("확인") || text.contains("검색")) {
            return "VIEW";
        }
        return "TRANSACTION"; // 기본값
    }

    private void parseRegionCondition(String text, List<Rule.Condition> conditions) {
        String[] regions = {"한국", "중국", "미국", "일본", "영국", "독일", "프랑스", "해외"};
        for (String region : regions) {
            if (text.contains(region)) {
                conditions.add(new Rule.Condition("region", Rule.Operator.EQUALS, region));
                System.out.println(" Found region condition: " + region);
                break;
            }
        }
    }

    private void parseAmountCondition(String text, List<Rule.Condition> conditions) {
        System.out.println("💰 Parsing amount condition from: " + text);
        
        Pattern amountPattern = Pattern.compile("(\\d+)만원\\s*(이상|이하|초과|미만)");
        Matcher matcher = amountPattern.matcher(text);
        
        if (matcher.find()) {
            double amount = Double.parseDouble(matcher.group(1)) * 10000;
            String operator = matcher.group(2);
            
            Rule.Operator op;
            switch (operator) {
                case "이상": op = Rule.Operator.GREATER_THAN_OR_EQUAL; break;
                case "이하": op = Rule.Operator.LESS_THAN_OR_EQUAL; break;
                case "초과": op = Rule.Operator.GREATER_THAN; break;
                case "미만": op = Rule.Operator.LESS_THAN; break;
                default: op = Rule.Operator.GREATER_THAN_OR_EQUAL;
            }
            
            conditions.add(new Rule.Condition("amount", op, amount));
            System.out.println(" Found amount condition: " + amount + " " + op);
        } else {
            System.out.println(" No amount condition found");
        }
    }

    private void parseDeviceCondition(String text, List<Rule.Condition> conditions) {
        if (text.contains("모바일") || text.contains("핸드폰") || text.contains("스마트폰")) {
            conditions.add(new Rule.Condition("deviceType", Rule.Operator.EQUALS, "MOBILE"));
            System.out.println(" Found device condition: MOBILE");
        } else if (text.contains("데스크탑") || text.contains("pc") || text.contains("컴퓨터")||text.contains("노트북")) {
            conditions.add(new Rule.Condition("deviceType", Rule.Operator.EQUALS, "DESKTOP"));
            System.out.println("Found device condition: DESKTOP");
        }
    }

    private void parseNewDeviceCondition(String text, List<Rule.Condition> conditions) {
        if (text.contains("신규") && (text.contains("기기") || text.contains("디바이스") || text.contains("장치")) || text.contains("신규기기")) {
            conditions.add(new Rule.Condition("isNewDevice", Rule.Operator.EQUALS, true));
            System.out.println("Found new device condition");
        }
    }

    private void parseEventTypeCondition(String text, List<Rule.Condition> conditions) {
        String eventType = detectEventType(text);
        if (eventType != null) {
            conditions.add(new Rule.Condition("eventType", Rule.Operator.EQUALS, eventType));
            System.out.println("Found event type condition: " + eventType);
        }
    }

    private void parseAgeCondition(String text, List<Rule.Condition> conditions) {
        Pattern agePattern = Pattern.compile("(\\d+)세\\s*(이상|이하|초과|미만)");
        Matcher matcher = agePattern.matcher(text);
        
        if (matcher.find()) {
            int age = Integer.parseInt(matcher.group(1));
            String operator = matcher.group(2);
            
            Rule.Operator op;
            switch (operator) {
                case "이상": op = Rule.Operator.GREATER_THAN_OR_EQUAL; break;
                case "이하": op = Rule.Operator.LESS_THAN_OR_EQUAL; break;
                case "초과": op = Rule.Operator.GREATER_THAN; break;
                case "미만": op = Rule.Operator.LESS_THAN; break;
                default: op = Rule.Operator.GREATER_THAN_OR_EQUAL;
            }
            
            conditions.add(new Rule.Condition("age", op, age));
            System.out.println("Found age condition: " + age + " " + op);
        }
    }

    private void parseGenderCondition(String text, List<Rule.Condition> conditions) {
        if (text.contains("남자") || text.contains("남성")) {
            conditions.add(new Rule.Condition("gender", Rule.Operator.EQUALS, "MALE"));
            System.out.println("Found gender condition: MALE");
        } else if (text.contains("여자") || text.contains("여성")) {
            conditions.add(new Rule.Condition("gender", Rule.Operator.EQUALS, "FEMALE"));
            System.out.println("Found gender condition: FEMALE");
        }
    }

    private void parseTimeRangeCondition(String text, List<Rule.Condition> conditions) {
        if (text.contains("새벽") || text.contains("밤") || text.contains("야간")) {
            conditions.add(new Rule.Condition("timeRange", Rule.Operator.EQUALS, "NIGHT"));
            System.out.println("Found time range condition: NIGHT");
        } else if (text.contains("휴일") || text.contains("주말")) {
            conditions.add(new Rule.Condition("isHoliday", Rule.Operator.EQUALS, true));
            System.out.println("Found holiday condition");
        }
    }

    private void parseAccountTypeCondition(String text, List<Rule.Condition> conditions) {
        if (text.contains("신규") && text.contains("계좌")) {
            conditions.add(new Rule.Condition("isNewAccount", Rule.Operator.EQUALS, true));
            System.out.println("Found new account condition");
        }
    }

    private void parseJobCondition(String text, List<Rule.Condition> conditions) {
        if (text.contains("무직자") || text.contains("무직")) {
            conditions.add(new Rule.Condition("job", Rule.Operator.EQUALS, "UNEMPLOYED"));
            System.out.println("Found job condition: UNEMPLOYED");
        }
    }

    private Rule.TimeWindow parseTimeWindow(String text) {
        Pattern timePattern = Pattern.compile("(\\d+)(분|시간|초)\\s*(간|동안|안에)");
        Matcher matcher = timePattern.matcher(text);
        
        if (matcher.find()) {
            int value = Integer.parseInt(matcher.group(1));
            String unit = matcher.group(2);
            
            Rule.TimeUnit timeUnit;
            switch (unit) {
                case "초": timeUnit = Rule.TimeUnit.SECONDS; break;
                case "분": timeUnit = Rule.TimeUnit.MINUTES; break;
                case "시간": timeUnit = Rule.TimeUnit.HOURS; break;
                default: timeUnit = Rule.TimeUnit.MINUTES;
            }
            
            System.out.println("Found time window: " + value + " " + timeUnit);
            return new Rule.TimeWindow(value, timeUnit);
        }
        
        System.out.println("No time window found");
        return null;
    }

    private Integer parseFrequencyCount(String text) {
        Pattern freqPattern = Pattern.compile("(\\d+)\\s*(회|건|번)");
        Matcher matcher = freqPattern.matcher(text);
        
        if (matcher.find()) {
            int count = Integer.parseInt(matcher.group(1));
            System.out.println("Found frequency count: " + count);
            return count;
        }
        
        System.out.println("No frequency count found");
        return null;
    }

    private Rule.Severity determineSeverity(String text) {
        if (text.contains("긴급") || text.contains("즉시") || text.contains("고위험")) {
            return Rule.Severity.HIGH;
        } else if (text.contains("주의") || text.contains("중간")) {
            return Rule.Severity.MEDIUM;
        } else if (text.contains("낮음") || text.contains("정보")) {
            return Rule.Severity.LOW;
        }
        return Rule.Severity.MEDIUM;
    }

    private Rule createDefaultRule(String ruleText, String ruleName) {
        String ruleId = "rule-" + System.currentTimeMillis();
        return new Rule(ruleId, ruleName, Rule.RuleType.SINGLE_EVENT)
                .withConditions(new ArrayList<>())
                .withSeverity(Rule.Severity.LOW)
                .withAction("DEFAULT_ALERT");
    }
} 