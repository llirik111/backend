package app.domain.model;

import app.domain.exception.BusinessRuleException;
import app.domain.exception.ValidationException;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@DiscriminatorValue("VISIT_BASED")
public class VisitBasedMembership extends Membership {

    @Column(name = "total_visits")
    private Integer totalVisits;

    @Column(name = "remaining_visits")
    private Integer remainingVisits;

    public VisitBasedMembership() {
    }

    public VisitBasedMembership(Long id, Client client, MembershipStatus status,
                                LocalDate startDate, LocalDate endDate, int frozenDays,
                                LocalDate freezeStartDate, BigDecimal price, Instant createdAt,
                                int totalVisits, int remainingVisits) {
        super(id, client, MembershipType.VISIT_BASED, status, startDate, endDate, frozenDays, freezeStartDate, price, createdAt);
        if (totalVisits <= 0) throw new ValidationException("Total visits must be greater than zero");
        if (remainingVisits < 0 || remainingVisits > totalVisits) {
            throw new ValidationException("Remaining visits must be between 0 and total visits");
        }
        this.totalVisits = totalVisits;
        this.remainingVisits = remainingVisits;
    }

    public VisitBasedMembership(Long id, Long clientId, MembershipStatus status,
                                LocalDate startDate, LocalDate endDate, int frozenDays,
                                LocalDate freezeStartDate, BigDecimal price, Instant createdAt,
                                int totalVisits, int remainingVisits) {
        this(id, new Client(clientId), status, startDate, endDate, frozenDays, freezeStartDate, price, createdAt, totalVisits, remainingVisits);
    }

    public static VisitBasedMembership create(Client client, LocalDate startDate, int durationDays, int totalVisits, BigDecimal price) {
        if (durationDays <= 0) throw new ValidationException("Duration days must be positive");
        if (totalVisits <= 0) throw new ValidationException("Total visits must be greater than zero");
        LocalDate start = startDate != null ? startDate : LocalDate.now();
        return new VisitBasedMembership(null, client, MembershipStatus.ACTIVE, start, start.plusDays(durationDays), 0, null, price, Instant.now(), totalVisits, totalVisits);
    }

    public static VisitBasedMembership create(Long clientId, LocalDate startDate, int durationDays, int totalVisits, BigDecimal price) {
        return create(new Client(clientId), startDate, durationDays, totalVisits, price);
    }

    @Override
    public void validateCheckIn(LocalDateTime visitTime) {
        if (status == MembershipStatus.CANCELLED) throw new BusinessRuleException("Membership is cancelled");
        if (status == MembershipStatus.EXPIRED || (remainingVisits != null && remainingVisits <= 0)) {
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
        if (this.remainingVisits != null) {
            this.remainingVisits--;
            if (this.remainingVisits == 0) {
                this.status = MembershipStatus.EXPIRED;
            }
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
