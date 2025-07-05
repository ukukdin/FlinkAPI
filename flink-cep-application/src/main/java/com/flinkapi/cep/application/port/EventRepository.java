package com.flinkapi.cep.application.port;

import com.flinkapi.cep.domain.model.Event;

import java.util.List;
import java.util.Optional;

/**
 * 🗃️ 이벤트 리포지토리 포트 인터페이스 - 이벤트 영속성 계층의 추상화
 * DDD 패턴으로 설계된 Repository 인터페이스
 */
public interface EventRepository {
    
    /**
     * 🔍 이벤트 저장
     */
    Event save(Event event);
    
    /**
     * 🔍 이벤트 ID로 조회
     */
    Optional<Event> findById(String eventId);
    
    /**
     * 🔍 사용자 ID로 조회
     */
    List<Event> findByUserId(String userId);
    
    /**
     * 🔍 사용자 ID와 시간 범위로 조회
     */
    List<Event> findByUserIdAndTimestampBetween(String userId, long fromTimestamp, long toTimestamp);
    
    /**
     * 🔍 이벤트 타입으로 조회
     */
    List<Event> findByEventType(String eventType);
    
    /**
     * 🔍 최근 이벤트 조회
     */
    List<Event> findRecentEvents(int limit);
    
    /**
     * 🔍 시간 범위로 조회
     */
    List<Event> findByTimestampBetween(long fromTimestamp, long toTimestamp);
    
    /**
     * 🔍 지역별 조회
     */
    List<Event> findByRegion(String region);
    
    /**
     * 🔍 디바이스 타입별 조회
     */
    List<Event> findByDeviceType(String deviceType);
    
    /**
     * 🔍 이벤트 존재 여부 확인
     */
    boolean existsById(String eventId);
    
    /**
     * 🗑️ 이벤트 삭제
     */
    void deleteById(String eventId);
    
    /**
     * 🗑️ 사용자의 모든 이벤트 삭제
     */
    void deleteByUserId(String userId);
    
    /**
     * 🗑️ 특정 시간 이전의 이벤트 삭제
     */
    void deleteByTimestampBefore(long timestamp);
    
    /**
     * 📊 이벤트 개수 조회
     */
    long count();
    
    /**
     * 📊 사용자별 이벤트 개수 조회
     */
    long countByUserId(String userId);
} 