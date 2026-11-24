package app.domain.model;

import app.domain.exception.ConflictException;
import app.domain.exception.ValidationException;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "memberships")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING)
public abstract class Membership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    protected Client client;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", insertable = false, updatable = false)
    protected MembershipType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    protected MembershipStatus status;

    @Column(name = "start_date", nullable = false)
    protected LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    protected LocalDate endDate;

    @Column(name = "frozen_days", nullable = false)
    protected int frozenDays;

    @Column(name = "planned_freeze_days", nullable = false)
    protected int plannedFreezeDays;

    @Column(name = "freeze_start_date")
    protected LocalDate freezeStartDate;

    @Column(name = "price", nullable = false)
    protected BigDecimal price;

    @Column(name = "created_at", nullable = false)
    protected Instant createdAt;

    public Membership() {
    }

    public Membership(Long id) {
        this.id = id;
    }

    public Membership(Long id, Client client, MembershipType type, MembershipStatus status,
                      LocalDate startDate, LocalDate endDate, int frozenDays,
                      LocalDate freezeStartDate, BigDecimal price, Instant createdAt) {
        if (client == null) throw new ValidationException("Client must not be null");
        if (startDate == null || endDate == null) throw new ValidationException("Start and end dates must not be null");
        if (endDate.isBefore(startDate)) throw new ValidationException("End date cannot be before start date");
        if (price != null && price.compareTo(BigDecimal.ZERO) < 0) throw new ValidationException("Price cannot be negative");
        this.id = id;
        this.client = client;
        this.type = type;
        this.status = status != null ? status : MembershipStatus.ACTIVE;
        this.startDate = startDate;
        this.endDate = endDate;
        this.frozenDays = Math.max(0, frozenDays);
        this.plannedFreezeDays = 0;
        this.freezeStartDate = freezeStartDate;
        this.price = price != null ? price : BigDecimal.ZERO;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public Membership(Long id, Long clientId, MembershipType type, MembershipStatus status,
                      LocalDate startDate, LocalDate endDate, int frozenDays,
                      LocalDate freezeStartDate, BigDecimal price, Instant createdAt) {
        this(id, new Client(clientId), type, status, startDate, endDate, frozenDays, freezeStartDate, price, createdAt);
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
        if (newEndDate != null && newEndDate.isBefore(this.startDate)) {
            throw new ValidationException("End date cannot be before start date");
        }
        if (newPrice != null && newPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Price cannot be negative");
        }
        if (newEndDate != null) {
            this.endDate = newEndDate;
        }
        if (newPrice != null) {
            this.price = newPrice;
        }
    }

    public void cancel() {
        if (this.status == MembershipStatus.CANCELLED) throw new ConflictException("Membership is already cancelled");
        this.status = MembershipStatus.CANCELLED;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }
    public Long getClientId() { return client != null ? client.getId() : null; }
    public MembershipType getType() { return type; }
    public MembershipStatus getStatus() { return status; }
    public void setStatus(MembershipStatus status) { this.status = status; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public int getFrozenDays() { return frozenDays; }
    public int getPlannedFreezeDays() { return plannedFreezeDays; }
    public LocalDate getFreezeStartDate() { return freezeStartDate; }
    public BigDecimal getPrice() { return price; }
    public Instant getCreatedAt() { return createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Membership that = (Membership) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
