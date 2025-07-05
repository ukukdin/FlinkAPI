package com.flinkapi.cep.domain.value;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Objects;

/**
 * 시간 윈도우 값 객체 - 시간 범위를 표현하는 Value Object
 *
 */
public class TimeWindow implements Serializable {
    
    @JsonProperty("duration")
    private final long duration;
    
    @JsonProperty("unit")
    private final TimeUnit unit;

    // 기본 생성자 (Jackson 용)
    public TimeWindow() {
        this.duration = 0;
        this.unit = TimeUnit.SECONDS;
    }

    public TimeWindow(long duration, TimeUnit unit) {
        if (duration < 0) {
            throw new IllegalArgumentException("Duration cannot be negative");
        }
        if (unit == null) {
            throw new IllegalArgumentException("TimeUnit cannot be null");
        }
        this.duration = duration;
        this.unit = unit;
    }

    // 비즈니스 로직 메서드들
    public long toMilliseconds() {
        return duration * unit.getMilliseconds();
    }

    public long toSeconds() {
        return toMilliseconds() / 1000;
    }

    public long toMinutes() {
        return toMilliseconds() / (60 * 1000);
    }

    public long toHours() {
        return toMilliseconds() / (60 * 60 * 1000);
    }

    public boolean isValid() {
        return duration > 0 && unit != null;
    }

    public boolean isShortTerm() {
        return toMinutes() <= 5;
    }

    public boolean isLongTerm() {
        return toHours() >= 1;
    }

    // 팩토리 메서드들
    public static TimeWindow ofMilliseconds(long duration) {
        return new TimeWindow(duration, TimeUnit.MILLISECONDS);
    }

    public static TimeWindow ofSeconds(long duration) {
        return new TimeWindow(duration, TimeUnit.SECONDS);
    }

    public static TimeWindow ofMinutes(long duration) {
        return new TimeWindow(duration, TimeUnit.MINUTES);
    }

    public static TimeWindow ofHours(long duration) {
        return new TimeWindow(duration, TimeUnit.HOURS);
    }

    public static TimeWindow ofDays(long duration) {
        return new TimeWindow(duration, TimeUnit.DAYS);
    }

    // 시간 단위 열거형
    public enum TimeUnit {
        @JsonProperty("MILLISECONDS")
        MILLISECONDS(1),
        
        @JsonProperty("SECONDS")
        SECONDS(1000),
        
        @JsonProperty("MINUTES")
        MINUTES(60 * 1000),
        
        @JsonProperty("HOURS")
        HOURS(60 * 60 * 1000),
        
        @JsonProperty("DAYS")
        DAYS(24 * 60 * 60 * 1000);

        private final long milliseconds;

        TimeUnit(long milliseconds) {
            this.milliseconds = milliseconds;
        }

        public long getMilliseconds() {
            return milliseconds;
        }
    }

    // Getter 메서드들 (불변 객체)
    public long getDuration() { return duration; }
    public TimeUnit getUnit() { return unit; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeWindow that = (TimeWindow) o;
        return duration == that.duration && unit == that.unit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(duration, unit);
    }

    @Override
    public String toString() {
        return String.format("TimeWindow{duration=%d %s}", duration, unit);
    }
} 