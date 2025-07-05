package com.flinkapi.cep;

import com.flinkapi.cep.engine.RuleEngine;
import com.flinkapi.cep.model.Event;
import com.flinkapi.cep.model.Rule;
import com.flinkapi.cep.engine.RuleParserService;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Flink CEP Library - Main API Entry Point
 * 
 * 사용 예시:
 * <pre>
 * FlinkCEPLibrary cepLib = FlinkCEPLibrary.builder()
 *     .withExecutionEnvironment(env)
 *     .withAlertHandler(alert -> System.out.println("Alert: " + alert))
 *     .build();
 *     
 * cepLib.addRule("한국에서 거래 이후 중국에서 로그인", "위험패턴");
 * cepLib.processEvents(eventStream);
 * </pre>
 */
public class FlinkCEPLibrary {
    
    private final StreamExecutionEnvironment env;
    private final RuleEngine ruleEngine;
    private final RuleParserService ruleParserService;
    private final Consumer<String> alertHandler;
    
    private FlinkCEPLibrary(Builder builder) {
        this.env = builder.env;
        this.ruleEngine = new RuleEngine();
        this.ruleParserService = new RuleParserService();
        this.alertHandler = builder.alertHandler != null ? builder.alertHandler : this::defaultAlertHandler;
    }
    
    /**
     * 자연어 룰을 추가합니다.
     * 
     * @param ruleText 자연어로 작성된 룰 (예: "한국에서 거래 이후 중국에서 로그인")
     * @param ruleName 룰 이름
     * @return 성공 여부
     */
    public boolean addRule(String ruleText, String ruleName) {
        try {
            Rule rule = ruleParserService.parseRule(ruleText, ruleName);
            ruleEngine.registerRule(rule);
            return true;
        } catch (Exception e) {
            System.err.println("Failed to add rule: " + ruleText + " - " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 룰 객체를 직접 추가합니다.
     * 
     * @param rule 룰 객체
     */
    public void addRule(Rule rule) {
        ruleEngine.registerRule(rule);
    }
    
    /**
     * 이벤트 스트림을 처리하여 패턴을 탐지합니다.
     * 
     * @param eventStream 이벤트 데이터 스트림
     */
    public void processEvents(DataStream<Event> eventStream) {
        DataStream<RuleEngine.RuleMatchResult> alertStream = ruleEngine.applyRules(eventStream);
        
        // RuleMatchResult를 String으로 변환하여 알림 핸들러에 전달
        DataStream<String> alerts = alertStream.map(result -> result.getMessage());
        alerts.addSink(new FlinkCEPSink(alertHandler));
    }
    
    /**
     * 등록된 모든 룰을 반환합니다.
     * 
     * @return 룰 목록
     */
    public List<Rule> getAllRules() {
        return new ArrayList<>(ruleEngine.getAllRules().values());
    }
    
    /**
     * 특정 룰을 제거합니다.
     * 
     * @param ruleId 룰 ID
     * @return 제거 성공 여부
     */
    public boolean removeRule(String ruleId) {
        try {
            ruleEngine.unregisterRule(ruleId);
            return true;
        } catch (Exception e) {
            System.err.println("Failed to remove rule: " + ruleId + " - " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 모든 룰을 제거합니다.
     */
    public void clearAllRules() {
        // 모든 룰 ID를 가져와서 하나씩 제거
        List<String> ruleIds = new ArrayList<>(ruleEngine.getAllRules().keySet());
        for (String ruleId : ruleIds) {
            ruleEngine.unregisterRule(ruleId);
        }
    }
    
    /**
     * 룰 매칭 통계를 반환합니다.
     * 
     * @return 룰별 매칭 횟수
     */
    public java.util.Map<String, Long> getMatchingStats() {
        return ruleEngine.getRuleMatchingStats();
    }
    
    /**
     * 기본 알림 핸들러
     */
    private void defaultAlertHandler(String alert) {
        System.out.println("[CEP Alert] " + alert);
    }
    
    /**
     * Builder 패턴으로 FlinkCEPLibrary 인스턴스를 생성합니다.
     * 
     * @return Builder 인스턴스
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * FlinkCEPLibrary Builder 클래스
     */
    public static class Builder {
        private StreamExecutionEnvironment env;
        private Consumer<String> alertHandler;
        
        /**
         * Flink 실행 환경을 설정합니다.
         * 
         * @param env StreamExecutionEnvironment
         * @return Builder
         */
        public Builder withExecutionEnvironment(StreamExecutionEnvironment env) {
            this.env = env;
            return this;
        }
        
        /**
         * 알림 핸들러를 설정합니다.
         * 
         * @param handler 알림 처리 함수
         * @return Builder
         */
        public Builder withAlertHandler(Consumer<String> handler) {
            this.alertHandler = handler;
            return this;
        }
        
        /**
         * FlinkCEPLibrary 인스턴스를 생성합니다.
         * 
         * @return FlinkCEPLibrary 인스턴스
         */
        public FlinkCEPLibrary build() {
            if (env == null) {
                throw new IllegalArgumentException("StreamExecutionEnvironment is required");
            }
            return new FlinkCEPLibrary(this);
        }
    }
    
    /**
     * 간단한 싱크 구현체
     */
    private static class FlinkCEPSink implements org.apache.flink.streaming.api.functions.sink.SinkFunction<String> {
        private final Consumer<String> alertHandler;
        
        public FlinkCEPSink(Consumer<String> alertHandler) {
            this.alertHandler = alertHandler;
        }
        
        @Override
        public void invoke(String value, Context context) {
            alertHandler.accept(value);
        }
    }
} 