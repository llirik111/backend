package app.domain.service;

import app.domain.exception.ConflictException;
import app.domain.exception.NotFoundException;
import app.domain.exception.ValidationException;
import app.domain.model.Client;
import app.domain.model.ClientStatus;
import app.domain.model.Membership;
import app.domain.model.UnlimitedMembership;
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

public class ClientServiceTest {

    private ClientRepository clientRepository;
    private MembershipRepository membershipRepository;
    private VisitRepository visitRepository;
    private ClientService clientService;

    @BeforeEach
    void setUp() {
        clientRepository = new InMemoryClientRepository();
        membershipRepository = new InMemoryMembershipRepository();
        visitRepository = new InMemoryVisitRepository();
        clientService = new ClientService(clientRepository, membershipRepository, visitRepository);
    }

    @Test
    void testCreateClientSuccessAndDuplicates() {
        Client c = clientService.createClient("Ivan Ivanov", "+79991112233", "ivan@test.com", LocalDate.of(1990, 1, 1));
        assertNotNull(c.getId());
        assertEquals(ClientStatus.ACTIVE, c.getStatus());

        assertThrows(ConflictException.class, () ->
                clientService.createClient("Other", "+79991112233", "other@test.com", LocalDate.of(1990, 1, 1)));
        assertThrows(ConflictException.class, () ->
                clientService.createClient("Other", "+79990001122", "ivan@test.com", LocalDate.of(1990, 1, 1)));
    }

    @Test
    void testCreateClientValidation() {
        assertThrows(ValidationException.class, () ->
                clientService.createClient(" ", "+79991112233", "a@b.com", LocalDate.of(1990, 1, 1)));
        assertThrows(ValidationException.class, () ->
                clientService.createClient("Name", "+79991112233", "not-email", LocalDate.of(1990, 1, 1)));
        assertThrows(ValidationException.class, () ->
                clientService.createClient("Name", "+79991112233", "a@b.com", LocalDate.now().plusDays(1)));
    }

    @Test
    void testUpdateClientSuccessAndAtomicRollback() {
        Client c = clientService.createClient("Ivan Ivanov", "+79991112233", "ivan@test.com", LocalDate.of(1990, 1, 1));
        Client updated = clientService.updateClient(c.getId(), "Ivan Petrov", "+79991112233", "petrov@test.com", LocalDate.of(1990, 1, 1));
        assertEquals("Ivan Petrov", updated.getFullName());

        assertThrows(ValidationException.class, () ->
                clientService.updateClient(c.getId(), "New Name", "+79991112233", "invalid-email", LocalDate.of(1990, 1, 1)));
        Client unchanged = clientService.getClient(c.getId());
        assertEquals("Ivan Petrov", unchanged.getFullName());
    }

    @Test
    void testBlockAndUnblockTransitions() {
        Client c = clientService.createClient("Ivan Ivanov", "+79991112233", "ivan@test.com", LocalDate.of(1990, 1, 1));
        clientService.blockClient(c.getId());
        assertEquals(ClientStatus.BLOCKED, clientService.getClient(c.getId()).getStatus());

        clientService.unblockClient(c.getId());
        assertEquals(ClientStatus.ACTIVE, clientService.getClient(c.getId()).getStatus());
    }

    @Test
    void testDeleteClientSuccessAndConflicts() {
        Client c = clientService.createClient("Ivan Ivanov", "+79991112233", "ivan@test.com", LocalDate.of(1990, 1, 1));

        Visit visit = visitRepository.save(Visit.start(c.getId(), 1L, LocalDateTime.now(), "L-101"));
        assertThrows(ConflictException.class, () -> clientService.deleteClient(c.getId()));

        visit.complete(LocalDateTime.now());
        Membership m = membershipRepository.save(UnlimitedMembership.create(c.getId(), LocalDate.now(), 30, BigDecimal.valueOf(1000)));
        assertThrows(ConflictException.class, () -> clientService.deleteClient(c.getId()));

        membershipRepository.deleteById(m.getId());
        clientService.deleteClient(c.getId());
        assertThrows(NotFoundException.class, () -> clientService.getClient(c.getId()));
    }

    @Test
    void testGetAndListFiltering() {
        Client c1 = clientService.createClient("Ivan Ivanov", "+79991112233", "ivan@test.com", LocalDate.of(1990, 1, 1));
        Client c2 = clientService.createClient("Anna Petrova", "+79994445566", "anna@test.com", LocalDate.of(1995, 5, 5));
        clientService.blockClient(c2.getId());

        assertEquals(c1.getId(), clientService.getClient(c1.getId()).getId());
        assertThrows(NotFoundException.class, () -> clientService.getClient(999L));

        List<Client> queryResult = clientService.listClients("petrova", null);
        assertEquals(1, queryResult.size());
        assertEquals("Anna Petrova", queryResult.get(0).getFullName());

        List<Client> activeOnly = clientService.listClients(null, ClientStatus.ACTIVE);
        assertEquals(1, activeOnly.size());
        assertEquals("Ivan Ivanov", activeOnly.get(0).getFullName());
    }
}
