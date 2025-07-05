package com.flinkapi.cep.examples;

import com.flinkapi.cep.FlinkCEPLibrary;
import com.flinkapi.cep.model.Event;
import com.flinkapi.cep.model.Rule;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

import java.util.Arrays;
import java.util.Random;

/**
 * Flink CEP Core Library 사용 예시
 * 
 * 이 예시는 다음과 같은 기능을 보여줍니다:
 * 1. 라이브러리 초기화
 * 2. 자연어 룰 등록
 * 3. 직접 Rule 객체 생성
 * 4. 실시간 이벤트 스트림 처리
 */
public class CEPLibraryExample {

    public static void main(String[] args) throws Exception {
        
        System.out.println("Flink CEP Library Example 시작");
        
        // 1. Flink 실행 환경 설정
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        
        // 2. CEP 라이브러리 초기화
        FlinkCEPLibrary cepLib = FlinkCEPLibrary.builder()
            .withExecutionEnvironment(env)
            .withAlertHandler(alert -> {
                System.out.println("PATTERN DETECTED:");
                System.out.println(alert);
                System.out.println("=" .repeat(50));
            })
            .build();
        
        // 3. 자연어 룰 등록
        System.out.println("자연어 룰 등록 중...");
        
        // 시퀀스 패턴 룰들
        cepLib.addRule("한국에서 거래 이후 중국에서 로그인", "해외_로그인_패턴");
        cepLib.addRule("신규 기기에서 100만원 이상 이체 이후 30분안에 해외에서 로그인", "고위험_시퀀스");
        cepLib.addRule("100만원 이상 이체 이후 신규 기기에서 로그인", "고액_후_신규기기");
        
        // 단일 이벤트 룰들
        cepLib.addRule("500만원 이상 해외 거래", "고액_해외거래");
        cepLib.addRule("새벽 시간대 100만원 이상 이체", "새벽_고액거래");
        cepLib.addRule("모바일에서 50만원 이상 거래 5분에 3회 이상", "빈발_모바일거래");
        
        // 4. 직접 Rule 객체 생성 예시
        System.out.println("커스텀 룰 생성 중...");
        
        Rule customRule = new Rule("custom-001", "VIP고객_특별관리", Rule.RuleType.SINGLE_EVENT)
            .withConditions(Arrays.asList(
                new Rule.Condition("amount", Rule.Operator.GREATER_THAN, 10000000), // 1천만원 초과
                new Rule.Condition("region", Rule.Operator.NOT_EQUALS, "한국"),
                new Rule.Condition("isVipCustomer", Rule.Operator.EQUALS, true)
            ))
            .withSeverity(Rule.Severity.HIGH)
            .withTimeWindow(new Rule.TimeWindow(15, Rule.TimeUnit.MINUTES))
            .withAction("IMMEDIATE_CALL");
        
        cepLib.addRule(customRule);
        
        // 5. 등록된 룰 확인
        System.out.println("등록된 룰 목록:");
        cepLib.getAllRules().forEach(rule -> 
            System.out.println("  - " + rule.getRuleName() + " (" + rule.getRuleType() + ")")
        );
        
        // 6. 테스트 이벤트 스트림 생성
        System.out.println("테스트 이벤트 스트림 생성 중...");
        
        DataStream<Event> eventStream = env.addSource(new TestEventSource());
        
        // 7. CEP 패턴 적용
        System.out.println("패턴 탐지 시작...");
        cepLib.processEvents(eventStream);
        
        // 8. Flink 작업 실행
        env.execute("Flink CEP Library Example");
    }
    
    /**
     * 테스트용 이벤트 소스
     * 실제 환경에서는 Kafka, 파일, 데이터베이스 등의 소스를 사용
     */
    private static class TestEventSource implements SourceFunction<Event> {
        
        private volatile boolean running = true;
        private final Random random = new Random();
        private final String[] users = {"user001", "user002", "user003", "user004", "user005"};
        private final String[] regions = {"한국", "중국", "미국", "일본", "영국"};
        private final String[] devices = {"MOBILE", "DESKTOP"};
        private final String[] eventTypes = {"TRANSACTION", "LOGIN", "VIEW"};
        
        @Override
        public void run(SourceContext<Event> ctx) throws Exception {
            int eventCount = 0;
            
            while (running && eventCount < 50) { // 50개 이벤트 생성 후 종료
                
                // 시퀀스 패턴을 만들기 위한 특별한 이벤트 시나리오
                if (eventCount % 10 == 0) {
                    // 시나리오 1: 한국 거래 → 중국 로그인 패턴
                    String userId = users[random.nextInt(users.length)];
                    
                    // 1단계: 한국에서 거래
                    Event transactionEvent = new Event("evt_" + eventCount, "TRANSACTION")
                        .withUserId(userId)
                        .withAmount(random.nextDouble() * 2000000 + 100000) // 10만원~210만원
                        .withRegion("한국")
                        .withDeviceType(devices[random.nextInt(devices.length)])
                        .withTimestamp(System.currentTimeMillis());
                    
                    ctx.collect(transactionEvent);
                    System.out.println("📤 Generated: " + transactionEvent);
                    
                    Thread.sleep(1000); // 1초 대기
                    
                                         // 2단계: 중국에서 로그인 (30% 확률)
                     if (random.nextFloat() < 0.3f) {
                         Event loginEvent = new Event("evt_" + (eventCount + 1), "LOGIN")
                             .withUserId(userId)
                             .withRegion("중국")
                             .withDeviceType(devices[random.nextInt(devices.length)])
                             .withTimestamp(System.currentTimeMillis());
                         
                         ctx.collect(loginEvent);
                         System.out.println("📤 Generated: " + loginEvent);
                     }
                    
                                 } else {
                     // 일반적인 랜덤 이벤트
                     Event event = new Event("evt_" + eventCount, eventTypes[random.nextInt(eventTypes.length)])
                         .withUserId(users[random.nextInt(users.length)])
                         .withAmount(random.nextDouble() * 5000000) // 0~500만원
                         .withRegion(regions[random.nextInt(regions.length)])
                         .withDeviceType(devices[random.nextInt(devices.length)])
                         .withTimestamp(System.currentTimeMillis());
                     
                     ctx.collect(event);
                     System.out.println("📤 Generated: " + event);
                 }
                
                eventCount++;
                Thread.sleep(2000); // 2초마다 이벤트 생성
            }
            
            System.out.println("이벤트 생성 완료. 총 " + eventCount + "개 이벤트 생성됨");
        }
        
        @Override
        public void cancel() {
            running = false;
        }
    }
} 