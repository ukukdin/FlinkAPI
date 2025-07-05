# 🚀 FlinkAPI - DDD 구조로 설계된 실시간 CEP 라이브러리

## 📖 개요

FlinkAPI는 **Domain-Driven Design (DDD)** 패턴을 적용하여 설계된 Apache Flink 기반의 실시간 Complex Event Processing (CEP) 라이브러리입니다. 
이 라이브러리는 다른 개발자들이 쉽게 사용할 수 있도록 모듈화되고 확장 가능한 구조로 설계되었습니다.

## 🏗️ DDD 아키텍처 구조

```
com.flinkapi.cep
├── domain/                    # 도메인 레이어 (핵심 비즈니스 로직)
│   ├── model/                 # 도메인 엔티티
│   │   ├── Event.java        # 이벤트 엔티티
│   │   └── Rule.java         # 룰 엔티티
│   ├── value/                 # 값 객체 (Value Objects)
│   │   ├── RuleCondition.java # 룰 조건 값 객체
│   │   └── TimeWindow.java    # 시간 윈도우 값 객체
│   └── service/               # 도메인 서비스
│       ├── RuleValidationService.java      # 룰 검증 서비스
│       └── EventProcessingService.java     # 이벤트 처리 서비스
│
├── application/               # 응용 레이어 (유스케이스)
│   ├── service/               # 응용 서비스
│   │   ├── RuleManagementService.java      # 룰 관리 서비스
│   │   └── EventStreamingService.java      # 이벤트 스트리밍 서비스
│   ├── dto/                   # 데이터 전송 객체
│   │   ├── RuleDto.java       # 룰 DTO
│   │   ├── EventDto.java      # 이벤트 DTO
│   │   ├── AlertDto.java      # 알림 DTO
│   │   ├── RuleCreateCommand.java  # 룰 생성 명령
│   │   └── RuleUpdateCommand.java  # 룰 업데이트 명령
│   └── port/                  # 포트 인터페이스
│       ├── RuleRepository.java     # 룰 리포지토리 인터페이스
│       └── EventRepository.java    # 이벤트 리포지토리 인터페이스
│
├── infrastructure/            # 인프라 레이어 (외부 시스템 연동)
│   ├── streaming/             # 스트림 처리
│   │   └── FlinkStreamProcessor.java   # Flink 스트림 프로세서
│   └── persistence/           # 영속성 (구현 예정)
│
└── interfaces/                # 인터페이스 레이어 (API)
    └── web/                   # 웹 인터페이스
        └── RuleController.java # 룰 관리 컨트롤러
```

## DDD 레이어별 설명

### Domain Layer (도메인 레이어)
비즈니스 핵심 로직과 규칙이 포함된 레이어입니다.

- **Entities**: `Event`, `Rule` - 핵심 도메인 엔티티
- **Value Objects**: `RuleCondition`, `TimeWindow` - 불변 값 객체
- **Domain Services**: `RuleValidationService`, `EventProcessingService` - 도메인 서비스

### Application Layer (응용 레이어)
유스케이스를 구현하고 도메인 서비스들을 조합하는 레이어입니다.

- **Application Services**: 비즈니스 유스케이스 구현
- **DTOs**: 외부와의 데이터 교환 객체
- **Commands**: CQRS 패턴의 명령 객체
- **Port Interfaces**: 외부 의존성 추상화

### Infrastructure Layer (인프라 레이어)
외부 시스템과의 연동을 담당하는 레이어입니다.

- **Stream Processing**: Flink 스트림 처리 구현
- **Persistence**: 데이터 저장소 구현
- **Message Queues**: Kafka 등 메시지 큐 연동

### Interface Layer (인터페이스 레이어)
외부 세계와의 접점을 담당하는 레이어입니다.

- **Web Controllers**: REST API 컨트롤러
- **Configuration**: 설정 파일들

##  주요 기능

### 실시간 이벤트 처리
- **이벤트 정규화**: 다양한 소스의 이벤트를 표준화
- **이상 탐지**: 실시간 이상 패턴 탐지
- **통계 수집**: 이벤트 통계 실시간 수집

### 동적 룰 관리
- **룰 생성/수정/삭제**: CRUD 연산 지원
- **룰 타입**: 단일 이벤트, 시퀀스, 빈도, 임계값, 이상 탐지
- **동적 업데이트**: 런타임에 룰 변경 가능

### 고급 패턴 매칭
- **복합 조건**: AND/OR 논리 연산자 지원
- **시간 윈도우**: 다양한 시간 단위 지원
- **시퀀스 패턴**: 이벤트 순서 패턴 매칭

## 📦 라이브러리 사용 방법

### 1. 의존성 추가
```xml
<dependency>
    <groupId>com.flinkapi</groupId>
    <artifactId>flink-cep-library</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. 기본 사용 예제
```java
// 도메인 서비스 초기화
RuleValidationService ruleValidationService = new RuleValidationService();
EventProcessingService eventProcessingService = new EventProcessingService();

// 리포지토리 구현체 (사용자가 구현)
RuleRepository ruleRepository = new YourRuleRepositoryImpl();
EventRepository eventRepository = new YourEventRepositoryImpl();

