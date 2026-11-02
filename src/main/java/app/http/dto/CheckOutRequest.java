package app.http.dto;

import java.time.LocalDateTime;

public record CheckOutRequest(
        LocalDateTime exitTime
) {}
