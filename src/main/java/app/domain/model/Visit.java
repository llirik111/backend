package app.domain.model;

import app.domain.exception.ConflictException;
import app.domain.exception.ValidationException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "visits")
public class Visit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "membership_id", nullable = false)
    private Membership membership;

    @Column(name = "entry_time", nullable = false)
    private LocalDateTime entryTime;

    @Column(name = "exit_time")
    private LocalDateTime exitTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private VisitStatus status;

    @Column(name = "locker_number")
    private String lockerNumber;

    public Visit() {
    }

    public Visit(Long id, Client client, Membership membership, LocalDateTime entryTime, LocalDateTime exitTime, VisitStatus status, String lockerNumber) {
        if (client == null) throw new ValidationException("Client must not be null");
        if (membership == null) throw new ValidationException("Membership must not be null");
        if (entryTime == null) throw new ValidationException("Entry time must not be null");
        this.id = id;
        this.client = client;
        this.membership = membership;
        this.entryTime = entryTime;
        this.exitTime = exitTime;
        this.status = status != null ? status : VisitStatus.IN_PROGRESS;
        this.lockerNumber = lockerNumber;
    }

    public Visit(Long id, Long clientId, Long membershipId, LocalDateTime entryTime, LocalDateTime exitTime, VisitStatus status, String lockerNumber) {
        this(id, new Client(clientId), new UnlimitedMembership(membershipId), entryTime, exitTime, status, lockerNumber);
    }

    public static Visit start(Client client, Membership membership, LocalDateTime entryTime, String lockerNumber) {
        return new Visit(null, client, membership, entryTime != null ? entryTime : LocalDateTime.now(), null, VisitStatus.IN_PROGRESS, lockerNumber);
    }

    public static Visit start(Long clientId, Long membershipId, LocalDateTime entryTime, String lockerNumber) {
        return start(new Client(clientId), new UnlimitedMembership(membershipId), entryTime, lockerNumber);
    }

    public void complete(LocalDateTime exitTime) {
        if (this.status == VisitStatus.COMPLETED) throw new ConflictException("Visit is already completed");
        LocalDateTime exit = exitTime != null ? exitTime : LocalDateTime.now();
        if (exit.isBefore(this.entryTime)) throw new ValidationException("Exit time cannot be before entry time");
        this.exitTime = exit;
        this.status = VisitStatus.COMPLETED;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }
    public Membership getMembership() { return membership; }
    public void setMembership(Membership membership) { this.membership = membership; }
    public Long getClientId() { return client != null ? client.getId() : null; }
    public Long getMembershipId() { return membership != null ? membership.getId() : null; }
    public LocalDateTime getEntryTime() { return entryTime; }
    public LocalDateTime getExitTime() { return exitTime; }
    public VisitStatus getStatus() { return status; }
    public String getLockerNumber() { return lockerNumber; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Visit visit = (Visit) o;
        return Objects.equals(id, visit.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
