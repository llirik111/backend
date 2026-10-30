package app.domain.service;

import app.domain.exception.ConflictException;
import app.domain.exception.NotFoundException;
import app.domain.model.Client;
import app.domain.model.ClientStatus;
import app.domain.repository.ClientRepository;
import app.domain.repository.MembershipRepository;
import app.domain.repository.VisitRepository;

import java.time.LocalDate;
import java.util.List;

public class ClientService {
    private final ClientRepository clientRepository;
    private final MembershipRepository membershipRepository;
    private final VisitRepository visitRepository;

    public ClientService(ClientRepository clientRepository, MembershipRepository membershipRepository, VisitRepository visitRepository) {
        this.clientRepository = clientRepository;
        this.membershipRepository = membershipRepository;
        this.visitRepository = visitRepository;
    }

    public Client createClient(String fullName, String phone, String email, LocalDate birthDate) {
        if (clientRepository.findByPhone(phone).isPresent()) {
            throw new ConflictException("Client with phone " + phone + " already exists");
        }
        if (clientRepository.findByEmail(email).isPresent()) {
            throw new ConflictException("Client with email " + email + " already exists");
        }
        return clientRepository.save(Client.create(fullName, phone, email, birthDate));
    }

    public Client getClient(Long id) {
        if (id == null) throw new NotFoundException("Client id must not be null");
        return clientRepository.findById(id).orElseThrow(() -> new NotFoundException("Client not found with id: " + id));
    }

    public Client updateClient(Long id, String fullName, String phone, String email, LocalDate birthDate) {
        Client client = getClient(id);
        clientRepository.findByPhone(phone).ifPresent(e -> {
            if (!e.getId().equals(id)) throw new ConflictException("Phone " + phone + " is already in use");
        });
        clientRepository.findByEmail(email).ifPresent(e -> {
            if (!e.getId().equals(id)) throw new ConflictException("Email " + email + " is already in use");
        });
        client.update(fullName, phone, email, birthDate);
        return clientRepository.save(client);
    }

    public void deleteClient(Long id) {
        if (!clientRepository.existsById(id)) throw new NotFoundException("Client not found with id: " + id);
        if (visitRepository.existsActiveByClientId(id)) throw new ConflictException("Cannot delete client while they are inside the gym");
        if (membershipRepository.existsActiveByClientId(id)) throw new ConflictException("Cannot delete client with active memberships");
        clientRepository.deleteById(id);
    }

    public List<Client> listClients(String query, ClientStatus status) {
        return clientRepository.findByFilter(query, status);
    }

    public Client blockClient(Long id) {
        Client client = getClient(id);
        client.block();
        return clientRepository.save(client);
    }

    public Client unblockClient(Long id) {
        Client client = getClient(id);
        client.unblock();
        return clientRepository.save(client);
    }
}
