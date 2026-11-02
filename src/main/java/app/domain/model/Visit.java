package app.domain.model;

import app.domain.exception.ConflictException;
import app.domain.exception.ValidationException;

import java.time.LocalDateTime;
import java.util.Objects;

public class Visit {
    private Long id;
    private Long clientId;
    private Long membershipId;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private VisitStatus status;
    private String lockerNumber;

    public Visit(Long id, Long clientId, Long membershipId, LocalDateTime entryTime, LocalDateTime exitTime, VisitStatus status, String lockerNumber) {
        if (clientId == null) throw new ValidationException("Client id must not be null");
        if (membershipId == null) throw new ValidationException("Membership id must not be null");
        if (entryTime == null) throw new ValidationException("Entry time must not be null");
        this.id = id;
        this.clientId = clientId;
        this.membershipId = membershipId;
        this.entryTime = entryTime;
        this.exitTime = exitTime;
        this.status = status != null ? status : VisitStatus.IN_PROGRESS;
        this.lockerNumber = lockerNumber;
    }

    public static Visit start(Long clientId, Long membershipId, LocalDateTime entryTime, String lockerNumber) {
        return new Visit(null, clientId, membershipId, entryTime != null ? entryTime : LocalDateTime.now(), null, VisitStatus.IN_PROGRESS, lockerNumber);
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
    public Long getClientId() { return clientId; }
    public Long getMembershipId() { return membershipId; }
    public LocalDateTime getEntryTime() { return entryTime; }
    public LocalDateTime getExitTime() { return exitTime; }
    public VisitStatus getStatus() { return status; }
    public String getLockerNumber() { return lockerNumber; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return Objects.equals(id, ((Visit) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