// 응용 서비스 초기화
RuleManagementService ruleManagementService = 
    new RuleManagementService(ruleRepository, ruleValidationService);

// 룰 생성
RuleCreateCommand createCommand = new RuleCreateCommand();
createCommand.setRuleId("rule-001");
createCommand.setRuleName("고액 거래 탐지");
createCommand.setRuleType(Rule.RuleType.SINGLE_EVENT);
createCommand.setSeverity(Rule.Severity.HIGH);

// 조건 설정
List<RuleCondition> conditions = Arrays.asList(
    new RuleCondition("amount", RuleCondition.Operator.GREATER_THAN, 50000.0)
);
createCommand.setConditions(conditions);

// 룰 생성 실행
RuleDto createdRule = ruleManagementService.createRule(createCommand);
```

### 3. 이벤트 처리 예제
```java
// 이벤트 생성
Event event = new Event("event-001", "TRANSACTION")
    .withUserId("user-001")
    .withAmount(75000.0)
    .withRegion("KR")
    .withDeviceType("mobile");

// 이벤트 처리
EventStreamingService eventStreamingService = 
    new EventStreamingService(eventRepository, ruleRepository, 
                             eventProcessingService, flinkStreamProcessor);

EventStreamingService.ProcessingResult result = 
    eventStreamingService.processEvent(event);

if (result.isSuccess() && !result.getAlerts().isEmpty()) {
    System.out.println("🚨 알림 발생: " + result.getAlerts().size() + "개");
}
```

## 🔧 확장 포인트

### 1. Repository 구현
```java
public class YourRuleRepositoryImpl implements RuleRepository {
    @Override
    public Rule save(Rule rule) {
        // 데이터베이스에 룰 저장 로직
        return rule;
    }
    
    @Override
    public Optional<Rule> findById(String ruleId) {
        // 데이터베이스에서 룰 조회 로직
        return Optional.empty();
    }
    
    // 기타 메서드들 구현...
}
```

### 2. 커스텀 이벤트 처리
```java
public class CustomEventProcessingService extends EventProcessingService {
    @Override
    public AnomalyDetectionResult detectAnomalies(Event event) {
        // 커스텀 이상 탐지 로직
        AnomalyDetectionResult baseResult = super.detectAnomalies(event);
        
        // 추가 이상 탐지 로직
        List<String> customAnomalies = detectCustomAnomalies(event);
        
        return new AnomalyDetectionResult(
            baseResult.hasAnomalies() || !customAnomalies.isEmpty(),
            mergeAnomalies(baseResult.getAnomalies(), customAnomalies),
            calculateScore(baseResult, customAnomalies)
        );
    }
}
```

## 🎯 DDD 원칙 준수

### 1. 도메인 주도 설계
- **유비쿼터스 언어**: 도메인 전문가와 개발자가 공통으로 사용하는 언어
- **경계 컨텍스트**: 명확한 도메인 경계 정의
- **애그리게이트**: 일관성 있는 도메인 객체 그룹

### 2. 레이어 분리
- **의존성 역전**: 고수준 모듈이 저수준 모듈에 의존하지 않음
- **관심사 분리**: 각 레이어는 명확한 책임을 가짐
- **테스트 용이성**: 각 레이어를 독립적으로 테스트 가능

### 3. 도메인 모델 무결성
- **불변성**: Value Object는 불변 객체로 설계
- **캡슐화**: 도메인 로직을 엔티티 내부에 캡슐화
- **검증**: 도메인 규칙을 통한 데이터 무결성 보장

## 📈 성능 최적화

### 1. 스트림 처리 최적화
- **병렬 처리**: 이벤트 스트림 병렬 처리
- **배치 처리**: 대량 이벤트 일괄 처리
- **메모리 관리**: 효율적인 메모리 사용

### 2. 룰 엔진 최적화
- **룰 인덱싱**: 빠른 룰 매칭을 위한 인덱스
- **조건 최적화**: 조건 평가 순서 최적화
- **캐싱**: 자주 사용되는 룰 캐싱

## 🛠️ 개발 환경

### 요구사항
- Java 8+
- Apache Flink 1.14+
- Maven 3.6+

### 빌드
```bash
mvn clean compile
```

### 테스트
```bash
mvn test
```

### 패키징
```bash
mvn package
```

## 🤝 기여하기

1. 프로젝트 포크
2. 기능 브랜치 생성 (`git checkout -b feature/amazing-feature`)
3. 변경사항 커밋 (`git commit -m 'Add amazing feature'`)
4. 브랜치 푸시 (`git push origin feature/amazing-feature`)
5. Pull Request 생성

## 📄 라이선스


## 📞 지원

- **이슈 신고**: [GitHub Issues](https://github.com/ukulkdin/FlinkAPI/issues)
- **문서**: [프로젝트 위키](https://github.com/your-repo/FlinkAPI/wiki)
- **예제**: [examples 디렉토리](./examples)

---

**FlinkAPI** - 🚀 **DDD로 설계된 차세대 실시간 CEP 라이브러리** 