
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

Flink SQL로 변환

WHERE 조건을 기반으로 CEP 패턴 생성

🚀 출력 예시 (CEP Java 코드):

java
복사
편집
Pattern<Event, ?> pattern = Pattern.<Event>begin("start")
    .where(new SimpleCondition<Event>() {
        @Override
        public boolean filter(Event event) {
            return event.amount > 10000 && "KR".equals(event.region);
        }
    });
또는 출력 예시 (JSON DSL):

{
  "pattern": "start",
  "conditions": [
    { "field": "amount", "op": ">", "value": 10000 },
    { "field": "region", "op": "==", "value": "KR" }
  ]
}

