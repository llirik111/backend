package app.storage;

import app.domain.model.Visit;
import app.domain.model.VisitStatus;
import app.domain.repository.VisitRepository;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryVisitRepository {
    private final Map<Long, Visit> visits = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(1);

    public Visit save(Visit visit) {
        if (visit.getId() == null) {
            visit.setId(idSequence.getAndIncrement());
        }
        visits.put(visit.getId(), visit);
        return visit;
    }

    public Optional<Visit> findById(Long id) {
        return Optional.ofNullable(visits.get(id));
    }

    public List<Visit> findAll() {
        return new ArrayList<>(visits.values());
    }

    public List<Visit> findByClientId(Long clientId) {
        if (clientId == null) return List.of();
        return visits.values().stream().filter(v -> clientId.equals(v.getClientId())).toList();
    }

    public List<Visit> findByFilter(Long clientId, VisitStatus status) {
        return visits.values().stream()
                .filter(v -> {
                    if (clientId != null && !clientId.equals(v.getClientId())) return false;
                    if (status != null && v.getStatus() != status) return false;
                    return true;
                })
                .sorted((a, b) -> a.getId().compareTo(b.getId()))
                .toList();
    }

    public Optional<Visit> findActiveByClientId(Long clientId) {
        if (clientId == null) return Optional.empty();
        return visits.values().stream()
                .filter(v -> clientId.equals(v.getClientId()) && v.getStatus() == VisitStatus.IN_PROGRESS)
                .findFirst();
    }

    public boolean existsActiveByClientId(Long clientId) {
        if (clientId == null) return false;
        return visits.values().stream()
                .anyMatch(v -> clientId.equals(v.getClientId()) && v.getStatus() == VisitStatus.IN_PROGRESS);
    }

    public boolean existsActiveByMembershipId(Long membershipId) {
        if (membershipId == null) return false;
        return visits.values().stream()
                .anyMatch(v -> membershipId.equals(v.getMembershipId()) && v.getStatus() == VisitStatus.IN_PROGRESS);
    }

    public boolean existsActiveWithLocker(String lockerNumber) {
        if (lockerNumber == null) return false;
        return visits.values().stream()
                .anyMatch(v -> lockerNumber.equalsIgnoreCase(v.getLockerNumber()) && v.getStatus() == VisitStatus.IN_PROGRESS);
    }

    public static VisitRepository create() {
        InMemoryVisitRepository target = new InMemoryVisitRepository();
        return (VisitRepository) Proxy.newProxyInstance(
                VisitRepository.class.getClassLoader(),
                new Class<?>[]{VisitRepository.class},
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
