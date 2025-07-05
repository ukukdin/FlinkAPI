package com.flinkapi.cep.application.dto;

import com.flinkapi.cep.domain.model.Rule;
import com.flinkapi.cep.domain.value.RuleCondition;
import com.flinkapi.cep.domain.value.TimeWindow;

import java.util.List;
import java.util.Map;

/**
 * 룰 생성 명령 객체 - 룰 생성을 위한 Command 객체
 * 
 */
public class RuleCreateCommand {
    
    private String ruleId;
    private String ruleName;
    private Rule.RuleType ruleType;
    private List<RuleCondition> conditions;
    private TimeWindow timeWindow;
    private Rule.Severity severity;
    private String action;
    private boolean enabled = true;
    private Integer frequencyCount;
    private List<Rule.SequenceStep> sequenceSteps;
    private Map<String, Object> metadata;

    // 기본 생성자
    public RuleCreateCommand() {}

    // 전체 생성자
    public RuleCreateCommand(String ruleId, String ruleName, Rule.RuleType ruleType,
                            List<RuleCondition> conditions, TimeWindow timeWindow,
                            Rule.Severity severity, String action, boolean enabled,
                            Integer frequencyCount, List<Rule.SequenceStep> sequenceSteps,
                            Map<String, Object> metadata) {
        this.ruleId = ruleId;
        this.ruleName = ruleName;
        this.ruleType = ruleType;
        this.conditions = conditions;
        this.timeWindow = timeWindow;
        this.severity = severity;
        this.action = action;
        this.enabled = enabled;
        this.frequencyCount = frequencyCount;
        this.sequenceSteps = sequenceSteps;
        this.metadata = metadata;
    }

    // Getters and Setters
    public String getRuleId() { return ruleId; }
    public void setRuleId(String ruleId) { this.ruleId = ruleId; }

    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }

    public Rule.RuleType getRuleType() { return ruleType; }
    public void setRuleType(Rule.RuleType ruleType) { this.ruleType = ruleType; }

    public List<RuleCondition> getConditions() { return conditions; }
    public void setConditions(List<RuleCondition> conditions) { this.conditions = conditions; }

    public TimeWindow getTimeWindow() { return timeWindow; }
    public void setTimeWindow(TimeWindow timeWindow) { this.timeWindow = timeWindow; }

    public Rule.Severity getSeverity() { return severity; }
    public void setSeverity(Rule.Severity severity) { this.severity = severity; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public Integer getFrequencyCount() { return frequencyCount; }
    public void setFrequencyCount(Integer frequencyCount) { this.frequencyCount = frequencyCount; }

    public List<Rule.SequenceStep> getSequenceSteps() { return sequenceSteps; }
    public void setSequenceSteps(List<Rule.SequenceStep> sequenceSteps) { this.sequenceSteps = sequenceSteps; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

    @Override
    public String toString() {
        return String.format("RuleCreateCommand{ruleId='%s', ruleName='%s', ruleType=%s, severity=%s}",
                ruleId, ruleName, ruleType, severity);
    }
} 