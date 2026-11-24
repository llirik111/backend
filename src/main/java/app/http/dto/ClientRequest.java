package app.http.dto;

import java.time.LocalDate;

public record ClientRequest(
        String fullName,
        String phone,
        String email,
        LocalDate birthDate,
        String preferredLocker
) {
    public ClientRequest(String fullName, String phone, String email, LocalDate birthDate) {
        this(fullName, phone, email, birthDate, null);
    }
}
