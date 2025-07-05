package com.flinkapi.cep.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 실시간 룰 정의 클래스
 * 바이브 코딩으로 만든 강력한 룰 시스템!
 */
public class Rule implements Serializable {
    
    @JsonProperty("ruleId")
    private String ruleId;
    
    @JsonProperty("ruleName")
    private String ruleName;
    
    @JsonProperty("ruleType")
    private RuleType ruleType;
    
    @JsonProperty("conditions")
    private List<Condition> conditions;
    
    @JsonProperty("timeWindow")
    private TimeWindow timeWindow;
    
    @JsonProperty("severity")
    private Severity severity;
    
    @JsonProperty("action")
    private String action;
    
    @JsonProperty("enabled")
    private boolean enabled = true;
    
    @JsonProperty("frequencyCount")
    private Integer frequencyCount; // FREQUENCY 타입 룰용 빈도수
    
    @JsonProperty("sequenceSteps")
    private List<SequenceStep> sequenceSteps; // SEQUENCE 타입 룰용 단계별 조건
    
    @JsonProperty("metadata")
    private Map<String, Object> metadata;

    // 기본 생성자
    public Rule() {}

    // 빌더 패턴 생성자
    public Rule(String ruleId, String ruleName, RuleType ruleType) {
        this.ruleId = ruleId;
        this.ruleName = ruleName;
        this.ruleType = ruleType;
    }

    // Fluent API 스타일 빌더 메서드들
    public Rule withConditions(List<Condition> conditions) {
        this.conditions = conditions;
        return this;
    }

    public Rule withTimeWindow(TimeWindow timeWindow) {
        this.timeWindow = timeWindow;
        return this;
    }

    public Rule withSeverity(Severity severity) {
        this.severity = severity;
        return this;
    }

    public Rule withAction(String action) {
        this.action = action;
        return this;
    }

    public Rule withEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public Rule withFrequencyCount(Integer frequencyCount) {
        this.frequencyCount = frequencyCount;
        return this;
    }

    public Rule withSequenceSteps(List<SequenceStep> sequenceSteps) {
        this.sequenceSteps = sequenceSteps;
        return this;
    }

