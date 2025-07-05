package com.flinkapi.cep.application.port;

import com.flinkapi.cep.domain.model.Event;
import com.flinkapi.cep.domain.model.Rule;

import java.util.List;

/**
 * 🌊 스트림 프로세서 포트 인터페이스 (헥사고날 아키텍처)
 * 
 * Application 레이어에서 스트림 처리를 위한 아웃바운드 포트
 * Infrastructure 레이어에서 이 인터페이스를 구현합니다.
 */
public interface StreamProcessor {
    
    /**
     * 🚀 스트림 처리 시작
     * 
     * @param rules 처리할 룰 목록
     */
    void startProcessing(List<Rule> rules);
    
    /**
     * 🛑 스트림 처리 중지
     */
    void stopProcessing();
    
    /**
     * 🔄 룰 동적 업데이트
     * 
     * @param rules 업데이트할 룰 목록
     */
    void updateRules(List<Rule> rules);
    
    /**
     * 📊 단일 이벤트 처리
     * 
     * @param event 처리할 이벤트
     * @return 처리 결과
     */
    ProcessingResult processEvent(Event event);
    
    /**
     * 📈 스트림 상태 조회
     * 
     * @return 스트림 상태 정보
     */
    StreamStatus getStatus();
    
    /**
     * 🔧 스트림 설정 업데이트
     * 
     * @param config 설정 정보
     */
    void updateConfiguration(StreamConfiguration config);
    
    /**
     * 📊 처리 결과 클래스
     */
    class ProcessingResult {
        private final boolean success;
        private final String message;
        private final long processedCount;
        private final long errorCount;
        
        public ProcessingResult(boolean success, String message, long processedCount, long errorCount) {
            this.success = success;
            this.message = message;
            this.processedCount = processedCount;
            this.errorCount = errorCount;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public long getProcessedCount() { return processedCount; }
        public long getErrorCount() { return errorCount; }
    }
    
    /**
     * 📊 스트림 상태 클래스
     */
    class StreamStatus {
        private final boolean running;
        private final String status;
        private final long uptime;
        private final long totalProcessed;
        
        public StreamStatus(boolean running, String status, long uptime, long totalProcessed) {
            this.running = running;
            this.status = status;
            this.uptime = uptime;
            this.totalProcessed = totalProcessed;
        }
        
        public boolean isRunning() { return running; }
        public String getStatus() { return status; }
        public long getUptime() { return uptime; }
        public long getTotalProcessed() { return totalProcessed; }
    }
    
    /**
     * 🔧 스트림 설정 클래스
     */
    class StreamConfiguration {
        private final int parallelism;
        private final long checkpointInterval;
        private final String sourceConfig;
        private final String sinkConfig;
        
        public StreamConfiguration(int parallelism, long checkpointInterval, String sourceConfig, String sinkConfig) {
            this.parallelism = parallelism;
            this.checkpointInterval = checkpointInterval;
            this.sourceConfig = sourceConfig;
            this.sinkConfig = sinkConfig;
        }
        
        public int getParallelism() { return parallelism; }
        public long getCheckpointInterval() { return checkpointInterval; }
        public String getSourceConfig() { return sourceConfig; }
        public String getSinkConfig() { return sinkConfig; }
    }
} 