package theworldofpuppies.UserService.utils;

import org.springframework.stereotype.Component;

import java.time.*;

@Component
public class DateTimeFormatter {

    private final ZoneId zone = ZoneId.systemDefault();

    public LocalDateTime convertLongToLocalDateTime(Long dateTime) {
        Instant instant = convertLongToInstant(dateTime);
        return LocalDateTime.ofInstant(instant, zone);
    }

    public LocalTime convertLongToLocalTime(Long dateTime) {
        Instant instant = convertLongToInstant(dateTime);
        return LocalTime.ofInstant(instant, zone);
    }

    public LocalDate convertLongToLocalDate(Long dateTime) {
        Instant instant = convertLongToInstant(dateTime);
        return LocalDate.ofInstant(instant, zone);
    }

    private Instant convertLongToInstant(Long dateTime) {
        return Instant.ofEpochMilli(dateTime);
    }

    public Long convertLocalDateTimeToLong(LocalDate date, LocalTime time) {
        return date.atTime(time).atZone(zone).toInstant().toEpochMilli();
    }


}
