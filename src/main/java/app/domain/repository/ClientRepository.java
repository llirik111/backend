package app.domain.repository;

import app.domain.model.Client;
import app.domain.model.ClientStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {

    Optional<Client> findByPhone(String phone);

    Optional<Client> findByEmail(String email);

    @Query("SELECT c FROM Client c WHERE (:status IS NULL OR c.status = :status) " +
           "AND (:query IS NULL OR LOWER(c.fullName) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(c.email) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(c.phone) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "ORDER BY c.id")
    List<Client> findByFilter(@Param("query") String query, @Param("status") ClientStatus status);

    @Query("SELECT DISTINCT c FROM Client c LEFT JOIN FETCH c.memberships ORDER BY c.id")
    List<Client> findAllWithMemberships();
}
