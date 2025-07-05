package com.flinkapi.cep.domain.service;

import com.flinkapi.cep.domain.model.Event;
import com.flinkapi.cep.domain.model.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 이벤트 처리 도메인 서비스 - 이벤트 처리 로직을 담당하는 핵심 도메인 서비스
 *
 */
public class EventProcessingService {
    
    private static final Logger logger = LoggerFactory.getLogger(EventProcessingService.class);
    
    // 이벤트 통계 저장소
    private final Map<String, EventStats> eventStats = new ConcurrentHashMap<>();

    /**
     * 이벤트 전처리
     */
    public ProcessingResult preprocessEvent(Event event) {
        logger.debug("🚀 Preprocessing event: {}", event.getEventId());
        
        List<String> issues = new ArrayList<>();
        
        // 이벤트 유효성 검증
        if (!event.isValidEvent()) {
            issues.add("이벤트가 유효하지 않습니다.");
            return new ProcessingResult(false, event, issues);
        }
        
        // 이벤트 정규화
        Event normalizedEvent = normalizeEvent(event);
        
        // 이벤트 통계 업데이트
        updateEventStats(normalizedEvent);
        
        logger.debug("Event preprocessing completed: {}", normalizedEvent.getEventId());
        return new ProcessingResult(true, normalizedEvent, issues);
    }

    /**
     * 🔧 이벤트 정규화
     */
    private Event normalizeEvent(Event event) {
        Event normalized = new Event(event.getEventId(), event.getEventType());
        
        // 기본 필드 복사
        normalized.setTimestamp(event.getTimestamp());
        normalized.setUserId(event.getUserId());
        normalized.setSessionId(event.getSessionId());
        normalized.setAmount(event.getAmount());
        normalized.setDeviceType(event.getDeviceType());
        normalized.setIpAddress(event.getIpAddress());
        normalized.setProperties(event.getProperties());
        
        // 지역 정규화
        String normalizedRegion = normalizeRegion(event.getRegion());
        normalized.setRegion(normalizedRegion);
        
        return normalized;
    }

    /**
     * 🌍 지역 정규화
     */
    private String normalizeRegion(String region) {
        if (region == null) return "UNKNOWN";
        
        String upperRegion = region.toUpperCase();
        
        // 국가 코드 정규화
        switch (upperRegion) {
            case "KOREA":
            case "SOUTH KOREA":
                return "KR";
            case "UNITED STATES":
            case "USA":
                return "US";
            case "CHINA":
                return "CN";
            case "JAPAN":
                return "JP";
            case "UNITED KINGDOM":
            case "UK":
                return "GB";
            case "RUSSIA":
                return "RU";
            default:
                return upperRegion.length() > 2 ? upperRegion.substring(0, 2) : upperRegion;
        }
    }

    /**
     * 이벤트 통계 업데이트
     */
    private void updateEventStats(Event event) {
        String key = event.getEventType() + "_" + event.getRegion();
        
        eventStats.compute(key, (k, stats) -> {
            if (stats == null) {
                stats = new EventStats(event.getEventType(), event.getRegion());
            }
            stats.incrementCount();
            stats.updateLastSeen(event.getTimestamp());
            
            if (event.getAmount() != null) {
                stats.updateAmount(event.getAmount());
            }
            
            return stats;
        });
    }

    /**
     *  이벤트와 룰 매칭
     */
    public boolean doesEventMatchRule(Event event, Rule rule) {
        if (event == null || rule == null) {
            return false;
        }
        
        if (!rule.isEnabled()) {
            return false;
        }
        
        return rule.canApplyToEvent(event);
    }

    /**
     *  이벤트 이상 탐지
     */
    public AnomalyDetectionResult detectAnomalies(Event event) {
        logger.debug("🔍 Detecting anomalies for event: {}", event.getEventId());
        
        List<String> anomalies = new ArrayList<>();
        
        // 고액 거래 이상 탐지
        if (event.isHighValueTransaction()) {
            anomalies.add("고액 거래 탐지");
        }
        
        // 해외 거래 이상 탐지
        if (event.isSuspiciousTransaction()) {
            anomalies.add("의심스러운 해외 거래 탐지");
        }
        
        // 시간대 이상 탐지
        if (isOffHoursTransaction(event)) {
            anomalies.add("비정상 시간대 거래 탐지");
        }
        
        // 디바이스 이상 탐지
        if (isSuspiciousDevice(event)) {
            anomalies.add("의심스러운 디바이스 탐지");
        }
        
        AnomalyDetectionResult result = new AnomalyDetectionResult(
            !anomalies.isEmpty(), 
            anomalies, 
            calculateAnomalyScore(anomalies)
        );
        
        logger.debug("🔍 Anomaly detection result: {}", result);
        return result;
    }

