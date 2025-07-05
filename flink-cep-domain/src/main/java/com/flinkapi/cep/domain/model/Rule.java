package com.flinkapi.cep.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.flinkapi.cep.domain.value.RuleCondition;
import com.flinkapi.cep.domain.value.TimeWindow;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 도메인 룰 엔티티 - 실시간 룰 정의의 핵심 도메인 모델
 */
public class Rule implements Serializable {
    
    @JsonProperty("ruleId")
    private String ruleId;
    
    @JsonProperty("ruleName")
    private String ruleName;
    
    @JsonProperty("ruleType")
    private RuleType ruleType;
    
    @JsonProperty("conditions")
    private List<RuleCondition> conditions;
    
    @JsonProperty("timeWindow")
    private TimeWindow timeWindow;
    
    @JsonProperty("severity")
    private Severity severity;
    
    @JsonProperty("action")
    private String action;
    
    @JsonProperty("enabled")
    private boolean enabled = true;
    
    @JsonProperty("frequencyCount")
    private Integer frequencyCount;
    
    @JsonProperty("sequenceSteps")
    private List<SequenceStep> sequenceSteps;
    
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
    public Rule withConditions(List<RuleCondition> conditions) {
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

    // 🔥 도메인 비즈니스 로직 메서드들
    public boolean canApplyToEvent(Event event) {
        if (!enabled) return false;
        if (conditions == null || conditions.isEmpty()) return false;
        
        return conditions.stream()
                .allMatch(condition -> condition.matches(event));
    }

    public boolean isHighPriorityRule() {
        return severity == Severity.HIGH || severity == Severity.CRITICAL;
    }

    public boolean isValidRule() {
        return ruleId != null && ruleName != null && ruleType != null;
    }

    public boolean requiresTimeWindow() {
        return ruleType == RuleType.FREQUENCY || ruleType == RuleType.SEQUENCE;
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

    // 시퀀스 단계 클래스
    public static class SequenceStep implements Serializable {
        @JsonProperty("stepName")
        private String stepName;
        
        @JsonProperty("eventType")
        private String eventType;
        
        @JsonProperty("conditions")
        private List<RuleCondition> conditions;
        
        @JsonProperty("optional")
        private boolean optional = false;
        
        @JsonProperty("timeConstraint")
        private TimeWindow timeConstraint;

        public SequenceStep() {}

        public SequenceStep(String stepName, String eventType) {
            this.stepName = stepName;
            this.eventType = eventType;
        }

        public SequenceStep withConditions(List<RuleCondition> conditions) {
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

        public List<RuleCondition> getConditions() { return conditions; }
        public void setConditions(List<RuleCondition> conditions) { this.conditions = conditions; }

        public boolean isOptional() { return optional; }
        public void setOptional(boolean optional) { this.optional = optional; }

        public TimeWindow getTimeConstraint() { return timeConstraint; }
        public void setTimeConstraint(TimeWindow timeConstraint) { this.timeConstraint = timeConstraint; }

        @Override
        public String toString() {
            return String.format("SequenceStep{stepName='%s', eventType='%s', optional=%s}",
                    stepName, eventType, optional);
        }
    }

    // Getter/Setter 메서드들
    public String getRuleId() { return ruleId; }
    public void setRuleId(String ruleId) { this.ruleId = ruleId; }

    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }

    public RuleType getRuleType() { return ruleType; }
    public void setRuleType(RuleType ruleType) { this.ruleType = ruleType; }

    public List<RuleCondition> getConditions() { return conditions; }
    public void setConditions(List<RuleCondition> conditions) { this.conditions = conditions; }

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
        return String.format("Rule{ruleId='%s', ruleName='%s', ruleType=%s, severity=%s, enabled=%s}",
                ruleId, ruleName, ruleType, severity, enabled);
    }
} 