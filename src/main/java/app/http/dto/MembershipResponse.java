package app.http.dto;

import app.domain.model.Membership;
import app.domain.model.MembershipStatus;
import app.domain.model.MembershipType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record MembershipResponse(
        Long id, Long clientId, MembershipType type, MembershipStatus status,
        LocalDate startDate, LocalDate endDate, Integer totalVisits, Integer remainingVisits,
        int frozenDays, LocalDate freezeStartDate, BigDecimal price, Instant createdAt
) {
    public static MembershipResponse fromDomain(Membership m) {
        return new MembershipResponse(
                m.getId(), m.getClientId(), m.getType(), m.getStatus(),
                m.getStartDate(), m.getEndDate(), m.getTotalVisits(), m.getRemainingVisits(),
                m.getFrozenDays(), m.getFreezeStartDate(), m.getPrice(), m.getCreatedAt()
        );
    }
}
