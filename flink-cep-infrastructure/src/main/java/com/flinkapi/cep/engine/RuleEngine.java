package com.flinkapi.cep.engine;

import com.flinkapi.cep.model.Event;
import com.flinkapi.cep.model.Rule;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternSelectFunction;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 실시간 룰 엔진 - 바이브 코딩으로 만든 초강력 엔진!
 * 동적으로 룰을 등록하고 실시간으로 패턴을 탐지합니다.
 */
public class RuleEngine {
    
    private static final Logger logger = LoggerFactory.getLogger(RuleEngine.class);
    
    // 등록된 룰들을 저장하는 맵
    private final Map<String, Rule> rules = new ConcurrentHashMap<>();
    
    // 룰 매칭 결과를 저장하는 맵
    private final Map<String, RuleMatchResult> matchResults = new ConcurrentHashMap<>();
    
    /**
     * 룰 등록 - 바이브하게 룰을 추가!
     */
    public void registerRule(Rule rule) {
        if (rule == null || rule.getRuleId() == null) {
            throw new IllegalArgumentException("룰과 룰 ID는 필수입니다!");
        }
        
        logger.info("Registering rule: {} ({})", rule.getRuleName(), rule.getRuleId());
        rules.put(rule.getRuleId(), rule);
    }
    
    /**
     *  룰 제거
     */
    public void unregisterRule(String ruleId) {
        Rule removedRule = rules.remove(ruleId);
        if (removedRule != null) {
            logger.info("Unregistered rule: {} ({})", removedRule.getRuleName(), ruleId);
        }
    }
    
    /**
     * 등록된 모든 룰 조회
     */
    public Map<String, Rule> getAllRules() {
        return new ConcurrentHashMap<>(rules);
    }
    
    /**
     * 특정 룰 조회
     */
    public Rule getRule(String ruleId) {
        return rules.get(ruleId);
    }
    
    /**
     * 이벤트 스트림에 모든 룰을 적용하여 알림 스트림 생성
     */
    public DataStream<RuleMatchResult> applyRules(DataStream<Event> eventStream) {
        logger.info("Applying {} rules to event stream", rules.size());
        
        // 모든 룰에 대해 패턴 매칭을 수행하고 결과를 합침
        DataStream<RuleMatchResult> alertStream = null;
        
        for (Rule rule : rules.values()) {
            if (!rule.isEnabled()) {
                logger.debug("⏸Skipping disabled rule: {}", rule.getRuleName());
                continue;
            }
            
            try {
                DataStream<RuleMatchResult> ruleAlerts = applyRule(eventStream, rule);
                
                if (alertStream == null) {
                    alertStream = ruleAlerts;
                } else {
                    alertStream = alertStream.union(ruleAlerts);
                }
                
            } catch (Exception e) {
                logger.error("Error applying rule {}: {}", rule.getRuleName(), e.getMessage(), e);
            }
        }
        
        return alertStream != null ? alertStream : 
               eventStream.map(new MapFunction<Event, RuleMatchResult>() {
                   @Override
                   public RuleMatchResult map(Event event) throws Exception {
                       // 매칭되는 룰이 없을 때 빈 결과 반환
                       return null;
                   }
               }).filter(result -> result != null);
    }
    
    /**
     * 단일 룰을 이벤트 스트림에 적용
     */
    public DataStream<RuleMatchResult> applyRule(DataStream<Event> eventStream, Rule rule) {
        logger.info("Applying rule: {} ({})", rule.getRuleName(), rule.getRuleId());
        
        // CEP 패턴 생성
        Pattern<Event, ?> pattern = CEPPatternBuilder.buildPattern(rule);
        
        // 패턴을 이벤트 스트림에 적용
        PatternStream<Event> patternStream = CEP.pattern(
            eventStream.keyBy(Event::getUserId), // 사용자별로 키 분할
            pattern
        );
        
        // 패턴 매칭 결과를 RuleMatchResult로 변환
        return patternStream.select(new RulePatternSelectFunction(rule));
    }
    
    /**
     * 룰 매칭 통계 조회
     */
    public Map<String, Long> getRuleMatchingStats() {
        Map<String, Long> stats = new ConcurrentHashMap<>();
        
        for (Map.Entry<String, RuleMatchResult> entry : matchResults.entrySet()) {
            RuleMatchResult result = entry.getValue();
            stats.put(result.getRuleId(), result.getMatchCount());
        }
        
        return stats;
    }
    
    /**
     * 패턴 매칭 결과를 처리하는 함수
     */
    private static class RulePatternSelectFunction implements PatternSelectFunction<Event, RuleMatchResult> {
        
