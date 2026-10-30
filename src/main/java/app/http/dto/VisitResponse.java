package app.http.dto;

import app.domain.model.Visit;
import app.domain.model.VisitStatus;

import java.time.LocalDateTime;

public record VisitResponse(
        Long id, Long clientId, Long membershipId, LocalDateTime entryTime,
        LocalDateTime exitTime, VisitStatus status, String lockerNumber
) {
    public static VisitResponse fromDomain(Visit v) {
        return new VisitResponse(
                v.getId(), v.getClientId(), v.getMembershipId(), v.getEntryTime(),
                v.getExitTime(), v.getStatus(), v.getLockerNumber()
        );
    }
}
