package com.senity.waved.common;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
public class TimeUtil {
    public ZonedDateTime getTodayZoned() {
        return ZonedDateTime.now(ZoneId.systemDefault()).truncatedTo(ChronoUnit.DAYS);
    }

    public ZonedDateTime localToZoned(LocalDateTime local) {
        return ZonedDateTime.of(local, ZoneId.systemDefault());
    }

    public ZonedDateTime timeStampToZoned(Timestamp timestamp) {
        return timestamp.toLocalDateTime().atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay(ZoneId.systemDefault());
    }

    public ZonedDateTime calculateEndDate(ZonedDateTime startDate) {
        return startDate.withHour(23).withMinute(59).withSecond(59).withNano(999000000); // 23:59:59.999
    }

    public ZonedDateTime getQuizDate(Timestamp timestamp) {
        return timestamp.toInstant().atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.systemDefault()).truncatedTo(ChronoUnit.DAYS);
    }
}