package app.domain.repository;

import app.domain.model.Membership;
import app.domain.model.MembershipStatus;
import app.domain.model.MembershipType;

import java.util.List;
import java.util.Optional;

public interface MembershipRepository {
    Membership save(Membership membership);
    Optional<Membership> findById(Long id);
    List<Membership> findAll();
    List<Membership> findByClientId(Long clientId);
    List<Membership> findByFilter(Long clientId, MembershipType type, MembershipStatus status);
    boolean deleteById(Long id);
    boolean existsById(Long id);
    boolean existsActiveByClientId(Long clientId);
}
