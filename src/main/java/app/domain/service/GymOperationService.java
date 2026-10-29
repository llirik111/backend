package app.domain.service;

import app.domain.exception.BusinessRuleException;
import app.domain.exception.ConflictException;
import app.domain.exception.NotFoundException;
import app.domain.exception.ValidationException;
import app.domain.model.Client;
import app.domain.model.ClientStatus;
import app.domain.model.Membership;
import app.domain.model.Visit;
import app.domain.model.VisitStatus;
import app.domain.repository.MembershipRepository;
import app.domain.repository.VisitRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class GymOperationService {
    private final MembershipRepository membershipRepository;
    private final VisitRepository visitRepository;
    private final ClientService clientService;

    public GymOperationService(MembershipRepository membershipRepository, VisitRepository visitRepository, ClientService clientService) {
        this.membershipRepository = membershipRepository;
        this.visitRepository = visitRepository;
        this.clientService = clientService;
    }

    public synchronized Visit checkIn(Long membershipId, LocalDateTime visitTime, String preferredLocker) {
        if (membershipId == null) throw new ValidationException("Membership id must not be null");
        LocalDateTime time = visitTime != null ? visitTime : LocalDateTime.now();
        Membership membership = membershipRepository.findById(membershipId)
                .orElseThrow(() -> new NotFoundException("Membership not found with id: " + membershipId));

        Client client = clientService.getClient(membership.getClientId());
        if (client.getStatus() == ClientStatus.BLOCKED) {
            throw new BusinessRuleException("Client account is blocked. Check-in denied.");
        }
        if (visitRepository.existsActiveByClientId(client.getId())) {
            throw new ConflictException("Client is already checked in and inside the gym");
        }

        String assignedLocker = allocateLocker(preferredLocker);
        membership.validateCheckIn(time);
        membership.applyCheckIn(time);
        membershipRepository.save(membership);

        Visit visit = Visit.start(client.getId(), membership.getId(), time, assignedLocker);
        return visitRepository.save(visit);
    }

    public synchronized Visit checkOut(Long visitId, LocalDateTime exitTime) {
        if (visitId == null) throw new ValidationException("Visit id must not be null");
        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new NotFoundException("Visit not found with id: " + visitId));
        LocalDateTime time = exitTime != null ? exitTime : LocalDateTime.now();
        visit.complete(time);
        return visitRepository.save(visit);
    }

    public synchronized Membership freezeMembership(Long membershipId, int days, LocalDate requestDate) {
        if (membershipId == null) throw new ValidationException("Membership id must not be null");
        Membership membership = membershipRepository.findById(membershipId)
                .orElseThrow(() -> new NotFoundException("Membership not found with id: " + membershipId));
        if (visitRepository.existsActiveByClientId(membership.getClientId())) {
            throw new ConflictException("Cannot freeze membership while client is currently inside the gym");
        }
        LocalDate date = requestDate != null ? requestDate : LocalDate.now();
        membership.freeze(days, date);
        return membershipRepository.save(membership);
    }

    public synchronized Membership unfreezeMembership(Long membershipId, LocalDate requestDate) {
        if (membershipId == null) throw new ValidationException("Membership id must not be null");
        Membership membership = membershipRepository.findById(membershipId)
                .orElseThrow(() -> new NotFoundException("Membership not found with id: " + membershipId));
        LocalDate date = requestDate != null ? requestDate : LocalDate.now();
        membership.unfreeze(date);
        return membershipRepository.save(membership);
    }

    public Visit getVisit(Long id) {
        if (id == null) throw new NotFoundException("Visit id must not be null");
        return visitRepository.findById(id).orElseThrow(() -> new NotFoundException("Visit not found with id: " + id));
    }

    public List<Visit> listVisits(Long clientId, VisitStatus status) {
        return visitRepository.findByFilter(clientId, status);
    }

    private String allocateLocker(String preferredLocker) {
        if (preferredLocker != null && !preferredLocker.trim().isEmpty()) {
            String locker = preferredLocker.trim();
            if (visitRepository.existsActiveWithLocker(locker)) {
                throw new ConflictException("Locker " + locker + " is currently in use");
            }
            return locker;
        }
        for (int i = 101; i <= 200; i++) {
            String candidate = "L-" + i;
            if (!visitRepository.existsActiveWithLocker(candidate)) {
                return candidate;
            }
        }
        throw new BusinessRuleException("No available lockers currently");
    }
}
