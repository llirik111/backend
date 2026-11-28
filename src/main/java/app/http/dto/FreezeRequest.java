package app.http.dto;

import java.time.LocalDate;

public record FreezeRequest(
        Integer days,
        LocalDate requestDate
) {}