    public Rule withMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
        return this;
    }

    // 룰 타입 열거형
    public enum RuleType {
        @JsonProperty("SINGLE_EVENT")
        SINGLE_EVENT("단일 이벤트 룰"),
        
        @JsonProperty("SEQUENCE")
        SEQUENCE("시퀀스 패턴 룰"),
        
        @JsonProperty("THRESHOLD")
        THRESHOLD("임계값 룰"),
        
        @JsonProperty("FREQUENCY")
        FREQUENCY("빈도 기반 룰"),
        
        @JsonProperty("ANOMALY")
        ANOMALY("이상 탐지 룰");

        private final String description;

        RuleType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 심각도 열거형
    public enum Severity {
        @JsonProperty("LOW")
        LOW("낮음", 1),
        
        @JsonProperty("MEDIUM")
        MEDIUM("보통", 2),
        
        @JsonProperty("HIGH")
        HIGH("높음", 3),
        
        @JsonProperty("CRITICAL")
        CRITICAL("위험", 4);

        private final String description;
        private final int level;

        Severity(String description, int level) {
            this.description = description;
            this.level = level;
        }

        public String getDescription() {
            return description;
        }

        public int getLevel() {
            return level;
        }
    }

    // 조건 클래스
    public static class Condition implements Serializable {
        @JsonProperty("field")
        private String field;
        
        @JsonProperty("operator")
        private Operator operator;
        
        @JsonProperty("value")
        private Object value;
        
        @JsonProperty("logicalOperator")
        private LogicalOperator logicalOperator = LogicalOperator.AND;

        public Condition() {}

        public Condition(String field, Operator operator, Object value) {
            this.field = field;
            this.operator = operator;
            this.value = value;
        }

        // Fluent API
        public Condition withLogicalOperator(LogicalOperator logicalOperator) {
            this.logicalOperator = logicalOperator;
            return this;
        }

        // Getters/Setters
        public String getField() { return field; }
        public void setField(String field) { this.field = field; }
        
        public Operator getOperator() { return operator; }
        public void setOperator(Operator operator) { this.operator = operator; }
        
        public Object getValue() { return value; }
        public void setValue(Object value) { this.value = value; }
        
        public LogicalOperator getLogicalOperator() { return logicalOperator; }
        public void setLogicalOperator(LogicalOperator logicalOperator) { this.logicalOperator = logicalOperator; }

        @Override
        public String toString() {
            return String.format("%s %s %s", field, operator, value);
        }
    }

    // 연산자 열거형
    public enum Operator {
        @JsonProperty("EQUALS")
        EQUALS("=="),
        
        @JsonProperty("NOT_EQUALS")
        NOT_EQUALS("!="),
        
        @JsonProperty("GREATER_THAN")
        GREATER_THAN(">"),
        
        @JsonProperty("GREATER_THAN_OR_EQUAL")
        GREATER_THAN_OR_EQUAL(">="),
        
        @JsonProperty("LESS_THAN")
        LESS_THAN("<"),
        
        @JsonProperty("LESS_THAN_OR_EQUAL")
        LESS_THAN_OR_EQUAL("<="),
        
        @JsonProperty("CONTAINS")
        CONTAINS("CONTAINS"),
        
        @JsonProperty("STARTS_WITH")
        STARTS_WITH("STARTS_WITH"),
        
        @JsonProperty("ENDS_WITH")
        ENDS_WITH("ENDS_WITH"),
        
        @JsonProperty("IN")
        IN("IN"),
        
        @JsonProperty("NOT_IN")
        NOT_IN("NOT_IN");

        private final String symbol;

        Operator(String symbol) {
            this.symbol = symbol;
        }

        public String getSymbol() {
            return symbol;
        }
    }

    // 논리 연산자
    public enum LogicalOperator {
        @JsonProperty("AND")
        AND,
        
        @JsonProperty("OR")
        OR
    }

    // 시간 윈도우 클래스
    public static class TimeWindow implements Serializable {
        @JsonProperty("duration")
        private long duration; // milliseconds
        
        @JsonProperty("unit")
        private TimeUnit unit;

        public TimeWindow() {}

        public TimeWindow(long duration, TimeUnit unit) {
            this.duration = duration;
            this.unit = unit;
        }

        // Getters/Setters
        public long getDuration() { return duration; }
        public void setDuration(long duration) { this.duration = duration; }
        
        public TimeUnit getUnit() { return unit; }
        public void setUnit(TimeUnit unit) { this.unit = unit; }

        public long toMilliseconds() {
            return duration * unit.getMilliseconds();
        }

        @Override
        public String toString() {
            return duration + " " + unit.name().toLowerCase();
        }
    }

    // 시간 단위
    public enum TimeUnit {
        @JsonProperty("MILLISECONDS")
        MILLISECONDS(1),
        
        @JsonProperty("SECONDS")
        SECONDS(1000),
        
        @JsonProperty("MINUTES")
        MINUTES(60 * 1000),
        
        @JsonProperty("HOURS")
        HOURS(60 * 60 * 1000),
        
        @JsonProperty("DAYS")
        DAYS(24 * 60 * 60 * 1000);

        private final long milliseconds;

        TimeUnit(long milliseconds) {
            this.milliseconds = milliseconds;
        }

        public long getMilliseconds() {
            return milliseconds;
        }
    }

    // 시퀀스 단계 클래스
    public static class SequenceStep implements Serializable {
        @JsonProperty("stepName")
        private String stepName;
        
        @JsonProperty("eventType")
        private String eventType; // "TRANSACTION", "LOGIN", etc.
        
        @JsonProperty("conditions")
        private List<Condition> conditions;
        
        @JsonProperty("optional")
        private boolean optional = false;
        
        @JsonProperty("timeConstraint")
        private TimeWindow timeConstraint; // 이전 단계로부터의 시간 제약

        public SequenceStep() {}

        public SequenceStep(String stepName, String eventType) {
            this.stepName = stepName;
            this.eventType = eventType;
        }

        // Fluent API
        public SequenceStep withConditions(List<Condition> conditions) {
            this.conditions = conditions;
            return this;
        }

        public SequenceStep withOptional(boolean optional) {
            this.optional = optional;
            return this;
        }

        public SequenceStep withTimeConstraint(TimeWindow timeConstraint) {
            this.timeConstraint = timeConstraint;
            return this;
        }

        // Getters/Setters
        public String getStepName() { return stepName; }
        public void setStepName(String stepName) { this.stepName = stepName; }
        
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
        
        public List<Condition> getConditions() { return conditions; }
        public void setConditions(List<Condition> conditions) { this.conditions = conditions; }
        
        public boolean isOptional() { return optional; }
        public void setOptional(boolean optional) { this.optional = optional; }
        
        public TimeWindow getTimeConstraint() { return timeConstraint; }
        public void setTimeConstraint(TimeWindow timeConstraint) { this.timeConstraint = timeConstraint; }

        @Override
        public String toString() {
            return String.format("Step[%s-%s: %d conditions]", stepName, eventType, 
                conditions != null ? conditions.size() : 0);
        }
    }

    // Getters/Setters
    public String getRuleId() { return ruleId; }
    public void setRuleId(String ruleId) { this.ruleId = ruleId; }

    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }

    public RuleType getRuleType() { return ruleType; }
    public void setRuleType(RuleType ruleType) { this.ruleType = ruleType; }

    public List<Condition> getConditions() { return conditions; }
    public void setConditions(List<Condition> conditions) { this.conditions = conditions; }

    public TimeWindow getTimeWindow() { return timeWindow; }
    public void setTimeWindow(TimeWindow timeWindow) { this.timeWindow = timeWindow; }

    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public Integer getFrequencyCount() { return frequencyCount; }
    public void setFrequencyCount(Integer frequencyCount) { this.frequencyCount = frequencyCount; }

    public List<SequenceStep> getSequenceSteps() { return sequenceSteps; }
    public void setSequenceSteps(List<SequenceStep> sequenceSteps) { this.sequenceSteps = sequenceSteps; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rule rule = (Rule) o;
        return Objects.equals(ruleId, rule.ruleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ruleId);
    }

    @Override
    public String toString() {
        return String.format("Rule{ruleId='%s', ruleName='%s', ruleType=%s, severity=%s, enabled=%s, sequenceSteps=%s}",
                ruleId, ruleName, ruleType, severity, enabled, 
                sequenceSteps != null ? sequenceSteps.size() + " steps" : "null");
    }
} 