package com.flinkapi.cep.application.service;

import com.flinkapi.cep.domain.model.Event;
import com.flinkapi.cep.domain.model.Rule;
import com.flinkapi.cep.domain.service.EventProcessingService;
import com.flinkapi.cep.application.port.EventRepository;
import com.flinkapi.cep.application.port.RuleRepository;
import com.flinkapi.cep.application.dto.EventDto;
import com.flinkapi.cep.application.dto.AlertDto;
import com.flinkapi.cep.application.port.StreamProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 이벤트 스트리밍 응용 서비스 - 실시간 이벤트 처리 유스케이스를 구현하는 Application Service
 * 
 */
public class EventStreamingService {
    
    private static final Logger logger = LoggerFactory.getLogger(EventStreamingService.class);
    
    private final EventRepository eventRepository;
    private final RuleRepository ruleRepository;
    private final EventProcessingService eventProcessingService;
    private final StreamProcessor streamProcessor;

    public EventStreamingService(EventRepository eventRepository, 
                                RuleRepository ruleRepository,
                                EventProcessingService eventProcessingService,
                                StreamProcessor streamProcessor) {
        this.eventRepository = eventRepository;
        this.ruleRepository = ruleRepository;
        this.eventProcessingService = eventProcessingService;
        this.streamProcessor = streamProcessor;
    }

    /**
     * 🚀 스트림 처리 시작
     */
    public void startStreamProcessing() {
        logger.info("🚀 Starting event stream processing...");
        
        // 활성화된 룰들 조회
        List<Rule> activeRules = ruleRepository.findByEnabled(true);
        logger.info("📋 Loaded {} active rules", activeRules.size());
        
        // 스트림 프로세서 시작
        streamProcessor.startProcessing(activeRules);
        
        logger.info("✅ Event stream processing started successfully");
    }

    /**
     * 🛑 스트림 처리 중지
     */
    public void stopStreamProcessing() {
        logger.info("🛑 Stopping event stream processing...");
        
        streamProcessor.stopProcessing();
        
        logger.info("✅ Event stream processing stopped successfully");
    }

    /**
     * 🔄 룰 동적 업데이트
     */
    public void updateRulesInStream(List<Rule> newRules) {
        logger.info("🔄 Updating rules in stream: {} rules", newRules.size());
        
        // 스트림에 새로운 룰 적용
        streamProcessor.updateRules(newRules);
        
        logger.info("✅ Rules updated in stream successfully");
    }

    /**
     * 📊 이벤트 처리
     */
    public ProcessingResult processEvent(Event event) {
        logger.debug("📊 Processing event: {}", event.getEventId());
        
        // 이벤트 전처리
        EventProcessingService.ProcessingResult preprocessResult = eventProcessingService.preprocessEvent(event);
        
        if (!preprocessResult.isSuccess()) {
            logger.warn("❌ Event preprocessing failed: {}", preprocessResult.getIssues());
            return new ProcessingResult(false, null, null, preprocessResult.getIssues());
        }
        
        Event processedEvent = preprocessResult.getProcessedEvent();
        
        // 이벤트 저장
        Event savedEvent = eventRepository.save(processedEvent);
        
        // 이상 탐지
        EventProcessingService.AnomalyDetectionResult anomalyResult = 
            eventProcessingService.detectAnomalies(processedEvent);
        
        // 활성화된 룰들과 매칭
        List<Rule> activeRules = ruleRepository.findByEnabled(true);
        List<Rule> matchedRules = activeRules.stream()
                .filter(rule -> eventProcessingService.doesEventMatchRule(processedEvent, rule))
                .collect(Collectors.toList());
        
        // 알림 생성
        List<AlertDto> alerts = matchedRules.stream()
                .map(rule -> createAlert(processedEvent, rule))
                .collect(Collectors.toList());
        
        ProcessingResult result = new ProcessingResult(
            true,
            convertToDto(savedEvent),
            alerts,
            preprocessResult.getIssues()
        );
        
        logger.debug("✅ Event processed successfully: {} alerts generated", alerts.size());
        return result;
    }

