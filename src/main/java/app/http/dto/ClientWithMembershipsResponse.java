package app.http.dto;

import app.domain.model.Client;
import app.domain.model.ClientStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record ClientWithMembershipsResponse(
        Long id,
        String fullName,
        String phone,
        String email,
        LocalDate birthDate,
        ClientStatus status,
        Instant createdAt,
        Instant updatedAt,
        String preferredLocker,
        List<MembershipResponse> memberships
) {
    public static ClientWithMembershipsResponse fromDomain(Client c) {
        List<MembershipResponse> mResponses = c.getMemberships() != null
                ? c.getMemberships().stream().map(MembershipResponse::fromDomain).toList()
                : List.of();
        return new ClientWithMembershipsResponse(
                c.getId(), c.getFullName(), c.getPhone(), c.getEmail(),
                c.getBirthDate(), c.getStatus(), c.getCreatedAt(), c.getUpdatedAt(),
                c.getPreferredLocker(), mResponses
        );
    }
}
