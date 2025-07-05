package com.flinkapi.cep;

import com.flinkapi.cep.engine.CEPPatternBuilder;
import com.flinkapi.cep.engine.RuleEngine;
import com.flinkapi.cep.model.Event;
import com.flinkapi.cep.model.Rule;
import org.apache.flink.cep.pattern.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 룰 엔진 테스트 - 바이브 코딩으로 만든 테스트!
 */
public class RuleEngineTest {
    
    private RuleEngine ruleEngine;
    
    @BeforeEach
    void setUp() {
        ruleEngine = new RuleEngine();
    }
    
    @Test
    void testRuleRegistration() {
        // Given: 고액 거래 룰 생성
        Rule highAmountRule = new Rule("test-001", "테스트 고액 거래", Rule.RuleType.SINGLE_EVENT)
                .withConditions(Arrays.asList(
                    new Rule.Condition("amount", Rule.Operator.GREATER_THAN, 50000.0)
                ))
                .withSeverity(Rule.Severity.HIGH);
        
        // When: 룰 등록
        ruleEngine.registerRule(highAmountRule);
        
        // Then: 룰이 등록되었는지 확인
        assertEquals(1, ruleEngine.getAllRules().size());
        assertEquals(highAmountRule, ruleEngine.getRule("test-001"));
    }
    
    @Test
    void testRuleUnregistration() {
        // Given: 룰을 등록
        Rule rule = new Rule("test-002", "테스트 룰", Rule.RuleType.SINGLE_EVENT);
        ruleEngine.registerRule(rule);
        
        // When: 룰 제거
        ruleEngine.unregisterRule("test-002");
        
        // Then: 룰이 제거되었는지 확인
        assertEquals(0, ruleEngine.getAllRules().size());
        assertNull(ruleEngine.getRule("test-002"));
    }
    
    @Test
    void testEventCreation() {
        // Given & When: 바이브한 이벤트 생성
        Event event = new Event(UUID.randomUUID().toString(), "TRANSACTION")
                .withUserId("user123")
                .withAmount(75000.0)
                .withRegion("KR")
                .withDeviceType("mobile")
                .withTimestamp(Instant.now().toEpochMilli());
        
        // Then: 이벤트 속성 확인
        assertEquals("TRANSACTION", event.getEventType());
        assertEquals("user123", event.getUserId());
        assertEquals(75000.0, event.getAmount());
        assertEquals("KR", event.getRegion());
        assertEquals("mobile", event.getDeviceType());
        
        // 헬퍼 메서드 테스트
        assertTrue(event.isHighValueTransaction());
        assertTrue(event.isFromKorea());
        assertTrue(event.isMobileDevice());
    }
    
    @Test
    void testCEPPatternGeneration() {
        // Given: 단일 이벤트 룰
        Rule singleEventRule = new Rule("pattern-001", "패턴 테스트", Rule.RuleType.SINGLE_EVENT)
                .withConditions(Arrays.asList(
                    new Rule.Condition("amount", Rule.Operator.GREATER_THAN, 10000.0)
                ));
        
        // When: CEP 패턴 생성
        Pattern<Event, ?> pattern = CEPPatternBuilder.buildPattern(singleEventRule);
        
        // Then: 패턴이 생성되었는지 확인
        assertNotNull(pattern);
    }
    
    @Test
    void testRuleConditions() {
        // Given: 복합 조건 룰
        Rule complexRule = new Rule("complex-001", "복합 조건 테스트", Rule.RuleType.SINGLE_EVENT)
                .withConditions(Arrays.asList(
                    new Rule.Condition("amount", Rule.Operator.GREATER_THAN, 50000.0),
                    new Rule.Condition("region", Rule.Operator.NOT_EQUALS, "KR")
                        .withLogicalOperator(Rule.LogicalOperator.AND)
                ))
                .withSeverity(Rule.Severity.CRITICAL);
        
        // When: 룰 등록
        ruleEngine.registerRule(complexRule);
        
        // Then: 룰 속성 확인
        Rule retrievedRule = ruleEngine.getRule("complex-001");
        assertNotNull(retrievedRule);
        assertEquals(2, retrievedRule.getConditions().size());
        assertEquals(Rule.Severity.CRITICAL, retrievedRule.getSeverity());
    }
    
    @Test
    void testTimeWindow() {
        // Given: 시간 윈도우가 있는 룰
        Rule timeWindowRule = new Rule("time-001", "시간 윈도우 테스트", Rule.RuleType.FREQUENCY)
                .withTimeWindow(new Rule.TimeWindow(5, Rule.TimeUnit.MINUTES));
        
        // When & Then: 시간 윈도우 확인
        Rule.TimeWindow timeWindow = timeWindowRule.getTimeWindow();
        assertNotNull(timeWindow);
        assertEquals(5, timeWindow.getDuration());
        assertEquals(Rule.TimeUnit.MINUTES, timeWindow.getUnit());
        assertEquals(5 * 60 * 1000, timeWindow.toMilliseconds()); // 5분을 밀리초로
    }
    
    @Test
    void testRuleTypes() {
        // Given & When & Then: 모든 룰 타입 테스트
        assertEquals("단일 이벤트 룰", Rule.RuleType.SINGLE_EVENT.getDescription());
        assertEquals("시퀀스 패턴 룰", Rule.RuleType.SEQUENCE.getDescription());
        assertEquals("임계값 룰", Rule.RuleType.THRESHOLD.getDescription());
        assertEquals("빈도 기반 룰", Rule.RuleType.FREQUENCY.getDescription());
        assertEquals("이상 탐지 룰", Rule.RuleType.ANOMALY.getDescription());
    }
    
    @Test
    void testSeverityLevels() {
        // Given & When & Then: 심각도 레벨 테스트
        assertEquals(1, Rule.Severity.LOW.getLevel());
        assertEquals(2, Rule.Severity.MEDIUM.getLevel());
        assertEquals(3, Rule.Severity.HIGH.getLevel());
        assertEquals(4, Rule.Severity.CRITICAL.getLevel());
    }
    
    @Test
    void testOperators() {
        // Given & When & Then: 연산자 기호 테스트
        assertEquals("==", Rule.Operator.EQUALS.getSymbol());
        assertEquals("!=", Rule.Operator.NOT_EQUALS.getSymbol());
        assertEquals(">", Rule.Operator.GREATER_THAN.getSymbol());
        assertEquals(">=", Rule.Operator.GREATER_THAN_OR_EQUAL.getSymbol());
        assertEquals("<", Rule.Operator.LESS_THAN.getSymbol());
        assertEquals("<=", Rule.Operator.LESS_THAN_OR_EQUAL.getSymbol());
    }
} 