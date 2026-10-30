package app.domain.model;

import app.domain.exception.BusinessRuleException;
import app.domain.exception.ConflictException;
import app.domain.exception.ValidationException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class UnlimitedMembership extends Membership {

    public UnlimitedMembership(Long id, Long clientId, MembershipStatus status,
                               LocalDate startDate, LocalDate endDate, int frozenDays,
                               LocalDate freezeStartDate, BigDecimal price, Instant createdAt) {
        super(id, clientId, MembershipType.UNLIMITED, status, startDate, endDate, frozenDays, freezeStartDate, price, createdAt);
    }

    public static UnlimitedMembership create(Long clientId, LocalDate startDate, int durationDays, BigDecimal price) {
        if (durationDays <= 0) throw new ValidationException("Duration days must be positive");
        LocalDate start = startDate != null ? startDate : LocalDate.now();
        LocalDate end = start.plusDays(durationDays);
        return new UnlimitedMembership(null, clientId, MembershipStatus.ACTIVE, start, end, 0, null, price, Instant.now());
    }

    @Override
    public void validateCheckIn(LocalDateTime visitTime) {
        if (status == MembershipStatus.FROZEN) throw new BusinessRuleException("Membership is frozen and cannot be used for check-in");
        if (status == MembershipStatus.EXPIRED) throw new BusinessRuleException("Membership has expired");
        if (status == MembershipStatus.CANCELLED) throw new BusinessRuleException("Membership is cancelled");
        LocalDate visitDate = visitTime.toLocalDate();
        if (visitDate.isBefore(startDate) || visitDate.isAfter(endDate)) {
            throw new BusinessRuleException("Check-in date is outside membership validity period (" + startDate + " to " + endDate + ")");
        }
    }

    @Override
    public void applyCheckIn(LocalDateTime visitTime) {
        validateCheckIn(visitTime);
    }

    @Override
    public void freeze(int days, LocalDate requestDate) {
        if (status != MembershipStatus.ACTIVE) throw new ConflictException("Only active membership can be frozen");
        if (days <= 0 || days > 30) throw new ValidationException("Freeze duration must be between 1 and 30 days");
        if (frozenDays + days > 30) throw new BusinessRuleException("Exceeded maximum allowed freeze limit (30 days total)");
        if (requestDate.isBefore(startDate) || requestDate.isAfter(endDate)) {
            throw new BusinessRuleException("Freeze request date is outside membership period");
        }
        this.status = MembershipStatus.FROZEN;
        this.freezeStartDate = requestDate;
        this.frozenDays += days;
    }

    @Override
    public void unfreeze(LocalDate requestDate) {
        if (status != MembershipStatus.FROZEN) throw new ConflictException("Membership is not currently frozen");
        long daysDiff = ChronoUnit.DAYS.between(freezeStartDate, requestDate);
        long daysToExtend = Math.max(1, daysDiff);
        this.endDate = this.endDate.plusDays(daysToExtend);
        this.status = MembershipStatus.ACTIVE;
        this.freezeStartDate = null;
    }

    @Override
    public Integer getTotalVisits() { return null; }

    @Override
    public Integer getRemainingVisits() { return null; }
}
