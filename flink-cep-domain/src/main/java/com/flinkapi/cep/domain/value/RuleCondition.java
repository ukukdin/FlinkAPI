package com.flinkapi.cep.domain.value;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.flinkapi.cep.domain.model.Event;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * 룰 조건 값 객체 - 이벤트 매칭 조건을 표현하는 Value Object
 * 
 */
public class RuleCondition implements Serializable {
    
    @JsonProperty("field")
    private final String field;
    
    @JsonProperty("operator")
    private final Operator operator;
    
    @JsonProperty("value")
    private final Object value;
    
    @JsonProperty("logicalOperator")
    private final LogicalOperator logicalOperator;

    // 기본 생성자 (Jackson 용)
    public RuleCondition() {
        this.field = null;
        this.operator = null;
        this.value = null;
        this.logicalOperator = LogicalOperator.AND;
    }

    public RuleCondition(String field, Operator operator, Object value) {
        this.field = field;
        this.operator = operator;
        this.value = value;
        this.logicalOperator = LogicalOperator.AND;
    }

    public RuleCondition(String field, Operator operator, Object value, LogicalOperator logicalOperator) {
        this.field = field;
        this.operator = operator;
        this.value = value;
        this.logicalOperator = logicalOperator;
    }

    // Fluent API - 새로운 인스턴스 생성
    public RuleCondition withLogicalOperator(LogicalOperator logicalOperator) {
        return new RuleCondition(this.field, this.operator, this.value, logicalOperator);
    }

    // 핵심 비즈니스 로직 - 이벤트 매칭
    public boolean matches(Event event) {
        if (event == null || field == null || operator == null) {
            return false;
        }

        Object eventValue = extractFieldValue(event, field);
        return evaluateCondition(eventValue, operator, value);
    }

    private Object extractFieldValue(Event event, String fieldName) {
        switch (fieldName) {
            case "eventType": return event.getEventType();
            case "userId": return event.getUserId();
            case "amount": return event.getAmount();
            case "region": return event.getRegion();
            case "deviceType": return event.getDeviceType();
            case "timestamp": return event.getTimestamp();
            case "sessionId": return event.getSessionId();
            case "ipAddress": return event.getIpAddress();
            default:
                if (event.getProperties() != null) {
                    return event.getProperties().get(fieldName);
                }
                return null;
        }
    }

    @SuppressWarnings("unchecked")
    private boolean evaluateCondition(Object eventValue, Operator operator, Object conditionValue) {
        if (eventValue == null && conditionValue == null) {
            return operator == Operator.EQUALS;
        }
        if (eventValue == null || conditionValue == null) {
            return operator == Operator.NOT_EQUALS;
        }

        switch (operator) {
            case EQUALS:
                return eventValue.equals(conditionValue);
            case NOT_EQUALS:
                return !eventValue.equals(conditionValue);
            case GREATER_THAN:
                return compareNumbers(eventValue, conditionValue) > 0;
            case GREATER_THAN_OR_EQUAL:
                return compareNumbers(eventValue, conditionValue) >= 0;
            case LESS_THAN:
                return compareNumbers(eventValue, conditionValue) < 0;
            case LESS_THAN_OR_EQUAL:
                return compareNumbers(eventValue, conditionValue) <= 0;
            case CONTAINS:
                return eventValue.toString().contains(conditionValue.toString());
            case STARTS_WITH:
                return eventValue.toString().startsWith(conditionValue.toString());
            case ENDS_WITH:
                return eventValue.toString().endsWith(conditionValue.toString());
            case IN:
                if (conditionValue instanceof List) {
                    return ((List<?>) conditionValue).contains(eventValue);
                }
                return false;
            case NOT_IN:
                if (conditionValue instanceof List) {
                    return !((List<?>) conditionValue).contains(eventValue);
                }
                return true;
            default:
                return false;
        }
    }

    private int compareNumbers(Object value1, Object value2) {
        if (value1 instanceof Number && value2 instanceof Number) {
            double d1 = ((Number) value1).doubleValue();
            double d2 = ((Number) value2).doubleValue();
            return Double.compare(d1, d2);
        }
        throw new IllegalArgumentException("Cannot compare non-numeric values");
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

    // 논리 연산자 열거형
    public enum LogicalOperator {
        @JsonProperty("AND")
        AND,
        
        @JsonProperty("OR")
        OR
    }

    // Getter 메서드들 (불변 객체)
    public String getField() { return field; }
    public Operator getOperator() { return operator; }
    public Object getValue() { return value; }
    public LogicalOperator getLogicalOperator() { return logicalOperator; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RuleCondition that = (RuleCondition) o;
        return Objects.equals(field, that.field) &&
               operator == that.operator &&
               Objects.equals(value, that.value) &&
               logicalOperator == that.logicalOperator;
    }

    @Override
    public int hashCode() {
        return Objects.hash(field, operator, value, logicalOperator);
    }

    @Override
    public String toString() {
        return String.format("RuleCondition{field='%s', operator=%s, value=%s, logicalOperator=%s}",
                field, operator, value, logicalOperator);
    }
} 