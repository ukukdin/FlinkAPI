
![image](https://github.com/user-attachments/assets/fe42a080-d35c-4c0e-a3dc-9b6450429f71)

# 개요

> ✨ 이 라이브러리는 MySQL SQL을 Apache Flink에서 실행 가능한 코드(Flink SQL, Table API, CEP Pattern)로 변환해주는 Java 기반 도구입니다.  
> 실시간 데이터 처리가 중요한 금융, 물류, 이상거래 탐지 시스템 등에 유용합니다.

---

## 📘 개요

**MySql2Flink**는 기존 MySQL SQL 문법을 분석하여 Apache Flink에서 바로 실행 가능한 형식으로 변환해주는 도구입니다.  
특히 **실시간 스트리밍 분석이 필요한 시스템**에서 유용하며, 다음과 같은 환경을 지원합니다:

- Flink SQL 문법
- Flink Table API(Java)
- Flink CEP 패턴(Java 또는 JSON)

---

## 🎯 주요 목표

- MySQL SQL을 Flink SQL 및 Table API로 변환
- WHERE 조건을 기반으로 Flink CEP 패턴 자동 생성
- SQL → Flink SQL → CEP Pattern → 실시간 룰 등록 자동화

---

## 🧩 핵심 모듈 구성

| 모듈 | 설명 |
|------|------|
| `MySQLParser` | MySQL SQL을 내부 객체(`SqlQuery`)로 파싱 |
| `FlinkSQLConverter` | Flink SQL 문법으로 변환 |
| `TableAPIConverter` | Java Table API 코드로 변환 |
| `CEPPatternConverter` | WHERE 조건을 기반으로 CEP 패턴 생성 |
| `FlinkTableDDLGenerator` | Kafka/JDBC 테이블 생성 SQL 자동 생성 |
| `RuleExporter` | CEP 룰을 Java 또는 JSON 형식으로 출력 |

---

## 📌 활용 시나리오

1. ✅ **사용자 입력** (MySQL SQL):
   ```sql
   SELECT user_id FROM transactions
   WHERE amount > 10000 AND region = 'KR';



🔄 변환 처리:

**Flink SQL로 변환**

WHERE 조건을 기반으로 CEP 패턴 생성

🚀 출력 예시 (CEP Java 코드):

### 🔸 Flink CEP Java 코드 예시
```java
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;

Pattern<Event, ?> pattern = Pattern.<Event>begin("start")
    .where(new SimpleCondition<Event>() {
        @Override
        public boolean filter(Event event) {
            return event.getAmount() > 10000 && "KR".equals(event.getRegion());
        }
    });
```

### 🔸 JSON 기반 룰 정의

```json
{
  "pattern": "start",
  "conditions": [
    {
      "field": "amount",
      "op": ">",
      "value": 10000
    },
    {
      "field": "region",
      "op": "==",
      "value": "KR"
    }
  ]
}
```


graph TB
    subgraph "📚 FlinkCEP Library API"
        FlinkCEPLibrary["🚀 FlinkCEPLibrary<br/>+ addRule(String, String)<br/>+ addRule(Rule)<br/>+ processEvents(DataStream)<br/>+ getAllRules()<br/>+ removeRule(String)<br/>+ clearAllRules()"]
        
        Builder["🏗️ Builder<br/>+ withExecutionEnvironment()<br/>+ withAlertHandler()<br/>+ build()"]
        
        FlinkCEPLibrary --> Builder
    end

    subgraph "📊 Core Domain Models"
        Event["📊 Event<br/>- eventId, eventType<br/>- userId, timestamp<br/>- amount, region<br/>- deviceType, properties<br/><br/>+ withUserId()<br/>+ withAmount()<br/>+ withRegion()<br/>+ isHighValueTransaction()<br/>+ isFromKorea()"]
        
        Rule["📋 Rule<br/>- ruleId, ruleName<br/>- ruleType, conditions<br/>- timeWindow, severity<br/>- sequenceSteps<br/><br/>+ withConditions()<br/>+ withTimeWindow()<br/>+ withSeverity()"]
        
        subgraph "Rule Components"
            RuleType["🎯 RuleType<br/>SINGLE_EVENT<br/>SEQUENCE<br/>THRESHOLD<br/>FREQUENCY<br/>ANOMALY"]
            Condition["🔍 Condition<br/>- field, operator<br/>- value, logicalOperator"]
            TimeWindow["⏰ TimeWindow<br/>- duration, unit<br/>+ toMilliseconds()"]
            SequenceStep["🔗 SequenceStep<br/>- stepName, eventType<br/>- conditions, optional"]
        end
        
        Rule --> RuleType
        Rule --> Condition
        Rule --> TimeWindow
        Rule --> SequenceStep
    end

    subgraph "🔧 Engine Layer"
        RuleEngine["🔥 RuleEngine<br/>+ registerRule(Rule)<br/>+ unregisterRule(String)<br/>+ applyRules(DataStream)<br/>+ getAllRules()<br/>+ getRuleMatchingStats()"]
        
        CEPPatternBuilder["🏗️ CEPPatternBuilder<br/>+ buildPattern(Rule)<br/>+ buildSingleEventPattern()<br/>+ buildSequencePattern()<br/>+ buildFrequencyPattern()"]
        
        RuleParserService["🧠 RuleParserService<br/>+ parseRule(String, String)<br/>+ parseSequenceRule()<br/>+ parseFrequencyRule()<br/>+ getExampleRules()"]
        
        RuleMatchResult["📋 RuleMatchResult<br/>- ruleId, ruleName<br/>- severity, triggerEvent<br/>- matchedEvents<br/>- matchTime, message"]
    end

    subgraph "⚡ Apache Flink Integration"
        FlinkEnv["🌊 StreamExecutionEnvironment"]
        FlinkStream["📡 DataStream&lt;Event&gt;"]
        FlinkPattern["🔍 Pattern&lt;Event&gt;"]
        FlinkSink["📤 FlinkCEPSink"]
        
        FlinkEnv --> FlinkStream
        FlinkPattern --> FlinkStream
        FlinkStream --> FlinkSink
    end

    %% Library API Dependencies
    FlinkCEPLibrary --> RuleEngine
    FlinkCEPLibrary --> RuleParserService
    FlinkCEPLibrary --> FlinkEnv
    FlinkCEPLibrary --> FlinkSink
    
    %% Engine Dependencies
    RuleEngine --> Rule
    RuleEngine --> Event
    RuleEngine --> CEPPatternBuilder
    RuleEngine --> RuleMatchResult
    
    CEPPatternBuilder --> Rule
    CEPPatternBuilder --> FlinkPattern
    
    RuleParserService --> Rule
    
    %% Flow
    FlinkCEPLibrary -.-> |"1. Parse"| RuleParserService
    FlinkCEPLibrary -.-> |"2. Register"| RuleEngine
    FlinkCEPLibrary -.-> |"3. Process"| FlinkStream
    RuleEngine -.-> |"4. Build Pattern"| CEPPatternBuilder
    CEPPatternBuilder -.-> |"5. Apply CEP"| FlinkPattern

    %% Usage Example
    subgraph "📝 Usage Example"
        Usage1["FlinkCEPLibrary cepLib = FlinkCEPLibrary.builder()<br/>.withExecutionEnvironment(env)<br/>.withAlertHandler(alert -> print(alert))<br/>.build();"]
        Usage2["cepLib.addRule(&quot;한국에서 로그인 이후 중국에서 이체&quot;, &quot;사기패턴&quot;);"]
        Usage3["cepLib.processEvents(eventStream);"]
        
        Usage1 --> Usage2
        Usage2 --> Usage3
    end

    classDef libraryClass fill:#e3f2fd,stroke:#1976d2,stroke-width:3px
    classDef domainClass fill:#f3e5f5,stroke:#7b1fa2
    classDef engineClass fill:#e8f5e8,stroke:#388e3c
    classDef flinkClass fill:#fff3e0,stroke:#f57c00
    classDef usageClass fill:#fce4ec,stroke:#c2185b

    class FlinkCEPLibrary,Builder libraryClass
    class Event,Rule,RuleType,Condition,TimeWindow,SequenceStep domainClass
    class RuleEngine,CEPPatternBuilder,RuleParserService,RuleMatchResult engineClass
    class FlinkEnv,FlinkStream,FlinkPattern,FlinkSink flinkClass
    class Usage1,Usage2,Usage3 usageClass
