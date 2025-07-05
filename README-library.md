# Flink CEP Core Library

**자연어로 실시간 패턴 탐지 룰을 생성하는 Apache Flink CEP 라이브러리**

## 🚀 특징

- **자연어 룰 파싱**: "한국에서 거래 이후 중국에서 로그인" 같은 자연어를 CEP 패턴으로 변환
- **시퀀스 패턴 지원**: 다단계 이벤트 시퀀스 탐지
- **실시간 처리**: Apache Flink CEP 기반 실시간 스트림 처리
- **유연한 조건**: 금액, 지역, 디바이스, 시간 등 다양한 조건 지원
- **간단한 API**: Builder 패턴으로 쉬운 설정

## 📦 의존성

```xml
<dependency>
    <groupId>com.flinkapi</groupId>
    <artifactId>flink-cep-core</artifactId>
    <version>1.0.0</version>
</dependency>

<!-- Flink 의존성들 (provided scope) -->
<dependency>
    <groupId>org.apache.flink</groupId>
    <artifactId>flink-streaming-java</artifactId>
    <version>1.18.0</version>
</dependency>
<dependency>
    <groupId>org.apache.flink</groupId>
    <artifactId>flink-cep</artifactId>
    <version>1.18.0</version>
</dependency>
```

## 🛠️ 기본 사용법

### 1. 라이브러리 초기화

```java
import com.flinkapi.cep.FlinkCEPLibrary;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

// Flink 실행 환경 생성
StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

// CEP 라이브러리 생성
FlinkCEPLibrary cepLib = FlinkCEPLibrary.builder()
    .withExecutionEnvironment(env)
    .withAlertHandler(alert -> {
        System.out.println("🚨 탐지된 패턴: " + alert);
        // 여기에 알림 로직 추가 (이메일, Slack 등)
    })
    .build();
```

### 2. 자연어 룰 추가

```java
// 시퀀스 패턴 룰
cepLib.addRule("한국에서 거래 이후 중국에서 로그인", "해외_로그인_패턴");
cepLib.addRule("신규 기기에서 100만원 이상 이체 이후 30분안에 해외에서 로그인", "고위험_패턴");

// 단일 이벤트 룰
cepLib.addRule("100만원 이상 해외 거래", "고액_해외거래");
cepLib.addRule("새벽 시간대 50만원 이상 이체", "새벽_거래");
cepLib.addRule("모바일에서 10만원 이상 거래 5분에 3회 이상", "빈발_거래");
```

### 3. 이벤트 스트림 처리

```java
import com.flinkapi.cep.model.Event;

// 이벤트 스트림 생성 (예: Kafka, 파일 등에서)
DataStream<Event> eventStream = env.addSource(new YourEventSource());

// CEP 패턴 적용
cepLib.processEvents(eventStream);

// Flink 작업 실행
env.execute("Real-time Pattern Detection");
```

## 📊 Event 데이터 모델

```java
Event event = new Event()
    .withEventId("event_123")
    .withUserId("user_001")
    .withEventType("TRANSACTION")
    .withAmount(1500000.0)
    .withRegion("한국")
    .withDeviceType("MOBILE")
    .withIsNewDevice(false)
    .withTimestamp(System.currentTimeMillis());
```

## 🎯 지원하는 룰 패턴

### 시퀀스 패턴
- `"A 이후 B"`: A 이벤트 발생 후 B 이벤트 발생
- `"A 후 B"`: A 이벤트 발생 후 B 이벤트 발생  
- `"A 다음에 B"`: A 이벤트 발생 다음에 B 이벤트 발생
- `"A 그리고 B"`: A와 B 이벤트가 순차적으로 발생

### 조건 키워드
- **금액**: `100만원 이상`, `50만원 이하`, `30만원 초과`, `10만원 미만`
- **지역**: `한국`, `중국`, `미국`, `일본`, `해외`
- **디바이스**: `모바일`, `데스크탑`, `PC`, `신규 기기`
- **시간**: `30분안에`, `1시간동안`, `새벽`, `야간`, `휴일`
- **나이**: `65세 이상`, `18세 미만`
- **성별**: `남자`, `여자`, `남성`, `여성`
- **빈도**: `3회 이상`, `5번`, `2건`

