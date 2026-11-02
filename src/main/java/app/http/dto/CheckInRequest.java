package app.http.dto;

import java.time.LocalDateTime;

public record CheckInRequest(
        LocalDateTime visitTime,
        String preferredLocker
) {}
