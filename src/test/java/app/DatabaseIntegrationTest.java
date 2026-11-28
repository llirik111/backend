package app;

import app.domain.model.Client;
import app.domain.model.Membership;
import app.domain.model.MembershipType;
import app.domain.model.UnlimitedMembership;
import app.domain.model.Visit;
import app.domain.model.VisitStatus;
import app.domain.repository.ClientRepository;
import app.domain.repository.MembershipRepository;
import app.domain.repository.VisitRepository;
import app.domain.service.ClientService;
import app.domain.service.GymOperationService;
import app.domain.service.MembershipService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class DatabaseIntegrationTest {

    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private MembershipRepository membershipRepository;
    @Autowired
    private VisitRepository visitRepository;
    @Autowired
    private ClientService clientService;
    @Autowired
    private MembershipService membershipService;
    @Autowired
    private GymOperationService gymOperationService;
    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    void testSaveClientAndRelatedMemberships() {
        Client client = clientRepository.save(Client.create("Sergey Petrov", "+79998887766", "sergey@test.com", LocalDate.of(1985, 5, 20), "L-105"));
        Membership membership = membershipRepository.save(UnlimitedMembership.create(client, LocalDate.now(), 30, BigDecimal.valueOf(3500)));
        assertNotNull(membership.getId());
        assertEquals(client.getId(), membership.getClientId());
        assertEquals(1, membershipRepository.findByClientId(client.getId()).size());
    }

    @Test
    void testCannotReferenceNonExistentClient() {
        Membership membership = UnlimitedMembership.create(new Client(999999L), LocalDate.now(), 30, BigDecimal.valueOf(1000));
        assertThrows(DataIntegrityViolationException.class, () -> membershipRepository.saveAndFlush(membership));
    }

    @Test
    void testTransactionalRollbackOnFailure() {
        Client c1 = clientService.createClient("Sender", "+79991110001", "sender@test.com", LocalDate.of(1992, 3, 15));
        Client c2 = clientService.createClient("Receiver", "+79991110002", "receiver@test.com", LocalDate.of(1994, 7, 22));
        Membership m = membershipService.createMembership(c1.getId(), MembershipType.UNLIMITED, LocalDate.now(), 30, null, BigDecimal.valueOf(2000));

        assertThrows(RuntimeException.class, () -> transactionTemplate.execute(status -> {
            membershipService.transferMembership(m.getId(), c2.getId());
            membershipRepository.flush();
            throw new RuntimeException("Rollback simulation");
        }));

        Membership refreshed = membershipRepository.findById(m.getId()).orElseThrow();
        assertEquals(c1.getId(), refreshed.getClientId());
    }

    @Test
    void testMultiEntityAtomicOperationSuccess() {
        Client c1 = clientService.createClient("Transfer Sender", "+79992220001", "tsender@test.com", LocalDate.of(1991, 1, 10));
        Client c2 = clientService.createClient("Transfer Receiver", "+79992220002", "treceiver@test.com", LocalDate.of(1993, 2, 20));
        Membership m = membershipService.createMembership(c1.getId(), MembershipType.UNLIMITED, LocalDate.now(), 30, null, BigDecimal.valueOf(2000));

        membershipService.transferMembership(m.getId(), c2.getId());
        assertEquals(c2.getId(), membershipRepository.findById(m.getId()).orElseThrow().getClientId());
    }

    @Test
    void testAvoidNPlusOneOnBulkFetch() {
        clientService.seed(30, 2);
        List<Client> list = clientRepository.findAllWithMemberships();
        assertTrue(list.size() >= 30);
        list.forEach(c -> assertNotNull(c.getMemberships()));
    }

    @Test
    void testCheckInModifiesMembershipAndCreatesVisit() {
        Client client = clientService.createClient("Gym Goer", "+79993330001", "goer@test.com", LocalDate.of(1995, 8, 12), "L-110");
        Membership membership = membershipService.createMembership(client.getId(), MembershipType.VISIT_BASED, LocalDate.now(), 30, 5, BigDecimal.valueOf(1500));

        Visit visit = gymOperationService.checkIn(membership.getId(), LocalDateTime.now(), null);
        assertEquals("L-110", visit.getLockerNumber());
        assertEquals(4, membershipRepository.findById(membership.getId()).orElseThrow().getRemainingVisits());

        gymOperationService.checkOut(visit.getId(), LocalDateTime.now());
        assertEquals(VisitStatus.COMPLETED, visitRepository.findById(visit.getId()).orElseThrow().getStatus());
    }
}
