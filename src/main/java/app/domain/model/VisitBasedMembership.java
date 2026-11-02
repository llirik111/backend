package app.domain.model;

import app.domain.exception.BusinessRuleException;
import app.domain.exception.ValidationException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class VisitBasedMembership extends Membership {
    private int totalVisits;
    private int remainingVisits;

    public VisitBasedMembership(Long id, Long clientId, MembershipStatus status,
                                LocalDate startDate, LocalDate endDate, int frozenDays,
                                LocalDate freezeStartDate, BigDecimal price, Instant createdAt,
                                int totalVisits, int remainingVisits) {
        super(id, clientId, MembershipType.VISIT_BASED, status, startDate, endDate, frozenDays, freezeStartDate, price, createdAt);
        if (totalVisits <= 0) throw new ValidationException("Total visits must be greater than zero");
        if (remainingVisits < 0 || remainingVisits > totalVisits) {
            throw new ValidationException("Remaining visits must be between 0 and total visits");
        }
        this.totalVisits = totalVisits;
        this.remainingVisits = remainingVisits;
    }

    public static VisitBasedMembership create(Long clientId, LocalDate startDate, int durationDays, int totalVisits, BigDecimal price) {
        if (durationDays <= 0) throw new ValidationException("Duration days must be positive");
        if (totalVisits <= 0) throw new ValidationException("Total visits must be greater than zero");
        LocalDate start = startDate != null ? startDate : LocalDate.now();
        LocalDate end = start.plusDays(durationDays);
        return new VisitBasedMembership(null, clientId, MembershipStatus.ACTIVE, start, end, 0, null, price, Instant.now(), totalVisits, totalVisits);
    }

    @Override
    public void validateCheckIn(LocalDateTime visitTime) {
        if (status == MembershipStatus.CANCELLED) throw new BusinessRuleException("Membership is cancelled");
        if (status == MembershipStatus.EXPIRED || remainingVisits <= 0) {
            throw new BusinessRuleException("No visits remaining on this membership");
        }
        LocalDate visitDate = visitTime.toLocalDate();
        if (visitDate.isBefore(startDate) || visitDate.isAfter(endDate)) {
            throw new BusinessRuleException("Check-in date is outside membership validity period (" + startDate + " to " + endDate + ")");
        }
    }

    @Override
    public void applyCheckIn(LocalDateTime visitTime) {
        validateCheckIn(visitTime);
        this.remainingVisits--;
        if (this.remainingVisits == 0) {
            this.status = MembershipStatus.EXPIRED;
        }
    }

    @Override
    public void freeze(int days, LocalDate requestDate) {
        throw new BusinessRuleException("Visit-based memberships do not support freezing");
    }

    @Override
    public void unfreeze(LocalDate requestDate) {
        throw new BusinessRuleException("Visit-based memberships do not support freezing or unfreezing");
    }

    @Override
    public Integer getTotalVisits() { return totalVisits; }

    @Override
    public Integer getRemainingVisits() { return remainingVisits; }
}
