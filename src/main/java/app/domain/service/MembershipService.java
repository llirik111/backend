package app.domain.service;

import app.domain.exception.BusinessRuleException;
import app.domain.exception.ConflictException;
import app.domain.exception.NotFoundException;
import app.domain.exception.ValidationException;
import app.domain.model.Client;
import app.domain.model.ClientStatus;
import app.domain.model.DaytimeMembership;
import app.domain.model.Membership;
import app.domain.model.MembershipStatus;
import app.domain.model.MembershipType;
import app.domain.model.UnlimitedMembership;
import app.domain.model.VisitBasedMembership;
import app.domain.repository.MembershipRepository;
import app.domain.repository.VisitRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class MembershipService {
    private final MembershipRepository membershipRepository;
    private final VisitRepository visitRepository;
    private final ClientService clientService;

    public MembershipService(MembershipRepository membershipRepository, VisitRepository visitRepository, ClientService clientService) {
        this.membershipRepository = membershipRepository;
        this.visitRepository = visitRepository;
        this.clientService = clientService;
    }

    public Membership createMembership(Long clientId, MembershipType type, LocalDate startDate, int durationDays, Integer totalVisits, BigDecimal price) {
        Client client = clientService.getClient(clientId);
        if (client.getStatus() == ClientStatus.BLOCKED) {
            throw new BusinessRuleException("Cannot issue membership to a blocked client");
        }
        if (type == null) throw new ValidationException("Membership type must not be null");
        Membership membership = switch (type) {
            case UNLIMITED -> UnlimitedMembership.create(clientId, startDate, durationDays, price);
            case VISIT_BASED -> {
                if (totalVisits == null || totalVisits <= 0) {
                    throw new ValidationException("Total visits must be specified and positive for visit-based membership");
                }
                yield VisitBasedMembership.create(clientId, startDate, durationDays, totalVisits, price);
            }
            case DAYTIME -> DaytimeMembership.create(clientId, startDate, durationDays, price);
        };
        return membershipRepository.save(membership);
    }

    public Membership getMembership(Long id) {
        if (id == null) throw new NotFoundException("Membership id must not be null");
        return membershipRepository.findById(id).orElseThrow(() -> new NotFoundException("Membership not found with id: " + id));
    }

    public Membership updateMembership(Long id, LocalDate endDate, BigDecimal price) {
        Membership membership = getMembership(id);
        membership.update(endDate, price);
        return membershipRepository.save(membership);
    }

    public void deleteMembership(Long id) {
        if (!membershipRepository.existsById(id)) throw new NotFoundException("Membership not found with id: " + id);
        if (visitRepository.existsActiveByMembershipId(id)) {
            throw new ConflictException("Cannot delete membership while client is using it inside the gym");
        }
        membershipRepository.deleteById(id);
    }

    public Membership cancelMembership(Long id) {
        Membership membership = getMembership(id);
        if (visitRepository.existsActiveByMembershipId(id)) {
            throw new ConflictException("Cannot cancel membership while a visit is in progress");
        }
        membership.cancel();
        return membershipRepository.save(membership);
    }

    public List<Membership> listMemberships(Long clientId, MembershipType type, MembershipStatus status) {
        return membershipRepository.findByFilter(clientId, type, status);
    }
}
