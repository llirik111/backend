package app.domain.model;

import app.domain.exception.ConflictException;
import app.domain.exception.ValidationException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public abstract class Membership {
    protected Long id;
    protected Long clientId;
    protected MembershipType type;
    protected MembershipStatus status;
    protected LocalDate startDate;
    protected LocalDate endDate;
    protected int frozenDays;
    protected LocalDate freezeStartDate;
    protected BigDecimal price;
    protected Instant createdAt;

    public Membership(Long id, Long clientId, MembershipType type, MembershipStatus status,
                      LocalDate startDate, LocalDate endDate, int frozenDays,
                      LocalDate freezeStartDate, BigDecimal price, Instant createdAt) {
        if (clientId == null) throw new ValidationException("Client id must not be null");
        if (startDate == null || endDate == null) throw new ValidationException("Start and end dates must not be null");
        if (endDate.isBefore(startDate)) throw new ValidationException("End date cannot be before start date");
        if (price != null && price.compareTo(BigDecimal.ZERO) < 0) throw new ValidationException("Price cannot be negative");
        this.id = id;
        this.clientId = clientId;
        this.type = type;
        this.status = status != null ? status : MembershipStatus.ACTIVE;
        this.startDate = startDate;
        this.endDate = endDate;
        this.frozenDays = Math.max(0, frozenDays);
        this.freezeStartDate = freezeStartDate;
        this.price = price != null ? price : BigDecimal.ZERO;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public abstract void validateCheckIn(LocalDateTime visitTime);
    public abstract void applyCheckIn(LocalDateTime visitTime);
    public abstract void freeze(int days, LocalDate requestDate);
    public abstract void unfreeze(LocalDate requestDate);
    public abstract Integer getTotalVisits();
    public abstract Integer getRemainingVisits();

    public void update(LocalDate newEndDate, BigDecimal newPrice) {
        if (this.status == MembershipStatus.CANCELLED || this.status == MembershipStatus.EXPIRED) {
            throw new ConflictException("Cannot modify cancelled or expired membership");
        }
        if (newEndDate != null) {
            if (newEndDate.isBefore(this.startDate)) throw new ValidationException("End date cannot be before start date");
            this.endDate = newEndDate;
        }
        if (newPrice != null) {
            if (newPrice.compareTo(BigDecimal.ZERO) < 0) throw new ValidationException("Price cannot be negative");
            this.price = newPrice;
        }
    }

    public void cancel() {
        if (this.status == MembershipStatus.CANCELLED) throw new ConflictException("Membership is already cancelled");
        this.status = MembershipStatus.CANCELLED;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getClientId() { return clientId; }
    public MembershipType getType() { return type; }
    public MembershipStatus getStatus() { return status; }
    public void setStatus(MembershipStatus status) { this.status = status; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public int getFrozenDays() { return frozenDays; }
    public LocalDate getFreezeStartDate() { return freezeStartDate; }
    public BigDecimal getPrice() { return price; }
    public Instant getCreatedAt() { return createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return Objects.equals(id, ((Membership) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
