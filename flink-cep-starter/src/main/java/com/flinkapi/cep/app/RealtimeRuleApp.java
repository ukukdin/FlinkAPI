package com.flinkapi.cep.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flinkapi.cep.engine.RuleEngine;
import com.flinkapi.cep.model.Event;
import com.flinkapi.cep.model.Rule;
import com.flinkapi.cep.web.controller.RuleController;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.PrintSinkFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;

/**
 * 🚀 실시간 룰 탐지 애플리케이션 - 바이브 코딩의 결정체!
 * Flink CEP를 활용한 초강력 실시간 룰 엔진입니다.
 */
public class RealtimeRuleApp {
    
    private static final Logger logger = LoggerFactory.getLogger(RealtimeRuleApp.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    public static void main(String[] args) throws Exception {
        logger.info("🚀 Starting Realtime Rule Detection App...");
        
        // Flink 실행 환경 설정
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(2);
        
        // 룰 엔진 초기화
        RuleEngine ruleEngine = new RuleEngine();
        
        // 🔗 웹 컨트롤러와 연결 (동적 룰 등록 가능)
        RuleController.setGlobalRuleEngine(ruleEngine);
        logger.info("🔗 Connected web controller to Flink engine for dynamic rule registration!");
        
        // 🔥 바이브한 룰들을 등록!
        registerAwesomeRules(ruleEngine);
        
        // 이벤트 스트림 생성 (Kafka 또는 테스트 데이터)
        DataStream<Event> eventStream = createEventStream(env);
        
        // 룰 엔진 적용하여 알림 스트림 생성
        DataStream<RuleEngine.RuleMatchResult> alertStream = ruleEngine.applyRules(eventStream);
        
        // 🚨 알림 결과 출력
        alertStream
            .map(new AlertProcessorFunction())
            .print("🚨 ALERT");
        
        // 📊 통계 정보 출력
        eventStream
            .map(new EventStatsFunction())
            .filter(stats -> stats != null)
            .print("📊 STATS");
        
        // 애플리케이션 실행
        logger.info("🎯 Executing Flink job...");
        env.execute("Realtime Rule Detection App");
    }
    
    /**
     * 🔥 바이브한 룰들을 등록!
     */
    private static void registerAwesomeRules(RuleEngine ruleEngine) {
        logger.info("🔥 Registering awesome rules...");
        
        // 룰 1: 고액 거래 탐지
        Rule highAmountRule = new Rule("rule-001", "고액 거래 탐지", Rule.RuleType.SINGLE_EVENT)
                .withConditions(Arrays.asList(
                    new Rule.Condition("amount", Rule.Operator.GREATER_THAN, 50000.0)
                ))
                .withSeverity(Rule.Severity.HIGH)
                .withAction("ALERT_HIGH_AMOUNT")
                .withTimeWindow(new Rule.TimeWindow(5, Rule.TimeUnit.MINUTES));
        
        // 룰 2: 해외 거래 탐지
        Rule foreignTransactionRule = new Rule("rule-002", "해외 거래 탐지", Rule.RuleType.SINGLE_EVENT)
                .withConditions(Arrays.asList(
                    new Rule.Condition("region", Rule.Operator.NOT_EQUALS, "KR")
                ))
                .withSeverity(Rule.Severity.MEDIUM)
                .withAction("ALERT_FOREIGN_TRANSACTION");
        
        // 룰 3: 빈번한 거래 탐지 (5분 내 3회 이상)
        Rule frequentTransactionRule = new Rule("rule-003", "빈번한 거래 탐지", Rule.RuleType.FREQUENCY)
                .withConditions(Arrays.asList(
                    new Rule.Condition("eventType", Rule.Operator.EQUALS, "TRANSACTION")
                ))
                .withSeverity(Rule.Severity.MEDIUM)
                .withAction("ALERT_FREQUENT_TRANSACTION")
                .withTimeWindow(new Rule.TimeWindow(5, Rule.TimeUnit.MINUTES));
        
        // 룰 4: 모바일 디바이스에서의 고액 거래
        Rule mobileHighAmountRule = new Rule("rule-004", "모바일 고액 거래 탐지", Rule.RuleType.SINGLE_EVENT)
                .withConditions(Arrays.asList(
                    new Rule.Condition("deviceType", Rule.Operator.EQUALS, "mobile"),
                    new Rule.Condition("amount", Rule.Operator.GREATER_THAN, 30000.0)
                        .withLogicalOperator(Rule.LogicalOperator.AND)
                ))
                .withSeverity(Rule.Severity.HIGH)
                .withAction("ALERT_MOBILE_HIGH_AMOUNT");
        
        // 룰 5: 이상 지역 접근 탐지
        Rule anomalyLocationRule = new Rule("rule-005", "이상 지역 접근 탐지", Rule.RuleType.ANOMALY)
                .withConditions(Arrays.asList(
                    new Rule.Condition("region", Rule.Operator.IN, Arrays.asList("US", "CN", "RU"))
                ))
                .withSeverity(Rule.Severity.CRITICAL)
                .withAction("ALERT_SUSPICIOUS_LOCATION")
                .withTimeWindow(new Rule.TimeWindow(10, Rule.TimeUnit.MINUTES));
        
        // 룰 6: 중국 소액 이체 빈발 패턴 탐지 🇨🇳
        Rule chinaSmallTransferRule = new Rule("rule-006", "중국 소액 이체 빈발 패턴 탐지", Rule.RuleType.FREQUENCY)
                .withConditions(Arrays.asList(
                    new Rule.Condition("region", Rule.Operator.EQUALS, "CN"),
                    new Rule.Condition("eventType", Rule.Operator.EQUALS, "TRANSACTION")
                        .withLogicalOperator(Rule.LogicalOperator.AND),
                    new Rule.Condition("amount", Rule.Operator.LESS_THAN_OR_EQUAL, 300000.0)
                        .withLogicalOperator(Rule.LogicalOperator.AND)
                ))
                .withSeverity(Rule.Severity.HIGH)
                .withAction("ALERT_CHINA_SMALL_TRANSFER_PATTERN")
                .withTimeWindow(new Rule.TimeWindow(1, Rule.TimeUnit.MINUTES));
        
        // 룰들 등록
        ruleEngine.registerRule(highAmountRule);
        ruleEngine.registerRule(foreignTransactionRule);
        ruleEngine.registerRule(frequentTransactionRule);
        ruleEngine.registerRule(mobileHighAmountRule);
        ruleEngine.registerRule(anomalyLocationRule);
        ruleEngine.registerRule(chinaSmallTransferRule);
        
        logger.info("✅ {} rules registered successfully!", ruleEngine.getAllRules().size());
    }
    
    /**
     * 🌊 이벤트 스트림 생성 (Kafka 또는 테스트 데이터)
     */
    private static DataStream<Event> createEventStream(StreamExecutionEnvironment env) {
        logger.info("🌊 Creating event stream...");
        
        // 개발/테스트용 인메모리 데이터 소스
        return env.addSource(new TestEventSourceFunction())
                .name("Test Event Source");
        
        // 실제 환경에서는 Kafka 소스 사용
        /*
        KafkaSource<String> kafkaSource = KafkaSource.<String>builder()
                .setBootstrapServers("localhost:9092")
                .setTopics("events")
                .setGroupId("rule-engine-group")
                .setStartingOffsets(Offsets.latest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();
        
        return env.fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), "Kafka Event Source")
                .map(new MapFunction<String, Event>() {
                    @Override
                    public Event map(String jsonString) throws Exception {
                        return objectMapper.readValue(jsonString, Event.class);
                    }
                });
        */
    }
    
    /**
     * 🎲 테스트용 이벤트 생성기
     */
    private static class TestEventSourceFunction implements SourceFunction<Event> {
        
        private volatile boolean running = true;
        private final Random random = new Random();
        private final String[] userIds = {"user001", "user002", "user003", "user004", "user005"};
        private final String[] regions = {"KR", "US", "JP", "CN", "UK", "RU"};
        private final String[] deviceTypes = {"mobile", "desktop", "tablet"};
        private final String[] eventTypes = {"TRANSACTION", "LOGIN", "LOGOUT", "VIEW"};
        
        @Override
        public void run(SourceContext<Event> ctx) throws Exception {
            logger.info("🎲 Starting test event generation...");
            
            while (running) {
                // 🔥 바이브한 테스트 이벤트 생성!
                String eventType = randomChoice(eventTypes);
                String region = randomChoice(regions);
                
                // 중국 소액 이체 패턴을 더 자주 생성 (20% 확률)
                if (random.nextDouble() < 0.2) {
                    eventType = "TRANSACTION";
                    region = "CN";
                }
                
                Event event = new Event(UUID.randomUUID().toString(), eventType)
                        .withUserId(randomChoice(userIds))
                        .withAmount(generateRandomAmount())
                        .withRegion(region)
                        .withDeviceType(randomChoice(deviceTypes))
                        .withIpAddress(generateRandomIP())
                        .withTimestamp(Instant.now().toEpochMilli());
                
                ctx.collect(event);
                
                // 0.5-1.5초 간격으로 이벤트 생성 (더 빠른 테스트)
                Thread.sleep(500 + random.nextInt(1000));
            }
        }
        
        @Override
        public void cancel() {
            logger.info("🛑 Stopping test event generation...");
            running = false;
        }
        
        private String randomChoice(String[] array) {
            return array[random.nextInt(array.length)];
        }
        
        private Double generateRandomAmount() {
            // 대부분 작은 금액, 가끔 큰 금액
            if (random.nextDouble() < 0.1) { // 10% 확률로 고액
                return 30000.0 + random.nextDouble() * 100000.0;
            } else if (random.nextDouble() < 0.3) { // 30% 확률로 중국 소액 이체 시뮬레이션
                return 50000.0 + random.nextDouble() * 200000.0; // 5-25만원
            } else {
                return random.nextDouble() * 10000.0;
            }
        }
        
        private String generateRandomIP() {
            return String.format("%d.%d.%d.%d", 
                random.nextInt(256), random.nextInt(256), 
                random.nextInt(256), random.nextInt(256));
        }
    }
    
    /**
     * 🚨 알림 처리 함수
     */
    private static class AlertProcessorFunction implements MapFunction<RuleEngine.RuleMatchResult, String> {
        
        @Override
        public String map(RuleEngine.RuleMatchResult result) throws Exception {
            StringBuilder alert = new StringBuilder();
            alert.append("\n🚨🚨🚨 RULE MATCH DETECTED 🚨🚨🚨\n");
            alert.append("═".repeat(50)).append("\n");
            alert.append("📋 Rule: ").append(result.getRuleName()).append("\n");
            alert.append("🆔 Rule ID: ").append(result.getRuleId()).append("\n");
            alert.append("📊 Severity: ").append(result.getSeverity()).append("\n");
            alert.append("🎯 Type: ").append(result.getRuleType()).append("\n");
            alert.append("⏰ Match Time: ").append(Instant.ofEpochMilli(result.getMatchTime())).append("\n");
            alert.append("🎬 Action: ").append(result.getAction()).append("\n");
            alert.append("═".repeat(50)).append("\n");
            alert.append(result.getMessage()).append("\n");
            alert.append("═".repeat(50)).append("\n");
            
            // 심각도에 따른 추가 처리
            if (result.getSeverity() == Rule.Severity.CRITICAL) {
                alert.append("🚨 CRITICAL ALERT - IMMEDIATE ACTION REQUIRED!\n");
            }
            
            return alert.toString();
        }
    }
    
    /**
     * 📊 이벤트 통계 함수
     */
    private static class EventStatsFunction implements MapFunction<Event, String> {
        
        private long eventCount = 0;
        
        @Override
        public String map(Event event) throws Exception {
            eventCount++;
            
            if (eventCount % 10 == 0) { // 10개마다 통계 출력
                return String.format("📊 Processed %d events | Latest: %s | User: %s | Amount: %.2f | Region: %s",
                        eventCount, event.getEventType(), event.getUserId(), 
                        event.getAmount() != null ? event.getAmount() : 0.0, event.getRegion());
            }
            
            return null; // 필터링됨
        }
    }
} 