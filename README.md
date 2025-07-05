# FlinkAPI CEP Library

![Apache Flink](https://img.shields.io/badge/Apache%20Flink-1.14.6-blue.svg)
![Java](https://img.shields.io/badge/Java-8-orange.svg)
![Maven](https://img.shields.io/badge/Maven-3.8+-green.svg)
![License](https://img.shields.io/badge/License-Apache%202.0-brightgreen.svg)

> Apache Flink 기반 실시간 Complex Event Processing (CEP) 라이브러리  
> DDD(Domain-Driven Design) 아키텍처로 설계된 멀티모듈 프로젝트

---

## 📋 목차

- [프로젝트 개요](#-프로젝트-개요)
- [아키텍처](#-아키텍처)
- [모듈 구성](#-모듈-구성)
- [시작하기](#-시작하기)
- [빌드 및 실행](#-빌드-및-실행)
- [사용 예제](#-사용-예제)
- [프로젝트 구조](#-프로젝트-구조)
- [개발 상태](#-개발-상태)
- [기여하기](#-기여하기)

---

## 🎯 프로젝트 개요

**FlinkAPI CEP Library**는 Apache Flink를 기반으로 한 실시간 복합 이벤트 처리(CEP) 라이브러리입니다. 
DDD 아키텍처를 적용하여 확장 가능하고 유지보수가 용이한 구조로 설계되었습니다.

### 주요 특징

- **실시간 이벤트 처리**: Apache Flink CEP를 활용한 고성능 스트림 처리
- **DDD 아키텍처**: 도메인 중심 설계로 비즈니스 로직과 기술적 구현 분리
- **멀티모듈 구조**: 각 레이어별 독립적인 모듈 관리
- **타입 안전성**: Java 8+ 기반의 강타입 시스템
- **확장 가능성**: 플러그인 아키텍처 지원

### 활용 사례

- 금융 사기 탐지 시스템
- IoT 센서 데이터 모니터링
- 실시간 로그 분석
- 비즈니스 프로세스 모니터링
- 네트워크 보안 이벤트 감지

---

## 🏗 아키텍처

FlinkAPI CEP Library는 DDD(Domain-Driven Design) 원칙을 따라 다음과 같은 레이어로 구성됩니다:

```
┌─────────────────────────────────────────┐
│              Starter                    │  ← 애플리케이션 엔트리포인트
├─────────────────────────────────────────┤
│             Interfaces                  │  ← REST API, 웹 컨트롤러 (개발 중)
├─────────────────────────────────────────┤
│            Application                  │  ← 애플리케이션 서비스, DTO
├─────────────────────────────────────────┤
│           Infrastructure                │  ← Flink 엔진, 외부 시스템 연동
├─────────────────────────────────────────┤
│              Domain                     │  ← 도메인 모델, 비즈니스 룰
└─────────────────────────────────────────┘
```

---

## 📦 모듈 구성

| 모듈 | 설명 | 상태 |
|------|------|------|
| **flink-cep-domain** | 도메인 모델, 비즈니스 룰, 값 객체 | ✅ 완료 |
| **flink-cep-application** | 애플리케이션 서비스, 포트, DTO | ✅ 완료 |
| **flink-cep-infrastructure** | Flink 엔진, CEP 패턴 빌더, 룰 파서 | ✅ 완료 |
| **flink-cep-interfaces** | REST API, 웹 컨트롤러 | 🚧 개발 중 |
| **flink-cep-starter** | 올인원 스타터, 메인 애플리케이션 | ✅ 완료 |

### 모듈별 주요 컴포넌트

#### Domain Layer
- `Rule`: 룰 도메인 모델
- `Event`: 이벤트 도메인 모델  
- `RuleCondition`: 룰 조건 값 객체
- `TimeWindow`: 시간 윈도우 값 객체

#### Application Layer
- `RuleManagementService`: 룰 관리 서비스
- `EventStreamingService`: 이벤트 스트리밍 서비스
- `RuleRepository`: 룰 저장소 포트
- `EventRepository`: 이벤트 저장소 포트

#### Infrastructure Layer
- `RuleEngine`: Flink CEP 룰 엔진
- `CEPPatternBuilder`: CEP 패턴 빌더
- `FlinkStreamProcessor`: Flink 스트림 프로세서
- `RuleParserService`: 룰 파싱 서비스

---

## 🚀 시작하기

### 필수 요구사항

- **Java**: 8 이상
- **Maven**: 3.8 이상
- **Apache Flink**: 1.14.6 (자동 포함)

### 설치

1. **저장소 클론**
   ```bash
   git clone https://github.com/ukukdin/FlinkAPI.git
   cd FlinkAPI
   ```

2. **의존성 설치 및 빌드**
   ```bash
   mvn clean install
   ```

3. **애플리케이션 실행**
   ```bash
   java -jar flink-cep-starter/target/flink-cep-starter-1.0.0.jar
   ```

---

## 🔧 빌드 및 실행

### 전체 프로젝트 빌드

```bash
# 컴파일
mvn clean compile

# 테스트 포함 빌드
mvn clean package

# 테스트 제외 빌드 (권장)
mvn clean package -DskipTests
```

### 개별 모듈 빌드

```bash
# 특정 모듈만 빌드
mvn clean package -pl flink-cep-starter -am

# 특정 모듈부터 빌드
mvn clean package -rf :flink-cep-infrastructure
```

### Fat JAR 실행

```bash
# 메인 애플리케이션 실행
java -jar flink-cep-starter/target/flink-cep-starter-1.0.0.jar

# 커스텀 설정으로 실행
java -Dflink.env=production -jar flink-cep-starter/target/flink-cep-starter-1.0.0.jar
```

---

## 💡 사용 예제

### 기본 사용법

```java
import com.flinkapi.cep.FlinkCEPApplication;
import com.flinkapi.cep.domain.model.Rule;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class Example {
    public static void main(String[] args) throws Exception {
        // 1. 애플리케이션 생성
        FlinkCEPApplication app = FlinkCEPApplication.create();
        
        // 2. 룰 등록 (향후 구현 예정)
        // Rule fraudRule = Rule.builder()
        //     .ruleName("고액 거래 탐지")
        //     .ruleType(Rule.RuleType.THRESHOLD)
        //     .build();
        // app.registerRule(fraudRule);
        
        // 3. 이벤트 스트리밍 시작
        app.startEventStreaming("transaction-events");
        
        // 4. 애플리케이션 실행
        app.execute("FlinkCEP Example");
    }
}
```

### 커스텀 환경 설정

```java
// 커스텀 Flink 환경으로 애플리케이션 생성
StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
env.setParallelism(4);

FlinkCEPApplication app = FlinkCEPApplication.create(env);
```

---

## 📁 프로젝트 구조

```
FlinkAPI/
├── pom.xml                           # 부모 POM
├── README.md                         # 프로젝트 문서
├── ModuleTest.java                   # 모듈 통합 테스트
│
├── flink-cep-domain/                 # 도메인 레이어
│   ├── src/main/java/
│   │   └── com/flinkapi/cep/
│   │       ├── domain/model/         # 도메인 모델
│   │       ├── domain/service/       # 도메인 서비스
│   │       └── domain/value/         # 값 객체
│   └── pom.xml
│
├── flink-cep-application/            # 애플리케이션 레이어
│   ├── src/main/java/
│   │   └── com/flinkapi/cep/
│   │       ├── application/dto/      # 데이터 전송 객체
│   │       ├── application/port/     # 포트 (인터페이스)
│   │       └── application/service/  # 애플리케이션 서비스
│   └── pom.xml
│
├── flink-cep-infrastructure/         # 인프라스트럭처 레이어
│   ├── src/main/java/
│   │   └── com/flinkapi/cep/
│   │       ├── engine/               # CEP 엔진
│   │       └── infrastructure/       # 인프라 구현체
│   └── pom.xml
│
├── flink-cep-interfaces/             # 인터페이스 레이어 (개발 중)
│   ├── src/main/java/
│   │   └── com/flinkapi/cep/
│   │       └── web/                  # REST 컨트롤러
│   └── pom.xml
│
└── flink-cep-starter/                # 스타터 애플리케이션
    ├── src/main/java/
    │   └── com/flinkapi/cep/
    │       ├── FlinkCEPApplication.java
    │       ├── FlinkCEPLibrary.java
    │       ├── app/                  # 샘플 애플리케이션
    │       └── examples/             # 사용 예제
    ├── src/test/java/               # 통합 테스트
    └── pom.xml
```

---

## 🚧 개발 상태

### 완료된 기능

- ✅ DDD 아키텍처 기반 멀티모듈 구조
- ✅ 도메인 모델 정의 (Rule, Event, Condition 등)
- ✅ Flink CEP 패턴 빌더
- ✅ 룰 엔진 기본 구현
- ✅ 이벤트 스트리밍 서비스 틀
- ✅ Maven 빌드 시스템
- ✅ Fat JAR 패키징
- ✅ 코드 정리 (이모지 제거 완료)

### 개발 중인 기능

- 🚧 REST API 인터페이스 (interfaces 모듈)
- 🚧 실시간 룰 등록/수정 기능
- 🚧 Kafka 연동
- 🚧 데이터베이스 퍼시스턴스
- 🚧 웹 대시보드

### 향후 계획

- 📋 Spring Boot 통합
- 📋 Docker 컨테이너 지원
- 📋 Kubernetes 배포 가이드
- 📋 성능 최적화
- 📋 모니터링 및 메트릭스
- 📋 자동화된 테스트 케이스 확장

---

## 🤝 기여하기

### 개발 환경 설정

1. **포크 및 클론**
   ```bash
   git fork https://github.com/ukukdin/FlinkAPI.git
   git clone https://github.com/YOUR_USERNAME/FlinkAPI.git
   ```

2. **개발 브랜치 생성**
   ```bash
   git checkout -b feature/your-feature-name
   ```

3. **변경사항 커밋**
   ```bash
   git commit -m "feat: add your feature description"
   ```

4. **풀 리포트 생성**
   ```bash
   git push origin feature/your-feature-name
   ```

### 코드 스타일

- Java 코딩 컨벤션 준수
- DDD 패턴 따르기
- 단위 테스트 작성
- 문서화 유지

### 이슈 리포팅

버그 발견이나 기능 요청 시 [GitHub Issues](https://github.com/ukukdin/FlinkAPI/issues)를 이용해 주세요.

---

## 📄 라이선스

이 프로젝트는 Apache License 2.0 하에 배포됩니다. 자세한 내용은 [LICENSE](LICENSE) 파일을 참조하세요.

---

## 👥 개발자 정보

- **개발팀**: IDC4 Team
- **이메일**: eum714211@gmail.com
- **GitHub**: [@ukukdin](https://github.com/ukukdin)

---

## 🔗 관련 링크

- [Apache Flink 공식 문서](https://flink.apache.org/)
- [Flink CEP 가이드](https://nightlies.apache.org/flink/flink-docs-release-1.14/docs/libs/cep/)
- [DDD 아키텍처 가이드](https://martinfowler.com/tags/domain%20driven%20design.html)

---

⭐ **이 프로젝트가 유용하다면 Star를 눌러주세요!**



