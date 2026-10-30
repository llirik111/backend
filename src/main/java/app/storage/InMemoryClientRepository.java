package app.storage;

import app.domain.model.Client;
import app.domain.model.ClientStatus;
import app.domain.repository.ClientRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryClientRepository implements ClientRepository {
    private final Map<Long, Client> clients = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(1);

    @Override
    public Client save(Client client) {
        if (client.getId() == null) {
            client.setId(idSequence.getAndIncrement());
        }
        clients.put(client.getId(), client);
        return client;
    }

    @Override
    public Optional<Client> findById(Long id) {
        return Optional.ofNullable(clients.get(id));
    }

    @Override
    public Optional<Client> findByPhone(String phone) {
        if (phone == null) return Optional.empty();
        return clients.values().stream().filter(c -> phone.equalsIgnoreCase(c.getPhone())).findFirst();
    }

    @Override
    public Optional<Client> findByEmail(String email) {
        if (email == null) return Optional.empty();
        return clients.values().stream().filter(c -> email.equalsIgnoreCase(c.getEmail())).findFirst();
    }

    @Override
    public List<Client> findAll() {
        return new ArrayList<>(clients.values());
    }

    @Override
    public List<Client> findByFilter(String query, ClientStatus status) {
        String queryLower = query != null ? query.trim().toLowerCase(Locale.ROOT) : null;
        return clients.values().stream()
                .filter(c -> {
                    if (status != null && c.getStatus() != status) return false;
                    if (queryLower != null && !queryLower.isEmpty()) {
                        return c.getFullName().toLowerCase(Locale.ROOT).contains(queryLower)
                                || c.getPhone().toLowerCase(Locale.ROOT).contains(queryLower)
                                || c.getEmail().toLowerCase(Locale.ROOT).contains(queryLower);
                    }
                    return true;
                })
                .sorted((a, b) -> a.getId().compareTo(b.getId()))
                .toList();
    }

    @Override
    public boolean deleteById(Long id) {
        return clients.remove(id) != null;
    }

    @Override
    public boolean existsById(Long id) {
        return clients.containsKey(id);
    }
}
