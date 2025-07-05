package com.flinkapi.cep.application.port;

import com.flinkapi.cep.domain.model.Rule;

import java.util.List;
import java.util.Optional;

/**
 *  룰 리포지토리 포트 인터페이스 - 룰 영속성 계층의 추상화
 * 
 */
public interface RuleRepository {
    
    /**
     * 룰 저장
     */
    Rule save(Rule rule);
    
    /**
     * 룰 ID로 조회
     */
    Optional<Rule> findById(String ruleId);
    
    /**
     * 모든 룰 조회
     */
    List<Rule> findAll();
    
    /**
     * 활성화 상태로 룰 조회
     */
    List<Rule> findByEnabled(boolean enabled);
    
    /**
     * 룰 타입으로 조회
     */
    List<Rule> findByRuleType(Rule.RuleType ruleType);
    
    /**
     * 심각도로 조회
     */
    List<Rule> findBySeverity(Rule.Severity severity);
    
    /**
     * 룰 존재 여부 확인
     */
    boolean existsById(String ruleId);
    
    /**
     * 룰 삭제
     */
    void deleteById(String ruleId);
    
    /**
     * 모든 룰 삭제
     */
    void deleteAll();
    
    /**
     * 룰 개수 조회
     */
    long count();
} 