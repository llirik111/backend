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
import app.domain.model.VisitStatus;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class GymOperationServiceTest {

    private ClientRepository clientRepository;
    private MembershipRepository membershipRepository;
    private VisitRepository visitRepository;
    private ClientService clientService;
    private MembershipService membershipService;
    private GymOperationService gymOperationService;
    private Client client1;
    private Client client2;
    private Membership membership1;

    @BeforeEach
    void setUp() {
        clientRepository = new InMemoryClientRepository();
        membershipRepository = new InMemoryMembershipRepository();
        visitRepository = new InMemoryVisitRepository();
        clientService = new ClientService(clientRepository, membershipRepository, visitRepository);
        membershipService = new MembershipService(membershipRepository, visitRepository, clientService);
        gymOperationService = new GymOperationService(membershipRepository, visitRepository, clientService);

        client1 = clientService.createClient("Alice", "+79991111111", "alice@test.com", LocalDate.of(1990, 1, 1));
        client2 = clientService.createClient("Bob", "+79992222222", "bob@test.com", LocalDate.of(1992, 2, 2));
        membership1 = membershipService.createMembership(client1.getId(), MembershipType.UNLIMITED, LocalDate.now().minusDays(1), 30, null, BigDecimal.valueOf(3000));
    }

    @Test
    void testCheckInLockers() {
        Visit v1 = gymOperationService.checkIn(membership1.getId(), LocalDateTime.now(), null);
        assertEquals("L-101", v1.getLockerNumber());
        assertEquals(VisitStatus.IN_PROGRESS, v1.getStatus());

        Membership m2 = membershipService.createMembership(client2.getId(), MembershipType.UNLIMITED, LocalDate.now(), 30, null, BigDecimal.valueOf(3000));
        Visit v2 = gymOperationService.checkIn(m2.getId(), LocalDateTime.now(), "L-150");
        assertEquals("L-150", v2.getLockerNumber());
    }

    @Test
    void testCheckInValidationAndConflicts() {
        assertThrows(ValidationException.class, () ->
                gymOperationService.checkIn(membership1.getId(), LocalDateTime.now(), "L-999"));
        assertThrows(ValidationException.class, () ->
                gymOperationService.checkIn(membership1.getId(), LocalDateTime.now(), "abc"));

        gymOperationService.checkIn(membership1.getId(), LocalDateTime.now(), null);
        assertThrows(ConflictException.class, () ->
                gymOperationService.checkIn(membership1.getId(), LocalDateTime.now(), null));
    }

    @Test
    void testCheckInBlockedOrInvalidMembership() {
        clientService.blockClient(client1.getId());
        assertThrows(BusinessRuleException.class, () ->
                gymOperationService.checkIn(membership1.getId(), LocalDateTime.now(), null));

        clientService.unblockClient(client1.getId());
        membershipService.cancelMembership(membership1.getId());
        assertThrows(BusinessRuleException.class, () ->
                gymOperationService.checkIn(membership1.getId(), LocalDateTime.now(), null));
    }

    @Test
    void testVisitBasedCheckInAndExpiration() {
        Membership vb = membershipService.createMembership(client2.getId(), MembershipType.VISIT_BASED, LocalDate.now(), 30, 1, BigDecimal.valueOf(1000));
        Visit v = gymOperationService.checkIn(vb.getId(), LocalDateTime.now(), null);
        assertEquals(Integer.valueOf(0), membershipService.getMembership(vb.getId()).getRemainingVisits());
        assertEquals(MembershipStatus.EXPIRED, membershipService.getMembership(vb.getId()).getStatus());

        gymOperationService.checkOut(v.getId(), LocalDateTime.now().plusHours(1));
        assertThrows(BusinessRuleException.class, () ->
                gymOperationService.checkIn(vb.getId(), LocalDateTime.now(), null));
    }

    @Test
    void testDaytimeCheckInHours() {
        Membership dt = membershipService.createMembership(client2.getId(), MembershipType.DAYTIME, LocalDate.now(), 30, null, BigDecimal.valueOf(2000));
        LocalDateTime validTime = LocalDate.now().atTime(12, 0);
        Visit v = gymOperationService.checkIn(dt.getId(), validTime, null);
        assertNotNull(v);
        gymOperationService.checkOut(v.getId(), validTime.plusHours(1));

        LocalDateTime invalidTime = LocalDate.now().atTime(21, 0);
        assertThrows(BusinessRuleException.class, () ->
                gymOperationService.checkIn(dt.getId(), invalidTime, null));
    }

    @Test
    void testCheckOutSuccessAndValidation() {
        LocalDateTime entry = LocalDateTime.now();
        Visit v = gymOperationService.checkIn(membership1.getId(), entry, "L-110");
        Visit completed = gymOperationService.checkOut(v.getId(), entry.plusHours(1));

        assertEquals(VisitStatus.COMPLETED, completed.getStatus());
        assertNotNull(completed.getExitTime());

        assertThrows(ConflictException.class, () ->
                gymOperationService.checkOut(v.getId(), entry.plusHours(2)));
    }

    @Test
    void testFreezeAndUnfreeze() {
        LocalDate origEnd = membership1.getEndDate();
        Membership frozen = gymOperationService.freezeMembership(membership1.getId(), 10, LocalDate.now());
        assertEquals(MembershipStatus.FROZEN, frozen.getStatus());

        assertThrows(BusinessRuleException.class, () ->
                gymOperationService.checkIn(membership1.getId(), LocalDateTime.now(), null));

        Membership unfrozen = gymOperationService.unfreezeMembership(membership1.getId(), LocalDate.now().plusDays(4));
        assertEquals(MembershipStatus.ACTIVE, unfrozen.getStatus());
        assertEquals(origEnd.plusDays(4), unfrozen.getEndDate());
    }

    @Test
    void testLockerPoolExhaustion() {
        for (int i = 101; i <= 200; i++) {
            Client c = clientService.createClient("U" + i, "+799" + i + "000", "u" + i + "@t.com", LocalDate.of(1990, 1, 1));
            Membership m = membershipService.createMembership(c.getId(), MembershipType.UNLIMITED, LocalDate.now(), 30, null, BigDecimal.valueOf(1000));
            gymOperationService.checkIn(m.getId(), LocalDateTime.now(), null);
        }

        Client over = clientService.createClient("Over", "+79999999999", "over@t.com", LocalDate.of(1990, 1, 1));
        Membership overM = membershipService.createMembership(over.getId(), MembershipType.UNLIMITED, LocalDate.now(), 30, null, BigDecimal.valueOf(1000));
        assertThrows(BusinessRuleException.class, () ->
                gymOperationService.checkIn(overM.getId(), LocalDateTime.now(), null));
    }
}
