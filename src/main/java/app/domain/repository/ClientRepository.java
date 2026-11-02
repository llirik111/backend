package app.domain.repository;

import app.domain.model.Client;
import app.domain.model.ClientStatus;

import java.util.List;
import java.util.Optional;

public interface ClientRepository {
    Client save(Client client);
    Optional<Client> findById(Long id);
    Optional<Client> findByPhone(String phone);
    Optional<Client> findByEmail(String email);
    List<Client> findAll();
    List<Client> findByFilter(String query, ClientStatus status);
    boolean deleteById(Long id);
    boolean existsById(Long id);
}