        private final Rule rule;
        
        public RulePatternSelectFunction(Rule rule) {
            this.rule = rule;
        }
        
        @Override
        public RuleMatchResult select(Map<String, List<Event>> pattern) throws Exception {
            logger.info("Pattern matched for rule: {} - {}", rule.getRuleId(), rule.getRuleName());
            
            // 매칭된 이벤트들 수집
            List<Event> matchedEvents = pattern.values().iterator().next();
            Event triggerEvent = matchedEvents.get(0); // 첫 번째 이벤트를 트리거로 사용
            
            // 매칭 결과 생성
            RuleMatchResult result = new RuleMatchResult()
                    .withRuleId(rule.getRuleId())
                    .withRuleName(rule.getRuleName())
                    .withRuleType(rule.getRuleType())
                    .withSeverity(rule.getSeverity())
                    .withTriggerEvent(triggerEvent)
                    .withMatchedEvents(matchedEvents)
                    .withMatchTime(Instant.now().toEpochMilli())
                    .withAction(rule.getAction())
                    .withMessage(generateAlertMessage(rule, triggerEvent));
            
            return result;
        }
        
        /**
         * 알림 메시지 생성
         */
        private String generateAlertMessage(Rule rule, Event triggerEvent) {
            StringBuilder message = new StringBuilder();
            message.append("ALERT: ").append(rule.getRuleName()).append(" 룰이 탐지되었습니다!\n");
            message.append("심각도: ").append(rule.getSeverity().getDescription()).append("\n");
            message.append("사용자: ").append(triggerEvent.getUserId()).append("\n");
            
            if (triggerEvent.getAmount() != null) {
                message.append("금액: ").append(triggerEvent.getAmount()).append("\n");
            }
            
            if (triggerEvent.getRegion() != null) {
                message.append("지역: ").append(triggerEvent.getRegion()).append("\n");
            }
            
            if (triggerEvent.getDeviceType() != null) {
                message.append("디바이스: ").append(triggerEvent.getDeviceType()).append("\n");
            }
            
            message.append("시간: ").append(Instant.ofEpochMilli(triggerEvent.getTimestamp()));
            
            return message.toString();
        }
    }
    
    /**
     * 룰 매칭 결과 클래스
     */
    public static class RuleMatchResult {
        private String ruleId;
        private String ruleName;
        private Rule.RuleType ruleType;
        private Rule.Severity severity;
        private Event triggerEvent;
        private List<Event> matchedEvents;
        private long matchTime;
        private String action;
        private String message;
        private long matchCount = 1;
        
        // Fluent API 스타일 빌더
        public RuleMatchResult withRuleId(String ruleId) {
            this.ruleId = ruleId;
            return this;
        }
        
        public RuleMatchResult withRuleName(String ruleName) {
            this.ruleName = ruleName;
            return this;
        }
        
        public RuleMatchResult withRuleType(Rule.RuleType ruleType) {
            this.ruleType = ruleType;
            return this;
        }
        
        public RuleMatchResult withSeverity(Rule.Severity severity) {
            this.severity = severity;
            return this;
        }
        
        public RuleMatchResult withTriggerEvent(Event triggerEvent) {
            this.triggerEvent = triggerEvent;
            return this;
        }
        
        public RuleMatchResult withMatchedEvents(List<Event> matchedEvents) {
            this.matchedEvents = matchedEvents;
            return this;
        }
        
        public RuleMatchResult withMatchTime(long matchTime) {
            this.matchTime = matchTime;
            return this;
        }
        
        public RuleMatchResult withAction(String action) {
            this.action = action;
            return this;
        }
        
        public RuleMatchResult withMessage(String message) {
            this.message = message;
            return this;
        }
        
        public RuleMatchResult withMatchCount(long matchCount) {
            this.matchCount = matchCount;
            return this;
        }
        
        // Getters
        public String getRuleId() { return ruleId; }
        public String getRuleName() { return ruleName; }
        public Rule.RuleType getRuleType() { return ruleType; }
        public Rule.Severity getSeverity() { return severity; }
        public Event getTriggerEvent() { return triggerEvent; }
        public List<Event> getMatchedEvents() { return matchedEvents; }
        public long getMatchTime() { return matchTime; }
        public String getAction() { return action; }
        public String getMessage() { return message; }
        public long getMatchCount() { return matchCount; }
        
        @Override
        public String toString() {
            return String.format("RuleMatchResult{ruleId='%s', ruleName='%s', severity=%s, matchTime=%d}",
                    ruleId, ruleName, severity, matchTime);
        }
    }
} 