package com.flinkapi.cep.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 *  실시간 이벤트 모델 - 모든 이벤트의 기본 구조
 * 바이브 코딩으로 만든 초강력 이벤트 클래스!
 */
public class Event implements Serializable {
    
    @JsonProperty("eventId")
    private String eventId;
    
    @JsonProperty("eventType")
    private String eventType;
    
    @JsonProperty("timestamp")
    private long timestamp;
    
    @JsonProperty("userId")
    private String userId;
    
    @JsonProperty("sessionId")
    private String sessionId;
    
    @JsonProperty("amount")
    private Double amount;
    
    @JsonProperty("region")
    private String region;
    
    @JsonProperty("deviceType")
    private String deviceType;
    
    @JsonProperty("ipAddress")
    private String ipAddress;
    
    @JsonProperty("properties")
    private Map<String, Object> properties;

    // 기본 생성자
    public Event() {
        this.timestamp = Instant.now().toEpochMilli();
    }

    // 빌더 패턴을 위한 생성자
    public Event(String eventId, String eventType) {
        this();
        this.eventId = eventId;
        this.eventType = eventType;
    }

    // Fluent API 스타일 빌더 메서드들
    public Event withUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public Event withAmount(Double amount) {
        this.amount = amount;
        return this;
    }

    public Event withRegion(String region) {
        this.region = region;
        return this;
    }

    public Event withDeviceType(String deviceType) {
        this.deviceType = deviceType;
        return this;
    }

    public Event withIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
        return this;
    }

    public Event withSessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    public Event withProperties(Map<String, Object> properties) {
        this.properties = properties;
        return this;
    }

    public Event withTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    // 바이브한 헬퍼 메서드들
    public boolean isHighValueTransaction() {
        return amount != null && amount > 10000.0;
    }

    public boolean isFromKorea() {
        return "KR".equals(region) || "Korea".equals(region);
    }

    public boolean isMobileDevice() {
        return "mobile".equalsIgnoreCase(deviceType) || "smartphone".equalsIgnoreCase(deviceType);
    }

    public boolean isRecentEvent(long maxAgeMillis) {
        return (Instant.now().toEpochMilli() - timestamp) <= maxAgeMillis;
    }

    // Getter/Setter 메서드들
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public Map<String, Object> getProperties() { return properties; }
    public void setProperties(Map<String, Object> properties) { this.properties = properties; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return timestamp == event.timestamp &&
               Objects.equals(eventId, event.eventId) &&
               Objects.equals(eventType, event.eventType) &&
               Objects.equals(userId, event.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId, eventType, timestamp, userId);
    }

    @Override
    public String toString() {
        return String.format("Event{eventId='%s', eventType='%s', userId='%s', amount=%s, region='%s', timestamp=%d}",
                eventId, eventType, userId, amount, region, timestamp);
    }
} 