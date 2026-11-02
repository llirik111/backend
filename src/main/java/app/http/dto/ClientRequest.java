package app.http.dto;

import java.time.LocalDate;

public record ClientRequest(
        String fullName,
        String phone,
        String email,
        LocalDate birthDate
) {}
