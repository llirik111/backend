package app.http.dto;

import app.domain.model.Client;
import app.domain.model.ClientStatus;

import java.time.Instant;
import java.time.LocalDate;

public record ClientResponse(
        Long id, String fullName, String phone, String email,
        LocalDate birthDate, ClientStatus status, Instant createdAt, Instant updatedAt,
        String preferredLocker
) {
    public ClientResponse(Long id, String fullName, String phone, String email, LocalDate birthDate, ClientStatus status, Instant createdAt, Instant updatedAt) {
        this(id, fullName, phone, email, birthDate, status, createdAt, updatedAt, null);
    }

    public static ClientResponse fromDomain(Client c) {
        return new ClientResponse(
                c.getId(), c.getFullName(), c.getPhone(), c.getEmail(),
                c.getBirthDate(), c.getStatus(), c.getCreatedAt(), c.getUpdatedAt(),
                c.getPreferredLocker()
        );
    }
}
