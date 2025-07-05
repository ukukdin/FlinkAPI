package com.flinkapi.cep.application.dto;

import java.util.Map;

/**
 *  이벤트 데이터 전송 객체 - 이벤트 정보를 외부로 전달하는 DTO
 * 
 */
public class EventDto {
    
    private String eventId;
    private String eventType;
    private long timestamp;
    private String userId;
    private String sessionId;
    private Double amount;
    private String region;
    private String deviceType;
    private String ipAddress;
    private Map<String, Object> properties;

    // 기본 생성자
    public EventDto() {}

    // 전체 생성자
    public EventDto(String eventId, String eventType, long timestamp, String userId,
                   String sessionId, Double amount, String region, String deviceType,
                   String ipAddress, Map<String, Object> properties) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.timestamp = timestamp;
        this.userId = userId;
        this.sessionId = sessionId;
        this.amount = amount;
        this.region = region;
        this.deviceType = deviceType;
        this.ipAddress = ipAddress;
        this.properties = properties;
    }

    // Getters and Setters
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
    public String toString() {
        return String.format("EventDto{eventId='%s', eventType='%s', userId='%s', amount=%s, region='%s'}",
                eventId, eventType, userId, amount, region);
    }
} 