package com.flinkapi.cep.application.dto;

import com.flinkapi.cep.domain.model.Rule;

/**
 *  알림 데이터 전송 객체 - 알림 정보를 외부로 전달하는 DTO
 * 
 */
public class AlertDto {
    
    private String alertId;
    private String ruleId;
    private String ruleName;
    private Rule.Severity severity;
    private String eventId;
    private String userId;
    private String message;
    private long timestamp;

    // 기본 생성자
    public AlertDto() {}

    // 전체 생성자
    public AlertDto(String alertId, String ruleId, String ruleName, Rule.Severity severity,
                   String eventId, String userId, String message, long timestamp) {
        this.alertId = alertId;
        this.ruleId = ruleId;
        this.ruleName = ruleName;
        this.severity = severity;
        this.eventId = eventId;
        this.userId = userId;
        this.message = message;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getAlertId() { return alertId; }
    public void setAlertId(String alertId) { this.alertId = alertId; }

    public String getRuleId() { return ruleId; }
    public void setRuleId(String ruleId) { this.ruleId = ruleId; }

    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }

    public Rule.Severity getSeverity() { return severity; }
    public void setSeverity(Rule.Severity severity) { this.severity = severity; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return String.format("AlertDto{alertId='%s', ruleId='%s', ruleName='%s', severity=%s, userId='%s'}",
                alertId, ruleId, ruleName, severity, userId);
    }
} 