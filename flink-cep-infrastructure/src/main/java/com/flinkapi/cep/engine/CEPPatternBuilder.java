package com.flinkapi.cep.engine;

import com.flinkapi.cep.model.Event;
import com.flinkapi.cep.model.Rule;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.IterativeCondition;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * CEP 패턴 빌더 - 바이브 코딩으로 만든 초강력 패턴 생성기!
 * Rule 객체를 받아서 실시간 Flink CEP Pattern으로 변환합니다.
 */
public class CEPPatternBuilder {
    
    private static final Logger logger = LoggerFactory.getLogger(CEPPatternBuilder.class);
    
    /**
     * 룰을 기반으로 CEP 패턴을 동적 생성!
     */
    public static Pattern<Event, ?> buildPattern(Rule rule) {
        logger.info("Building CEP pattern for rule: {}", rule.getRuleName());
        
        switch (rule.getRuleType()) {
            case SINGLE_EVENT:
                return buildSingleEventPattern(rule);
            case SEQUENCE:
                return buildSequencePattern(rule);
            case THRESHOLD:
                return buildThresholdPattern(rule);
            case FREQUENCY:
                return buildFrequencyPattern(rule);
            case ANOMALY:
                return buildAnomalyPattern(rule);
            default:
                throw new IllegalArgumentException("지원하지 않는 룰 타입: " + rule.getRuleType());
        }
    }
    
    /**
     * 단일 이벤트 패턴 - 하나의 이벤트가 조건을 만족하면 발생
     */
    private static Pattern<Event, ?> buildSingleEventPattern(Rule rule) {
        logger.info("Building single event pattern...");
        
        Pattern<Event, ?> pattern = Pattern.<Event>begin("alert")
                .where(new ConditionBuilder(rule.getConditions()).build());
        
        // 시간 윈도우 적용
        if (rule.getTimeWindow() != null) {
            pattern = pattern.within(Time.milliseconds(rule.getTimeWindow().toMilliseconds()));
        }
        
        return pattern;
    }
    
    /**
     * 시퀀스 패턴 - 여러 이벤트가 순차적으로 발생 (동적 생성)
     */
    private static Pattern<Event, ?> buildSequencePattern(Rule rule) {
        logger.info("Building sequence pattern with {} steps...", 
                   rule.getSequenceSteps() != null ? rule.getSequenceSteps().size() : 0);
        
        // sequenceSteps가 없으면 기본 단일 이벤트 패턴으로 처리
        if (rule.getSequenceSteps() == null || rule.getSequenceSteps().isEmpty()) {
            logger.warn("No sequence steps found, falling back to single event pattern");
            return buildSingleEventPattern(rule);
        }
        
        List<Rule.SequenceStep> steps = rule.getSequenceSteps();
        Pattern<Event, ?> pattern = null;
        
        // 첫 번째 단계
        Rule.SequenceStep firstStep = steps.get(0);
        logger.info("Creating first step: {} ({})", firstStep.getStepName(), firstStep.getEventType());
        
        pattern = Pattern.<Event>begin(firstStep.getStepName())
                .where(new ConditionBuilder(firstStep.getConditions()).build());
        
        // 나머지 단계들을 순차적으로 연결
        for (int i = 1; i < steps.size(); i++) {
            Rule.SequenceStep step = steps.get(i);
            logger.info("Adding step {}: {} ({})", i + 1, step.getStepName(), step.getEventType());
            
            if (step.isOptional()) {
                // 선택적 단계 (followedByAny 사용)
                pattern = pattern.followedByAny(step.getStepName())
                        .where(new ConditionBuilder(step.getConditions()).build())
                        .optional();
            } else {
                // 필수 단계 (next 사용 - 연속된 이벤트)
                pattern = pattern.next(step.getStepName())
                        .where(new ConditionBuilder(step.getConditions()).build());
            }
        }
        
        // 전체 시퀀스에 시간 윈도우 적용
        if (rule.getTimeWindow() != null) {
            long timeWindowMs = rule.getTimeWindow().toMilliseconds();
            logger.info("Applying time window: {} ms", timeWindowMs);
            pattern = pattern.within(Time.milliseconds(timeWindowMs));
        } else {
            // 기본 30분 윈도우 설정
            logger.info("Applying default 30-minute window");
            pattern = pattern.within(Time.minutes(30));
        }
        
        logger.info("Sequence pattern built successfully with {} steps", steps.size());
        return pattern;
    }
    
