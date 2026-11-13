package app.domain.service;

import app.domain.exception.BusinessRuleException;
import app.domain.exception.ConflictException;
import app.domain.exception.NotFoundException;
import app.domain.exception.ValidationException;
import app.domain.model.Client;
import app.domain.model.Membership;
import app.domain.model.MembershipStatus;
import app.domain.model.MembershipType;
import app.domain.model.Visit;
import app.domain.repository.ClientRepository;
import app.domain.repository.MembershipRepository;
import app.domain.repository.VisitRepository;
import app.storage.InMemoryClientRepository;
import app.storage.InMemoryMembershipRepository;
import app.storage.InMemoryVisitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MembershipServiceTest {

    private ClientRepository clientRepository;
    private MembershipRepository membershipRepository;
    private VisitRepository visitRepository;
    private ClientService clientService;
    private MembershipService membershipService;
    private Client testClient;

    @BeforeEach
    void setUp() {
        clientRepository = new InMemoryClientRepository();
        membershipRepository = new InMemoryMembershipRepository();
        visitRepository = new InMemoryVisitRepository();
        clientService = new ClientService(clientRepository, membershipRepository, visitRepository);
        membershipService = new MembershipService(membershipRepository, visitRepository, clientService);
        testClient = clientService.createClient("Test User", "+79991234567", "test@test.com", LocalDate.of(1990, 1, 1));
    }

    @Test
    void testCreateMembershipTypes() {
        Membership unl = membershipService.createMembership(testClient.getId(), MembershipType.UNLIMITED, LocalDate.now(), 30, null, BigDecimal.valueOf(3000));
        assertNotNull(unl.getId());
        assertEquals(MembershipStatus.ACTIVE, unl.getStatus());

        Membership vb = membershipService.createMembership(testClient.getId(), MembershipType.VISIT_BASED, LocalDate.now(), 30, 5, BigDecimal.valueOf(1500));
        assertEquals(Integer.valueOf(5), vb.getTotalVisits());
        assertEquals(Integer.valueOf(5), vb.getRemainingVisits());

        Membership dt = membershipService.createMembership(testClient.getId(), MembershipType.DAYTIME, LocalDate.now(), 30, null, BigDecimal.valueOf(2000));
        assertEquals(MembershipType.DAYTIME, dt.getType());
    }

    @Test
    void testCreateMembershipValidation() {
        assertThrows(NotFoundException.class, () ->
                membershipService.createMembership(999L, MembershipType.UNLIMITED, LocalDate.now(), 30, null, BigDecimal.valueOf(1000)));

        clientService.blockClient(testClient.getId());
        assertThrows(BusinessRuleException.class, () ->
                membershipService.createMembership(testClient.getId(), MembershipType.UNLIMITED, LocalDate.now(), 30, null, BigDecimal.valueOf(1000)));

        clientService.unblockClient(testClient.getId());
        assertThrows(ValidationException.class, () ->
                membershipService.createMembership(testClient.getId(), null, LocalDate.now(), 30, null, BigDecimal.valueOf(1000)));
        assertThrows(ValidationException.class, () ->
                membershipService.createMembership(testClient.getId(), MembershipType.VISIT_BASED, LocalDate.now(), 30, null, BigDecimal.valueOf(1000)));
    }

    @Test
    void testUpdateMembership() {
        Membership m = membershipService.createMembership(testClient.getId(), MembershipType.UNLIMITED, LocalDate.of(2026, 1, 1), 30, null, BigDecimal.valueOf(3000));
        Membership updated = membershipService.updateMembership(m.getId(), LocalDate.of(2026, 3, 1), BigDecimal.valueOf(4000));
        assertEquals(LocalDate.of(2026, 3, 1), updated.getEndDate());
        assertEquals(BigDecimal.valueOf(4000), updated.getPrice());

        assertThrows(ValidationException.class, () ->
                membershipService.updateMembership(m.getId(), null, BigDecimal.valueOf(-100)));
    }

    @Test
    void testCancelMembership() {
        Membership m = membershipService.createMembership(testClient.getId(), MembershipType.UNLIMITED, LocalDate.now(), 30, null, BigDecimal.valueOf(3000));
        Visit v = visitRepository.save(Visit.start(testClient.getId(), m.getId(), LocalDateTime.now(), "L-101"));

        assertThrows(ConflictException.class, () -> membershipService.cancelMembership(m.getId()));

        v.complete(LocalDateTime.now());
        Membership cancelled = membershipService.cancelMembership(m.getId());
        assertEquals(MembershipStatus.CANCELLED, cancelled.getStatus());
    }

    @Test
    void testDeleteAndListMemberships() {
        Membership m1 = membershipService.createMembership(testClient.getId(), MembershipType.UNLIMITED, LocalDate.now(), 30, null, BigDecimal.valueOf(3000));
        membershipService.createMembership(testClient.getId(), MembershipType.VISIT_BASED, LocalDate.now(), 30, 5, BigDecimal.valueOf(1500));

        List<Membership> unlOnly = membershipService.listMemberships(null, MembershipType.UNLIMITED, null);
        assertEquals(1, unlOnly.size());

        membershipService.deleteMembership(m1.getId());
        assertThrows(NotFoundException.class, () -> membershipService.getMembership(m1.getId()));
    }
}
