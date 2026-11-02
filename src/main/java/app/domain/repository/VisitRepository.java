package app.domain.repository;

import app.domain.model.Visit;
import app.domain.model.VisitStatus;

import java.util.List;
import java.util.Optional;

public interface VisitRepository {
    Visit save(Visit visit);
    Optional<Visit> findById(Long id);
    List<Visit> findAll();
    List<Visit> findByClientId(Long clientId);
    List<Visit> findByFilter(Long clientId, VisitStatus status);
    Optional<Visit> findActiveByClientId(Long clientId);
    boolean existsActiveByClientId(Long clientId);
    boolean existsActiveByMembershipId(Long membershipId);
    boolean existsActiveWithLocker(String lockerNumber);
}
