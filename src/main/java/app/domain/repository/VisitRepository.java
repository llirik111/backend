package app.domain.repository;

import app.domain.model.Visit;
import app.domain.model.VisitStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VisitRepository extends JpaRepository<Visit, Long> {

    @Query("SELECT v FROM Visit v WHERE v.client.id = :clientId ORDER BY v.id")
    List<Visit> findByClientId(@Param("clientId") Long clientId);

    @Query("SELECT v FROM Visit v WHERE (:clientId IS NULL OR v.client.id = :clientId) " +
           "AND (:status IS NULL OR v.status = :status) " +
           "ORDER BY v.id")
    List<Visit> findByFilter(@Param("clientId") Long clientId, @Param("status") VisitStatus status);

    @Query("SELECT v FROM Visit v WHERE v.client.id = :clientId AND v.status = 'IN_PROGRESS'")
    Optional<Visit> findActiveByClientId(@Param("clientId") Long clientId);

    @Query("SELECT (COUNT(v) > 0) FROM Visit v WHERE v.client.id = :clientId AND v.status = 'IN_PROGRESS'")
    boolean existsActiveByClientId(@Param("clientId") Long clientId);

    @Query("SELECT (COUNT(v) > 0) FROM Visit v WHERE v.membership.id = :membershipId AND v.status = 'IN_PROGRESS'")
    boolean existsActiveByMembershipId(@Param("membershipId") Long membershipId);

    @Query("SELECT (COUNT(v) > 0) FROM Visit v WHERE v.lockerNumber = :lockerNumber AND v.status = 'IN_PROGRESS'")
    boolean existsActiveWithLocker(@Param("lockerNumber") String lockerNumber);
}
