package app.storage;

import app.domain.model.Membership;
import app.domain.model.MembershipStatus;
import app.domain.model.MembershipType;
import app.domain.repository.MembershipRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryMembershipRepository implements MembershipRepository {
    private final Map<Long, Membership> memberships = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(1);

    @Override
    public Membership save(Membership membership) {
        if (membership.getId() == null) {
            membership.setId(idSequence.getAndIncrement());
        }
        memberships.put(membership.getId(), membership);
        return membership;
    }

    @Override
    public Optional<Membership> findById(Long id) {
        return Optional.ofNullable(memberships.get(id));
    }

    @Override
    public List<Membership> findAll() {
        return new ArrayList<>(memberships.values());
    }

    @Override
    public List<Membership> findByClientId(Long clientId) {
        return memberships.values().stream()
                .filter(m -> m.getClientId().equals(clientId))
                .sorted((a, b) -> a.getId().compareTo(b.getId()))
                .toList();
    }

    @Override
    public List<Membership> findByFilter(Long clientId, MembershipType type, MembershipStatus status) {
        return memberships.values().stream()
                .filter(m -> (clientId == null || m.getClientId().equals(clientId))
                        && (type == null || m.getType() == type)
                        && (status == null || m.getStatus() == status))
                .sorted((a, b) -> a.getId().compareTo(b.getId()))
                .toList();
    }

    @Override
    public boolean deleteById(Long id) {
        return memberships.remove(id) != null;
    }

    @Override
    public boolean existsById(Long id) {
        return memberships.containsKey(id);
    }

    @Override
    public boolean existsActiveByClientId(Long clientId) {
        return memberships.values().stream()
                .filter(m -> m.getClientId().equals(clientId))
                .anyMatch(m -> m.getStatus() == MembershipStatus.ACTIVE || m.getStatus() == MembershipStatus.FROZEN);
    }
}