    /**
     * 🔍 이벤트 조회
     */
    public List<EventDto> findEvents(String userId, long fromTimestamp, long toTimestamp) {
        logger.debug("🔍 Finding events for user: {} from {} to {}", userId, fromTimestamp, toTimestamp);
        
        List<Event> events = eventRepository.findByUserIdAndTimestampBetween(userId, fromTimestamp, toTimestamp);
        
        return events.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 🔍 최근 이벤트 조회
     */
    public List<EventDto> findRecentEvents(int limit) {
        logger.debug("🔍 Finding recent events: limit={}", limit);
        
        List<Event> events = eventRepository.findRecentEvents(limit);
        
        return events.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 📊 이벤트 통계 조회
     */
    public EventStatistics getEventStatistics() {
        logger.debug("📊 Getting event statistics");
        
        Map<String, EventProcessingService.EventStats> statsMap = eventProcessingService.getEventStats();
        
        long totalEvents = statsMap.values().stream()
                .mapToLong(EventProcessingService.EventStats::getCount)
                .sum();
        
        double totalAmount = statsMap.values().stream()
                .mapToDouble(EventProcessingService.EventStats::getTotalAmount)
                .sum();
        
        return new EventStatistics(totalEvents, totalAmount, statsMap.size());
    }

    /**
     * 🚨 알림 생성
     */
    private AlertDto createAlert(Event event, Rule rule) {
        return new AlertDto(
            generateAlertId(),
            rule.getRuleId(),
            rule.getRuleName(),
            rule.getSeverity(),
            event.getEventId(),
            event.getUserId(),
            generateAlertMessage(event, rule),
            System.currentTimeMillis()
        );
    }

    /**
     * 🔤 알림 ID 생성
     */
    private String generateAlertId() {
        return "alert_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }

    /**
     * 📝 알림 메시지 생성
     */
    private String generateAlertMessage(Event event, Rule rule) {
        StringBuilder message = new StringBuilder();
        message.append("🚨 ").append(rule.getRuleName()).append(" 룰이 탐지되었습니다!\n");
        message.append("📊 심각도: ").append(rule.getSeverity().getDescription()).append("\n");
        message.append("👤 사용자: ").append(event.getUserId()).append("\n");
        
        if (event.getAmount() != null) {
            message.append("💰 금액: ").append(event.getAmount()).append("\n");
        }
        
        if (event.getRegion() != null) {
            message.append("🌍 지역: ").append(event.getRegion()).append("\n");
        }
        
        if (event.getDeviceType() != null) {
            message.append("📱 디바이스: ").append(event.getDeviceType()).append("\n");
        }
        
        message.append("⏰ 시간: ").append(java.time.Instant.ofEpochMilli(event.getTimestamp()));
        
        return message.toString();
    }

    /**
     * 🔄 도메인 모델을 DTO로 변환
     */
    private EventDto convertToDto(Event event) {
        return new EventDto(
            event.getEventId(),
            event.getEventType(),
            event.getTimestamp(),
            event.getUserId(),
            event.getSessionId(),
            event.getAmount(),
            event.getRegion(),
            event.getDeviceType(),
            event.getIpAddress(),
            event.getProperties()
        );
    }

    /**
     * 🔄 처리 결과 클래스
     */
    public static class ProcessingResult {
        private final boolean success;
        private final EventDto processedEvent;
        private final List<AlertDto> alerts;
        private final List<String> issues;

        public ProcessingResult(boolean success, EventDto processedEvent, List<AlertDto> alerts, List<String> issues) {
            this.success = success;
            this.processedEvent = processedEvent;
            this.alerts = alerts;
            this.issues = issues;
        }

        public boolean isSuccess() { return success; }
        public EventDto getProcessedEvent() { return processedEvent; }
        public List<AlertDto> getAlerts() { return alerts; }
        public List<String> getIssues() { return issues; }

        @Override
        public String toString() {
            return String.format("ProcessingResult{success=%s, alerts=%d, issues=%d}",
                    success, alerts != null ? alerts.size() : 0, issues != null ? issues.size() : 0);
        }
    }

    /**
     * 📊 이벤트 통계 클래스
     */
    public static class EventStatistics {
        private final long totalEvents;
        private final double totalAmount;
        private final int uniqueEventTypes;

        public EventStatistics(long totalEvents, double totalAmount, int uniqueEventTypes) {
            this.totalEvents = totalEvents;
            this.totalAmount = totalAmount;
            this.uniqueEventTypes = uniqueEventTypes;
        }

        public long getTotalEvents() { return totalEvents; }
        public double getTotalAmount() { return totalAmount; }
        public int getUniqueEventTypes() { return uniqueEventTypes; }

        @Override
        public String toString() {
            return String.format("EventStatistics{totalEvents=%d, totalAmount=%.2f, uniqueEventTypes=%d}",
                    totalEvents, totalAmount, uniqueEventTypes);
        }
    }
} 