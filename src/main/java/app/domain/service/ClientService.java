package app.domain.service;

import app.domain.DomainLock;
import app.domain.exception.ConflictException;
import app.domain.exception.NotFoundException;
import app.domain.model.Client;
import app.domain.model.ClientStatus;
import app.domain.model.Membership;
import app.domain.model.UnlimitedMembership;
import app.domain.model.VisitBasedMembership;
import app.domain.repository.ClientRepository;
import app.domain.repository.MembershipRepository;
import app.domain.repository.VisitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class ClientService {
    private final ClientRepository clientRepository;
    private final MembershipRepository membershipRepository;
    private final VisitRepository visitRepository;

    public ClientService(ClientRepository clientRepository, MembershipRepository membershipRepository, VisitRepository visitRepository) {
        this.clientRepository = clientRepository;
        this.membershipRepository = membershipRepository;
        this.visitRepository = visitRepository;
    }

    @Transactional
    public Client createClient(String fullName, String phone, String email, LocalDate birthDate) {
        return createClient(fullName, phone, email, birthDate, null);
    }

    @Transactional
    public Client createClient(String fullName, String phone, String email, LocalDate birthDate, String preferredLocker) {
        synchronized (DomainLock.INSTANCE) {
            if (clientRepository.findByPhone(phone).isPresent()) {
                throw new ConflictException("Client with phone " + phone + " already exists");
            }
            if (clientRepository.findByEmail(email).isPresent()) {
                throw new ConflictException("Client with email " + email + " already exists");
            }
            return clientRepository.save(Client.create(fullName, phone, email, birthDate, preferredLocker));
        }
    }

    @Transactional(readOnly = true)
    public Client getClient(Long id) {
        if (id == null) throw new NotFoundException("Client id must not be null");
        return clientRepository.findById(id).orElseThrow(() -> new NotFoundException("Client not found with id: " + id));
    }

    @Transactional
    public Client updateClient(Long id, String fullName, String phone, String email, LocalDate birthDate) {
        return updateClient(id, fullName, phone, email, birthDate, null);
    }

    @Transactional
    public Client updateClient(Long id, String fullName, String phone, String email, LocalDate birthDate, String preferredLocker) {
        synchronized (DomainLock.INSTANCE) {
            Client client = getClient(id);
            clientRepository.findByPhone(phone).ifPresent(e -> {
                if (!e.getId().equals(id)) throw new ConflictException("Phone " + phone + " is already in use");
            });
            clientRepository.findByEmail(email).ifPresent(e -> {
                if (!e.getId().equals(id)) throw new ConflictException("Email " + email + " is already in use");
            });
            client.update(fullName, phone, email, birthDate, preferredLocker);
            return clientRepository.save(client);
        }
    }

    @Transactional
    public void deleteClient(Long id) {
        synchronized (DomainLock.INSTANCE) {
            if (!clientRepository.existsById(id)) throw new NotFoundException("Client not found with id: " + id);
            if (visitRepository.existsActiveByClientId(id)) throw new ConflictException("Cannot delete client while they are inside the gym");
            if (membershipRepository.existsActiveByClientId(id)) throw new ConflictException("Cannot delete client with active memberships");
            clientRepository.deleteById(id);
        }
    }

    @Transactional(readOnly = true)
    public List<Client> listClients(String query, ClientStatus status) {
        return clientRepository.findByFilter(query, status);
    }

    @Transactional(readOnly = true)
    public List<Client> listClientsWithMemberships() {
        return clientRepository.findAllWithMemberships();
    }

    @Transactional
    public Client blockClient(Long id) {
        synchronized (DomainLock.INSTANCE) {
            Client client = getClient(id);
            client.block();
            return clientRepository.save(client);
        }
    }

    @Transactional
    public Client unblockClient(Long id) {
        synchronized (DomainLock.INSTANCE) {
            Client client = getClient(id);
            client.unblock();
            return clientRepository.save(client);
        }
    }

    @Transactional
    public int seed(int clientsCount, int membershipsPerClient) {
        int totalCreated = 0;
        long timestamp = System.currentTimeMillis() % 10000000L;
        for (int i = 1; i <= clientsCount; i++) {
            String phone = "+7999" + String.format("%07d", (timestamp + i) % 10000000L);
            String email = "client_" + timestamp + "_" + i + "@example.com";
            Client client = Client.create("Client " + i, phone, email, LocalDate.of(1990, 1, 1), "L-" + (101 + (i % 100)));
            client = clientRepository.save(client);
            for (int j = 1; j <= membershipsPerClient; j++) {
                Membership m = (j % 2 == 1)
                        ? UnlimitedMembership.create(client, LocalDate.now().minusDays(5), 30, BigDecimal.valueOf(2500))
                        : VisitBasedMembership.create(client, LocalDate.now().minusDays(2), 30, 10, BigDecimal.valueOf(3000));
                membershipRepository.save(m);
            }
            totalCreated++;
        }
        return totalCreated;
    }
}
