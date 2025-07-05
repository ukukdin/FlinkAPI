package com.flinkapi.cep.domain.service;

import com.flinkapi.cep.domain.model.Event;
import com.flinkapi.cep.domain.model.Rule;
import com.flinkapi.cep.domain.value.RuleCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 *  룰 검증 도메인 서비스 - 룰의 유효성을 검증하는 핵심 도메인 서비스
 * DDD 패턴으로 설계된 도메인 서비스
 */
public class RuleValidationService {
    
    private static final Logger logger = LoggerFactory.getLogger(RuleValidationService.class);

    /**
     * 룰 전체 검증
     */
    public ValidationResult validateRule(Rule rule) {
        logger.debug("Validating rule: {}", rule.getRuleId());
        
        List<String> errors = new ArrayList<>();
        
        // 기본 필드 검증
        if (rule.getRuleId() == null || rule.getRuleId().trim().isEmpty()) {
            errors.add("룰 ID는 필수입니다.");
        }
        
        if (rule.getRuleName() == null || rule.getRuleName().trim().isEmpty()) {
            errors.add("룰 이름은 필수입니다.");
        }
        
        if (rule.getRuleType() == null) {
            errors.add("룰 타입은 필수입니다.");
        }
        
        if (rule.getSeverity() == null) {
            errors.add("심각도는 필수입니다.");
        }
        
        // 조건 검증
        if (rule.getConditions() == null || rule.getConditions().isEmpty()) {
            errors.add("최소 하나의 조건이 필요합니다.");
        } else {
            for (int i = 0; i < rule.getConditions().size(); i++) {
                RuleCondition condition = rule.getConditions().get(i);
                List<String> conditionErrors = validateCondition(condition);
                for (String error : conditionErrors) {
                    errors.add(String.format("조건 %d: %s", i + 1, error));
                }
            }
        }
        
        // 룰 타입별 특별 검증
        validateRuleTypeSpecific(rule, errors);
        
        return new ValidationResult(errors.isEmpty(), errors);
    }

    /**
     *  조건 검증
     */
    public List<String> validateCondition(RuleCondition condition) {
        List<String> errors = new ArrayList<>();
        
        if (condition.getField() == null || condition.getField().trim().isEmpty()) {
            errors.add("필드명은 필수입니다.");
        }
        
        if (condition.getOperator() == null) {
            errors.add("연산자는 필수입니다.");
        }
        
        if (condition.getValue() == null) {
            errors.add("비교값은 필수입니다.");
        }
        
        return errors;
    }

    /**
     * 룰 타입별 특별 검증
     */
    private void validateRuleTypeSpecific(Rule rule, List<String> errors) {
        switch (rule.getRuleType()) {
            case FREQUENCY:
                if (rule.getFrequencyCount() == null || rule.getFrequencyCount() <= 0) {
                    errors.add("FREQUENCY 룰에는 양수 빈도수가 필요합니다.");
                }
                if (rule.getTimeWindow() == null) {
                    errors.add("FREQUENCY 룰에는 시간 윈도우가 필요합니다.");
                }
                break;
                
            case SEQUENCE:
                if (rule.getSequenceSteps() == null || rule.getSequenceSteps().isEmpty()) {
                    errors.add("SEQUENCE 룰에는 최소 하나의 순서 단계가 필요합니다.");
                }
                break;
                
            case THRESHOLD:
                // 임계값 룰 특별 검증
                break;
                
            case ANOMALY:
                // 이상 탐지 룰 특별 검증
                break;
                
            default:
                // 단일 이벤트 룰은 기본 검증만 수행
                break;
        }
    }

    /**
     *  이벤트와 룰 호환성 검증
     */
    public boolean canRuleApplyToEvent(Rule rule, Event event) {
        if (rule == null || event == null) {
            return false;
        }
        
        if (!rule.isEnabled()) {
            return false;
        }
        
        // 조건 검증
        if (rule.getConditions() == null || rule.getConditions().isEmpty()) {
            return false;
        }
        
        return rule.canApplyToEvent(event);
    }

    /**
     *  룰 우선순위 검증
     */
    public int calculateRulePriority(Rule rule) {
        if (rule == null || rule.getSeverity() == null) {
            return 0;
        }
        
        int priority = rule.getSeverity().getLevel() * 100;
        
                 // 룰 타입별 가중치
         switch (rule.getRuleType()) {
             case ANOMALY:
                 priority += 40;
                 break;
             case FREQUENCY:
                 priority += 30;
                 break;
             case SEQUENCE:
                 priority += 20;
                 break;
             case THRESHOLD:
                 priority += 10;
                 break;
             default:
                 break;
         }
        
        return priority;
    }

    /**
     *  검증 결과 클래스
     */
    public static class ValidationResult {
        private final boolean valid;
        private final List<String> errors;

        public ValidationResult(boolean valid, List<String> errors) {
            this.valid = valid;
            this.errors = errors != null ? errors : new ArrayList<>();
        }

        public boolean isValid() {
            return valid;
        }

        public List<String> getErrors() {
            return new ArrayList<>(errors);
        }

        public String getErrorMessage() {
            return String.join(", ", errors);
        }

        @Override
        public String toString() {
            return String.format("ValidationResult{valid=%s, errors=%s}", valid, errors);
        }
    }
} 