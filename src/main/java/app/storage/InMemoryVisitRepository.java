package app.storage;

import app.domain.model.Visit;
import app.domain.model.VisitStatus;
import app.domain.repository.VisitRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryVisitRepository implements VisitRepository {
    private final Map<Long, Visit> visits = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(1);

    @Override
    public Visit save(Visit visit) {
        if (visit.getId() == null) {
            visit.setId(idSequence.getAndIncrement());
        }
        visits.put(visit.getId(), visit);
        return visit;
    }

    @Override
    public Optional<Visit> findById(Long id) {
        return Optional.ofNullable(visits.get(id));
    }

    @Override
    public List<Visit> findAll() {
        return new ArrayList<>(visits.values());
    }

    @Override
    public List<Visit> findByClientId(Long clientId) {
        return visits.values().stream()
                .filter(v -> v.getClientId().equals(clientId))
                .sorted((a, b) -> a.getId().compareTo(b.getId()))
                .toList();
    }

    @Override
    public List<Visit> findByFilter(Long clientId, VisitStatus status) {
        return visits.values().stream()
                .filter(v -> (clientId == null || v.getClientId().equals(clientId))
                        && (status == null || v.getStatus() == status))
                .sorted((a, b) -> a.getId().compareTo(b.getId()))
                .toList();
    }

    @Override
    public Optional<Visit> findActiveByClientId(Long clientId) {
        return visits.values().stream()
                .filter(v -> v.getClientId().equals(clientId) && v.getStatus() == VisitStatus.IN_PROGRESS)
                .findFirst();
    }

    @Override
    public boolean existsActiveByClientId(Long clientId) {
        return visits.values().stream()
                .anyMatch(v -> v.getClientId().equals(clientId) && v.getStatus() == VisitStatus.IN_PROGRESS);
    }

    @Override
    public boolean existsActiveByMembershipId(Long membershipId) {
        return visits.values().stream()
                .anyMatch(v -> v.getMembershipId().equals(membershipId) && v.getStatus() == VisitStatus.IN_PROGRESS);
    }

    @Override
    public boolean existsActiveWithLocker(String lockerNumber) {
        if (lockerNumber == null) return false;
        return visits.values().stream()
                .anyMatch(v -> v.getStatus() == VisitStatus.IN_PROGRESS && lockerNumber.equalsIgnoreCase(v.getLockerNumber()));
    }
}