    /**
     * 임계값 패턴 - 특정 값이 임계값을 초과했을 때
     */
    private static Pattern<Event, ?> buildThresholdPattern(Rule rule) {
        logger.info("Building threshold pattern...");
        
        Pattern<Event, ?> pattern = Pattern.<Event>begin("threshold")
                .where(new ConditionBuilder(rule.getConditions()).build());
        
        // 시간 윈도우 내에서 임계값 체크
        if (rule.getTimeWindow() != null) {
            pattern = pattern.within(Time.milliseconds(rule.getTimeWindow().toMilliseconds()));
        }
        
        return pattern;
    }
    
    /**
     * 빈도 패턴 - 특정 시간 내에 N번 이상 발생
     */
    private static Pattern<Event, ?> buildFrequencyPattern(Rule rule) {
        logger.info("Building frequency pattern...");
        
        // 기본적으로 5분 내에 3번 이상 발생
        int times = 3;
        long windowMillis = 5 * 60 * 1000; // 5분
        
        if (rule.getTimeWindow() != null) {
            windowMillis = rule.getTimeWindow().toMilliseconds();
        }
        
        Pattern<Event, ?> pattern = Pattern.<Event>begin("frequent")
                .where(new ConditionBuilder(rule.getConditions()).build())
                .times(times)
                .within(Time.milliseconds(windowMillis));
        
        return pattern;
    }
    
    /**
     * 이상 탐지 패턴 - 일반적이지 않은 패턴 감지
     */
    private static Pattern<Event, ?> buildAnomalyPattern(Rule rule) {
        logger.info("Building anomaly detection pattern...");
        
        // 예: 짧은 시간 내에 다른 지역에서의 접근
        Pattern<Event, ?> pattern = Pattern.<Event>begin("first_location")
                .where(new ConditionBuilder(rule.getConditions()).build())
                .next("second_location")
                .where(new SimpleCondition<Event>() {
                    @Override
                    public boolean filter(Event event) throws Exception {
                        // 다른 지역에서의 접근 감지
                        return event.getRegion() != null && !event.getRegion().isEmpty();
                    }
                })
                .within(Time.minutes(10)); // 10분 내에 발생
        
        return pattern;
    }
    
    /**
     * 조건 빌더 - 동적으로 조건들을 결합
     */
    public static class ConditionBuilder implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        private final List<Rule.Condition> conditions;
        
        public ConditionBuilder(List<Rule.Condition> conditions) {
            this.conditions = conditions;
        }
        