    /**
     *  비정상 시간대 체크
     */
    private boolean isOffHoursTransaction(Event event) {
        if (event.getAmount() == null || event.getAmount() <= 10000) {
            return false; // 소액 거래는 제외
        }
        
        Instant timestamp = Instant.ofEpochMilli(event.getTimestamp());
        int hour = timestamp.atZone(java.time.ZoneId.systemDefault()).getHour();
        
        // 새벽 2시~6시 사이의 고액 거래
        return hour >= 2 && hour <= 6;
    }

    /**
     * 📱 의심스러운 디바이스 체크
     */
    private boolean isSuspiciousDevice(Event event) {
        if (event.getDeviceType() == null) {
            return false;
        }
        
        // 모바일에서 고액 거래
        return event.isMobileDevice() && event.isHighValueTransaction();
    }

    /**
     *  이상 점수 계산
     */
    private double calculateAnomalyScore(List<String> anomalies) {
        if (anomalies.isEmpty()) {
            return 0.0;
        }
        
        double score = 0.0;
        for (String anomaly : anomalies) {
            switch (anomaly) {
                case "고액 거래 탐지":
                    score += 0.3;
                    break;
                case "의심스러운 해외 거래 탐지":
                    score += 0.4;
                    break;
                case "비정상 시간대 거래 탐지":
                    score += 0.2;
                    break;
                case "의심스러운 디바이스 탐지":
                    score += 0.1;
                    break;
                default:
                    score += 0.1;
                    break;
            }
        }
        
        return Math.min(score, 1.0); // 최대 1.0
    }

    /**
     *  이벤트 통계 조회
     */
    public Map<String, EventStats> getEventStats() {
        return new ConcurrentHashMap<>(eventStats);
    }

    /**
     *  처리 결과 클래스
     */
    public static class ProcessingResult {
        private final boolean success;
        private final Event processedEvent;
        private final List<String> issues;

        public ProcessingResult(boolean success, Event processedEvent, List<String> issues) {
            this.success = success;
            this.processedEvent = processedEvent;
            this.issues = issues != null ? issues : new ArrayList<>();
        }

        public boolean isSuccess() { return success; }
        public Event getProcessedEvent() { return processedEvent; }
        public List<String> getIssues() { return new ArrayList<>(issues); }

        @Override
        public String toString() {
            return String.format("ProcessingResult{success=%s, issues=%s}", success, issues);
        }
    }

    /**
     *  이상 탐지 결과 클래스
     */
    public static class AnomalyDetectionResult {
        private final boolean hasAnomalies;
        private final List<String> anomalies;
        private final double anomalyScore;

        public AnomalyDetectionResult(boolean hasAnomalies, List<String> anomalies, double anomalyScore) {
            this.hasAnomalies = hasAnomalies;
            this.anomalies = anomalies != null ? anomalies : new ArrayList<>();
            this.anomalyScore = anomalyScore;
        }

        public boolean hasAnomalies() { return hasAnomalies; }
        public List<String> getAnomalies() { return new ArrayList<>(anomalies); }
        public double getAnomalyScore() { return anomalyScore; }

        @Override
        public String toString() {
            return String.format("AnomalyDetectionResult{hasAnomalies=%s, anomalies=%s, score=%.2f}", 
                hasAnomalies, anomalies, anomalyScore);
        }
    }

    /**
     *  이벤트 통계 클래스
     */
    public static class EventStats {
        private final String eventType;
        private final String region;
        private long count;
        private long lastSeen;
        private double totalAmount;
        private double minAmount = Double.MAX_VALUE;
        private double maxAmount = Double.MIN_VALUE;

        public EventStats(String eventType, String region) {
            this.eventType = eventType;
            this.region = region;
        }

        public void incrementCount() {
            this.count++;
        }

        public void updateLastSeen(long timestamp) {
            this.lastSeen = Math.max(this.lastSeen, timestamp);
        }

        public void updateAmount(double amount) {
            this.totalAmount += amount;
            this.minAmount = Math.min(this.minAmount, amount);
            this.maxAmount = Math.max(this.maxAmount, amount);
        }

        // Getters
        public String getEventType() { return eventType; }
        public String getRegion() { return region; }
        public long getCount() { return count; }
        public long getLastSeen() { return lastSeen; }
        public double getTotalAmount() { return totalAmount; }
        public double getMinAmount() { return minAmount; }
        public double getMaxAmount() { return maxAmount; }
        public double getAverageAmount() { return count > 0 ? totalAmount / count : 0; }

        @Override
        public String toString() {
            return String.format("EventStats{eventType='%s', region='%s', count=%d, totalAmount=%.2f}",
                eventType, region, count, totalAmount);
        }
    }
} 