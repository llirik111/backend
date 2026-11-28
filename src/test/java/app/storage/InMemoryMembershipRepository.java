package app.storage;

import app.domain.model.Membership;
import app.domain.model.MembershipStatus;
import app.domain.model.MembershipType;
import app.domain.repository.MembershipRepository;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryMembershipRepository {
    private final Map<Long, Membership> memberships = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(1);

    public Membership save(Membership membership) {
        if (membership.getId() == null) {
            membership.setId(idSequence.getAndIncrement());
        }
        memberships.put(membership.getId(), membership);
        return membership;
    }

    public Optional<Membership> findById(Long id) {
        return Optional.ofNullable(memberships.get(id));
    }

    public List<Membership> findAll() {
        return new ArrayList<>(memberships.values());
    }

    public List<Membership> findByClientId(Long clientId) {
        if (clientId == null) return List.of();
        return memberships.values().stream().filter(m -> clientId.equals(m.getClientId())).toList();
    }

    public List<Membership> findByFilter(Long clientId, MembershipType type, MembershipStatus status) {
        return memberships.values().stream()
                .filter(m -> {
                    if (clientId != null && !clientId.equals(m.getClientId())) return false;
                    if (type != null && m.getType() != type) return false;
                    if (status != null && m.getStatus() != status) return false;
                    return true;
                })
                .sorted((a, b) -> a.getId().compareTo(b.getId()))
                .toList();
    }

    public void deleteById(Long id) {
        memberships.remove(id);
    }

    public boolean existsById(Long id) {
        return memberships.containsKey(id);
    }

    public boolean existsActiveByClientId(Long clientId) {
        if (clientId == null) return false;
        return memberships.values().stream()
                .anyMatch(m -> clientId.equals(m.getClientId()) && m.getStatus() == MembershipStatus.ACTIVE);
    }

    public static MembershipRepository create() {
        InMemoryMembershipRepository target = new InMemoryMembershipRepository();
        return (MembershipRepository) Proxy.newProxyInstance(
                MembershipRepository.class.getClassLoader(),
                new Class<?>[]{MembershipRepository.class},
                (proxy, method, args) -> {
                    for (Method m : target.getClass().getMethods()) {
                        if (m.getName().equals(method.getName()) && m.getParameterCount() == method.getParameterCount()) {
                            return m.invoke(target, args);
                        }
                    }
                    return null;
                }
        );
    }
}