        public SimpleCondition<Event> build() {
            return new RuleCondition(conditions);
        }
    }
    
    /**
     * 직렬화 가능한 룰 조건 클래스
     */
    public static class RuleCondition extends SimpleCondition<Event> {
        private static final long serialVersionUID = 1L;
        private final List<Rule.Condition> conditions;
        
        public RuleCondition(List<Rule.Condition> conditions) {
            this.conditions = conditions;
        }
        
        @Override
        public boolean filter(Event event) throws Exception {
            if (conditions == null || conditions.isEmpty()) {
                return true;
            }
            
            boolean result = true;
            Rule.LogicalOperator currentLogicalOp = Rule.LogicalOperator.AND;
            
            for (int i = 0; i < conditions.size(); i++) {
                Rule.Condition condition = conditions.get(i);
                boolean conditionResult = evaluateCondition(event, condition);
                
                if (i == 0) {
                    result = conditionResult;
                } else {
                    if (currentLogicalOp == Rule.LogicalOperator.AND) {
                        result = result && conditionResult;
                    } else {
                        result = result || conditionResult;
                    }
                }
                
                // 다음 반복을 위한 논리 연산자 설정
                if (i < conditions.size() - 1) {
                    currentLogicalOp = condition.getLogicalOperator();
                }
            }
            
            return result;
        }
        
        /**
         * 단일 조건 평가 - 리플렉션을 사용한 동적 필드 접근
         */
        private boolean evaluateCondition(Event event, Rule.Condition condition) {
            try {
                Object fieldValue = getFieldValue(event, condition.getField());
                Object conditionValue = condition.getValue();
                
                logger.debug("Evaluating: {} {} {} (actual: {})", 
                    condition.getField(), condition.getOperator(), conditionValue, fieldValue);
                
                switch (condition.getOperator()) {
                    case EQUALS:
                        return equals(fieldValue, conditionValue);
                    case NOT_EQUALS:
                        return !equals(fieldValue, conditionValue);
                    case GREATER_THAN:
                        return compareNumbers(fieldValue, conditionValue) > 0;
                    case GREATER_THAN_OR_EQUAL:
                        return compareNumbers(fieldValue, conditionValue) >= 0;
                    case LESS_THAN:
                        return compareNumbers(fieldValue, conditionValue) < 0;
                    case LESS_THAN_OR_EQUAL:
                        return compareNumbers(fieldValue, conditionValue) <= 0;
                    case CONTAINS:
                        return fieldValue != null && fieldValue.toString().contains(conditionValue.toString());
                    case STARTS_WITH:
                        return fieldValue != null && fieldValue.toString().startsWith(conditionValue.toString());
                    case ENDS_WITH:
                        return fieldValue != null && fieldValue.toString().endsWith(conditionValue.toString());
                    case IN:
                        return conditionValue instanceof Collection && 
                               ((Collection<?>) conditionValue).contains(fieldValue);
                    case NOT_IN:
                        return !(conditionValue instanceof Collection && 
                                ((Collection<?>) conditionValue).contains(fieldValue));
                    default:
                        logger.warn("지원하지 않는 연산자: {}", condition.getOperator());
                        return false;
                }
            } catch (Exception e) {
                logger.error("조건 평가 중 오류 발생: {}", e.getMessage(), e);
                return false;
            }
        }
        
        /**
         * 리플렉션을 사용한 필드 값 추출
         */
        private Object getFieldValue(Event event, String fieldName) throws Exception {
            // 먼저 직접 메서드 호출 시도
            switch (fieldName.toLowerCase()) {
                case "eventid": return event.getEventId();
                case "eventtype": return event.getEventType();
                case "userid": return event.getUserId();
                case "sessionid": return event.getSessionId();
                case "amount": return event.getAmount();
                case "region": return event.getRegion();
                case "devicetype": return event.getDeviceType();
                case "ipaddress": return event.getIpAddress();
                case "timestamp": return event.getTimestamp();
                case "properties": return event.getProperties();
            }
            
            // 리플렉션을 통한 필드 접근
            try {
                Field field = Event.class.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(event);
            } catch (NoSuchFieldException e) {
                // properties 맵에서 찾기
                if (event.getProperties() != null) {
                    return event.getProperties().get(fieldName);
                }
                throw new IllegalArgumentException("필드를 찾을 수 없습니다: " + fieldName);
            }
        }
        
        /**
         * 숫자 비교
         */
        private int compareNumbers(Object value1, Object value2) {
            if (value1 == null || value2 == null) {
                throw new IllegalArgumentException("null 값은 비교할 수 없습니다");
            }
            
            double num1 = ((Number) value1).doubleValue();
            double num2 = ((Number) value2).doubleValue();
            
            return Double.compare(num1, num2);
        }
        
        /**
         * 값 동등성 비교
         */
        private boolean equals(Object value1, Object value2) {
            if (value1 == null && value2 == null) return true;
            if (value1 == null || value2 == null) return false;
            return value1.equals(value2);
        }
    }
} 