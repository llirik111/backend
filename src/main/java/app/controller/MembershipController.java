package app.controller;

import app.domain.exception.ValidationException;
import app.domain.model.Membership;
import app.domain.model.MembershipStatus;
import app.domain.model.MembershipType;
import app.domain.model.Visit;
import app.domain.service.GymOperationService;
import app.domain.service.MembershipService;
import app.http.dto.CheckInRequest;
import app.http.dto.FreezeRequest;
import app.http.dto.MembershipRequest;
import app.http.dto.MembershipResponse;
import app.http.dto.VisitResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/memberships")
public class MembershipController {
    private final MembershipService membershipService;
    private final GymOperationService gymOperationService;

    public MembershipController(MembershipService membershipService, GymOperationService gymOperationService) {
        this.membershipService = membershipService;
        this.gymOperationService = gymOperationService;
    }

    @GetMapping
    public List<MembershipResponse> list(@RequestParam(required = false) Long clientId, @RequestParam(required = false) MembershipType type, @RequestParam(required = false) MembershipStatus status) {
        return membershipService.listMemberships(clientId, type, status).stream().map(MembershipResponse::fromDomain).toList();
    }

    @PostMapping
    public ResponseEntity<MembershipResponse> create(@RequestBody(required = false) MembershipRequest req) {
        if (req == null) throw new ValidationException("Request body must not be null");
        int duration = req.durationDays() != null ? req.durationDays() : 30;
        Membership m = membershipService.createMembership(req.clientId(), req.type(), req.startDate(), duration, req.totalVisits(), req.price());
        return ResponseEntity.created(URI.create("/api/memberships/" + m.getId())).body(MembershipResponse.fromDomain(m));
    }

    @GetMapping("/{id}")
    public MembershipResponse get(@PathVariable Long id) {
        return MembershipResponse.fromDomain(membershipService.getMembership(id));
    }

    @PutMapping("/{id}")
    public MembershipResponse update(@PathVariable Long id, @RequestBody(required = false) MembershipRequest req) {
        if (req == null) throw new ValidationException("Request body must not be null");
        return MembershipResponse.fromDomain(membershipService.updateMembership(id, req.endDate(), req.price()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        membershipService.deleteMembership(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/freeze")
    public MembershipResponse freeze(@PathVariable Long id, @RequestBody(required = false) FreezeRequest req) {
        int days = (req != null && req.days() != null) ? req.days() : 7;
        LocalDate date = (req != null && req.requestDate() != null) ? req.requestDate() : LocalDate.now();
        return MembershipResponse.fromDomain(gymOperationService.freezeMembership(id, days, date));
    }

    @PostMapping("/{id}/unfreeze")
    public MembershipResponse unfreeze(@PathVariable Long id, @RequestBody(required = false) FreezeRequest req) {
        LocalDate date = (req != null && req.requestDate() != null) ? req.requestDate() : LocalDate.now();
        return MembershipResponse.fromDomain(gymOperationService.unfreezeMembership(id, date));
    }

    @PostMapping("/{id}/check-in")
    public ResponseEntity<VisitResponse> checkIn(@PathVariable Long id, @RequestBody(required = false) CheckInRequest req) {
        LocalDateTime time = (req != null && req.visitTime() != null) ? req.visitTime() : LocalDateTime.now();
        String locker = req != null ? req.preferredLocker() : null;
        Visit v = gymOperationService.checkIn(id, time, locker);
        return ResponseEntity.created(URI.create("/api/visits/" + v.getId())).body(VisitResponse.fromDomain(v));
    }

    @PostMapping("/{id}/cancel")
    public MembershipResponse cancel(@PathVariable Long id) {
        return MembershipResponse.fromDomain(membershipService.cancelMembership(id));
    }
}