## 📈 고급 사용법

### 룰 관리

```java
// 등록된 룰 조회
List<Rule> rules = cepLib.getAllRules();
System.out.println("총 " + rules.size() + "개 룰이 등록됨");

// 특정 룰 제거
cepLib.removeRule("rule-12345");

// 모든 룰 제거
cepLib.clearAllRules();

// 매칭 통계 조회
Map<String, Long> stats = cepLib.getMatchingStats();
stats.forEach((ruleId, count) -> 
    System.out.println("Rule " + ruleId + ": " + count + "회 매칭")
);
```

### 직접 Rule 객체 생성

```java
import com.flinkapi.cep.model.Rule;

Rule customRule = new Rule("custom-rule", "커스텀룰", Rule.RuleType.SINGLE_EVENT)
    .withConditions(Arrays.asList(
        new Rule.Condition("amount", Rule.Operator.GREATER_THAN, 1000000),
        new Rule.Condition("region", Rule.Operator.EQUALS, "해외")
    ))
    .withSeverity(Rule.Severity.HIGH)
    .withTimeWindow(new Rule.TimeWindow(30, Rule.TimeUnit.MINUTES))
    .withAction("BLOCK_TRANSACTION");

cepLib.addRule(customRule);
```

## 🔄 라이브러리 빌드

```bash
# 라이브러리 컴파일
mvn compile

# 테스트 실행
mvn test

# JAR 패키징
mvn package

# 의존성이 포함된 Fat JAR 생성
mvn package -P fat-jar
```

## 📝 사용 예시 프로젝트

```java
public class CEPExample {
    public static void main(String[] args) throws Exception {
        // 1. Flink 환경 설정
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        
        // 2. CEP 라이브러리 초기화
        FlinkCEPLibrary cepLib = FlinkCEPLibrary.builder()
            .withExecutionEnvironment(env)
            .withAlertHandler(alert -> {
                System.out.println("🚨 ALERT: " + alert);
                // 실제 알림 로직 (이메일, Slack, DB 저장 등)
            })
            .build();
        
        // 3. 룰 등록
        cepLib.addRule("한국에서 거래 이후 중국에서 로그인", "의심패턴1");
        cepLib.addRule("100만원 이상 새벽 거래", "고액야간거래");
        cepLib.addRule("신규 기기에서 50만원 이상 이체 3회", "신규기기고액");
        
        // 4. 이벤트 스트림 생성 (테스트용)
        DataStream<Event> events = env.fromElements(
            new Event("evt1", "user1", "TRANSACTION").withAmount(1200000).withRegion("한국"),
            new Event("evt2", "user1", "LOGIN").withRegion("중국").withTimestamp(System.currentTimeMillis() + 60000)
        );
        
        // 5. 패턴 탐지 시작
        cepLib.processEvents(events);
        
        // 6. 실행
        env.execute("CEP Pattern Detection");
    }
}
```

## 🎨 라이브러리 확장

라이브러리는 확장 가능하도록 설계되었습니다:

- **새로운 조건 타입 추가**: `RuleParserService`에 파싱 메서드 추가
- **커스텀 이벤트 타입**: `Event` 클래스 확장
- **알림 채널 추가**: `alertHandler`에 다양한 알림 로직 구현
- **성능 최적화**: Flink 설정 튜닝 및 파티셔닝 전략 적용

## 📞 지원 및 문의

- **Issues**: GitHub Issues를 통한 버그 리포트 및 기능 요청
- **Documentation**: 상세 API 문서는 Javadoc 참조
- **Examples**: `examples/` 디렉토리의 샘플 코드 참조

---

**Apache Flink CEP를 활용한 실시간 패턴 탐지를 쉽고 직관적으로! 🚀** 