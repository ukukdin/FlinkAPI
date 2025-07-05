package com.flinkapi.cep.application.service;

import com.flinkapi.cep.domain.model.Rule;
import com.flinkapi.cep.domain.service.RuleValidationService;
import com.flinkapi.cep.application.port.RuleRepository;
import com.flinkapi.cep.application.dto.RuleDto;
import com.flinkapi.cep.application.dto.RuleCreateCommand;
import com.flinkapi.cep.application.dto.RuleUpdateCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 🎯 룰 관리 응용 서비스 - 룰 관리 유스케이스를 구현하는 Application Service
 * DDD 패턴으로 설계된 응용 서비스
 */
public class RuleManagementService {
    
    private static final Logger logger = LoggerFactory.getLogger(RuleManagementService.class);
    
    private final RuleRepository ruleRepository;
    private final RuleValidationService ruleValidationService;

    public RuleManagementService(RuleRepository ruleRepository, RuleValidationService ruleValidationService) {
        this.ruleRepository = ruleRepository;
        this.ruleValidationService = ruleValidationService;
    }

    /**
     * 🚀 룰 생성
     */
    public RuleDto createRule(RuleCreateCommand command) {
        logger.info("🚀 Creating rule: {}", command.getRuleName());
        
        // 도메인 모델 생성
        Rule rule = new Rule(command.getRuleId(), command.getRuleName(), command.getRuleType())
                .withConditions(command.getConditions())
                .withTimeWindow(command.getTimeWindow())
                .withSeverity(command.getSeverity())
                .withAction(command.getAction())
                .withEnabled(command.isEnabled())
                .withFrequencyCount(command.getFrequencyCount())
                .withSequenceSteps(command.getSequenceSteps())
                .withMetadata(command.getMetadata());
        
        // 룰 검증
        RuleValidationService.ValidationResult validationResult = ruleValidationService.validateRule(rule);
        if (!validationResult.isValid()) {
            String errorMessage = "룰 생성 실패: " + validationResult.getErrorMessage();
            logger.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
        
        // 중복 확인
        if (ruleRepository.existsById(rule.getRuleId())) {
            String errorMessage = "이미 존재하는 룰 ID입니다: " + rule.getRuleId();
            logger.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
        
        // 룰 저장
        Rule savedRule = ruleRepository.save(rule);
        logger.info("✅ Rule created successfully: {}", savedRule.getRuleId());
        
        return convertToDto(savedRule);
    }

    /**
     * 🔄 룰 업데이트
     */
    public RuleDto updateRule(RuleUpdateCommand command) {
        logger.info("🔄 Updating rule: {}", command.getRuleId());
        
        // 기존 룰 조회
        Optional<Rule> existingRuleOpt = ruleRepository.findById(command.getRuleId());
        if (!existingRuleOpt.isPresent()) {
            String errorMessage = "룰을 찾을 수 없습니다: " + command.getRuleId();
            logger.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
        
        Rule existingRule = existingRuleOpt.get();
        
        // 룰 업데이트
        Rule updatedRule = new Rule(existingRule.getRuleId(), 
                                   command.getRuleName() != null ? command.getRuleName() : existingRule.getRuleName(),
                                   command.getRuleType() != null ? command.getRuleType() : existingRule.getRuleType())
                .withConditions(command.getConditions() != null ? command.getConditions() : existingRule.getConditions())
                .withTimeWindow(command.getTimeWindow() != null ? command.getTimeWindow() : existingRule.getTimeWindow())
                .withSeverity(command.getSeverity() != null ? command.getSeverity() : existingRule.getSeverity())
                .withAction(command.getAction() != null ? command.getAction() : existingRule.getAction())
                .withEnabled(command.isEnabled() != null ? command.isEnabled() : existingRule.isEnabled())
                .withFrequencyCount(command.getFrequencyCount() != null ? command.getFrequencyCount() : existingRule.getFrequencyCount())
                .withSequenceSteps(command.getSequenceSteps() != null ? command.getSequenceSteps() : existingRule.getSequenceSteps())
                .withMetadata(command.getMetadata() != null ? command.getMetadata() : existingRule.getMetadata());
        
        // 룰 검증
        RuleValidationService.ValidationResult validationResult = ruleValidationService.validateRule(updatedRule);
        if (!validationResult.isValid()) {
            String errorMessage = "룰 업데이트 실패: " + validationResult.getErrorMessage();
            logger.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
        
        // 룰 저장
        Rule savedRule = ruleRepository.save(updatedRule);
        logger.info("✅ Rule updated successfully: {}", savedRule.getRuleId());
        
        return convertToDto(savedRule);
    }

    /**
     * 🔍 룰 조회
     */
    public Optional<RuleDto> findRule(String ruleId) {
        logger.debug("🔍 Finding rule: {}", ruleId);
        
        return ruleRepository.findById(ruleId)
                .map(this::convertToDto);
    }

    /**
     * 📋 모든 룰 조회
     */
    public List<RuleDto> findAllRules() {
        logger.debug("📋 Finding all rules");
        
        return ruleRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 📋 활성화된 룰 조회
     */
    public List<RuleDto> findActiveRules() {
        logger.debug("📋 Finding active rules");
        
        return ruleRepository.findByEnabled(true)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 📋 룰 타입별 조회
     */
    public List<RuleDto> findRulesByType(Rule.RuleType ruleType) {
        logger.debug("📋 Finding rules by type: {}", ruleType);
        
        return ruleRepository.findByRuleType(ruleType)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 📋 심각도별 조회
     */
    public List<RuleDto> findRulesBySeverity(Rule.Severity severity) {
        logger.debug("📋 Finding rules by severity: {}", severity);
        
        return ruleRepository.findBySeverity(severity)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 🗑️ 룰 삭제
     */
    public void deleteRule(String ruleId) {
        logger.info("🗑️ Deleting rule: {}", ruleId);
        
        if (!ruleRepository.existsById(ruleId)) {
            String errorMessage = "룰을 찾을 수 없습니다: " + ruleId;
            logger.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
        
        ruleRepository.deleteById(ruleId);
        logger.info("✅ Rule deleted successfully: {}", ruleId);
    }

    /**
     * 🔄 룰 활성화/비활성화
     */
    public RuleDto toggleRuleEnabled(String ruleId) {
        logger.info("🔄 Toggling rule enabled state: {}", ruleId);
        
        Optional<Rule> ruleOpt = ruleRepository.findById(ruleId);
        if (!ruleOpt.isPresent()) {
            String errorMessage = "룰을 찾을 수 없습니다: " + ruleId;
            logger.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
        
        Rule rule = ruleOpt.get();
        Rule updatedRule = new Rule(rule.getRuleId(), rule.getRuleName(), rule.getRuleType())
                .withConditions(rule.getConditions())
                .withTimeWindow(rule.getTimeWindow())
                .withSeverity(rule.getSeverity())
                .withAction(rule.getAction())
                .withEnabled(!rule.isEnabled())
                .withFrequencyCount(rule.getFrequencyCount())
                .withSequenceSteps(rule.getSequenceSteps())
                .withMetadata(rule.getMetadata());
        
        Rule savedRule = ruleRepository.save(updatedRule);
        logger.info("✅ Rule enabled state toggled: {} -> {}", ruleId, savedRule.isEnabled());
        
        return convertToDto(savedRule);
    }

    /**
     * 📊 룰 통계 조회
     */
    public RuleStatistics getRuleStatistics() {
        logger.debug("📊 Getting rule statistics");
        
        List<Rule> allRules = ruleRepository.findAll();
        
        long totalRules = allRules.size();
        long activeRules = allRules.stream().filter(Rule::isEnabled).count();
        long inactiveRules = totalRules - activeRules;
        
        long highPriorityRules = allRules.stream()
                .filter(Rule::isHighPriorityRule)
                .count();
        
        return new RuleStatistics(totalRules, activeRules, inactiveRules, highPriorityRules);
    }

    /**
     * 🔄 도메인 모델을 DTO로 변환
     */
    private RuleDto convertToDto(Rule rule) {
        return new RuleDto(
                rule.getRuleId(),
                rule.getRuleName(),
                rule.getRuleType(),
                rule.getConditions(),
                rule.getTimeWindow(),
                rule.getSeverity(),
                rule.getAction(),
                rule.isEnabled(),
                rule.getFrequencyCount(),
                rule.getSequenceSteps(),
                rule.getMetadata()
        );
    }

    /**
     * 📊 룰 통계 클래스
     */
    public static class RuleStatistics {
        private final long totalRules;
        private final long activeRules;
        private final long inactiveRules;
        private final long highPriorityRules;

        public RuleStatistics(long totalRules, long activeRules, long inactiveRules, long highPriorityRules) {
            this.totalRules = totalRules;
            this.activeRules = activeRules;
            this.inactiveRules = inactiveRules;
            this.highPriorityRules = highPriorityRules;
        }

        public long getTotalRules() { return totalRules; }
        public long getActiveRules() { return activeRules; }
        public long getInactiveRules() { return inactiveRules; }
        public long getHighPriorityRules() { return highPriorityRules; }

        @Override
        public String toString() {
            return String.format("RuleStatistics{total=%d, active=%d, inactive=%d, highPriority=%d}",
                    totalRules, activeRules, inactiveRules, highPriorityRules);
        }
    }
} 