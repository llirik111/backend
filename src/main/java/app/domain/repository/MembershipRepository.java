package app.domain.repository;

import app.domain.model.Membership;
import app.domain.model.MembershipStatus;
import app.domain.model.MembershipType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MembershipRepository extends JpaRepository<Membership, Long> {

    @Query("SELECT m FROM Membership m WHERE m.client.id = :clientId ORDER BY m.id")
    List<Membership> findByClientId(@Param("clientId") Long clientId);

    @Query("SELECT m FROM Membership m WHERE (:clientId IS NULL OR m.client.id = :clientId) " +
           "AND (:type IS NULL OR m.type = :type) " +
           "AND (:status IS NULL OR m.status = :status) " +
           "ORDER BY m.id")
    List<Membership> findByFilter(@Param("clientId") Long clientId, @Param("type") MembershipType type, @Param("status") MembershipStatus status);

    @Query("SELECT (COUNT(m) > 0) FROM Membership m WHERE m.client.id = :clientId AND m.status = 'ACTIVE'")
    boolean existsActiveByClientId(@Param("clientId") Long clientId);
}
