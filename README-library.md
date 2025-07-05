#  FlinkAPI CEP 라이브러리

Apache Flink 기반 복합 이벤트 처리(CEP) 라이브러리입니다. DDD(Domain-Driven Design) 아키텍처를 준수하며, 모듈화된 구조로 필요한 부분만 선택적으로 사용할 수 있습니다.

## 특징

- **자연어 룰 파싱**: "한국에서 거래 이후 중국에서 로그인" 같은 자연어를 CEP 패턴으로 변환
- **시퀀스 패턴 지원**: 다단계 이벤트 시퀀스 탐지
- **실시간 처리**: Apache Flink CEP 기반 실시간 스트림 처리
- **유연한 조건**: 금액, 지역, 디바이스, 시간 등 다양한 조건 지원
- **간단한 API**: Builder 패턴으로 쉬운 설정

##  Jitpack을 통한 라이브러리 사용

### Maven 설정

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <!--  도메인 모듈만 사용 -->
    <dependency>
        <groupId>com.github.ukukdin.FlinkAPI</groupId>
        <artifactId>flink-cep-domain</artifactId>
        <version>v1.0.0</version>
    </dependency>
    
    <!-- 응용 서비스도 사용 -->
    <dependency>
        <groupId>com.github.ukukdin.FlinkAPI</groupId>
        <artifactId>flink-cep-application</artifactId>
        <version>v1.0.0</version>
    </dependency>
    
    <!--  인프라 구현체도 사용 -->
    <dependency>
        <groupId>com.github.ukukdin.FlinkAPI</groupId>
        <artifactId>flink-cep-infrastructure</artifactId>
        <version>v1.0.0</version>
    </dependency>
</dependencies>
```

### Gradle 설정

```gradle
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    // 🎯 도메인 모듈만 사용
    implementation 'com.github.ukukdin.FlinkAPI:flink-cep-domain:v1.0.0'
    
    // 📋 응용 서비스도 사용
    implementation 'com.github.ukukdin.FlinkAPI:flink-cep-application:v1.0.0'
    
    // 🔧 인프라 구현체도 사용
    implementation 'com.github.ukukdin.FlinkAPI:flink-cep-infrastructure:v1.0.0'
}
```

## 🎯 사용 시나리오별 모듈 선택

### 1. 도메인 모델만 사용하는 경우

```java
// 🎯 도메인 모듈만 import
import com.flinkapi.cep.domain.model.Event;
import com.flinkapi.cep.domain.model.Rule;
import com.flinkapi.cep.domain.value.RuleCondition;

// 비즈니스 로직에서 도메인 모델 사용
Event event = new Event("evt-001", "TRANSACTION");
Rule rule = new Rule("rule-001", "Fraud Detection", Rule.RuleType.FRAUD_DETECTION);

// 도메인 로직 실행
if (event.isSuspiciousTransaction()) {
    // 의심거래 처리
}
```

### 2. 응용 서비스까지 사용하는 경우

```java
//  응용 레이어까지 import
import com.flinkapi.cep.application.service.RuleManagementService;
import com.flinkapi.cep.application.service.EventStreamingService;
import com.flinkapi.cep.application.dto.RuleCreateCommand;

// 응용 서비스 사용
RuleManagementService ruleService = new RuleManagementService(ruleRepository);
EventStreamingService eventService = new EventStreamingService(eventRepository, streamProcessor);

// 룰 생성 및 관리
RuleCreateCommand command = new RuleCreateCommand("rule-001", "Fraud Detection", ...);
ruleService.createRule(command);
```

### 3. 전체 스택 사용하는 경우

```java
//  인프라 구현체까지 모두 import
import com.flinkapi.cep.engine.RuleEngine;
import com.flinkapi.cep.infrastructure.streaming.FlinkStreamProcessor;

// 전체 스택 구성
RuleEngine ruleEngine = new RuleEngine();
FlinkStreamProcessor processor = new FlinkStreamProcessor();

// Flink 스트림 처리 실행
ruleEngine.registerRule(rule);
processor.startProcessing(rules);
```

## 주요 기능

### 이벤트 처리
- 실시간 이벤트 스트림 처리
- 복합 이벤트 패턴 매칭
- 이상 탐지 및 경고 생성

### 룰 관리
- 동적 룰 생성 및 업데이트
- 룰 검증 및 테스트
- 룰 성능 모니터링

### DDD 아키텍처
- 도메인 중심 설계
- 계층간 의존성 역전
- 모듈 독립성 보장

## 추가 문서

- [DDD 아키텍처 가이드](README-DDD.md)
- [모듈 구조 설명](README-modules.md)

## 기여하기

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## 라이선스

이 프로젝트는 MIT 라이선스 하에 있습니다.

##  링크

- **GitHub**: https://github.com/ukukdin/FlinkAPI
- **Jitpack**: https://jitpack.io/#ukukdin/FlinkAPI
- **릴리즈**: https://github.com/ukukdin/FlinkAPI/releases/tag/v1.0.0

---

### 빠른 시작 가이드

```bash
# 1. 프로젝트 생성
mkdir my-flink-cep-project
cd my-flink-cep-project

# 2. Maven 프로젝트 초기화
mvn archetype:generate -DgroupId=com.example -DartifactId=my-cep-app

# 3. pom.xml에 FlinkAPI 의존성 추가
# (위의 Maven 설정 참고)

# 4. 첫 번째 CEP 애플리케이션 작성
# (위의 사용 예제 참고)

# 5. 실행
mvn clean compile exec:java
```

 **축하합니다!** 이제 FlinkAPI CEP 라이브러리를 사용할 준비가 되었습니다! 