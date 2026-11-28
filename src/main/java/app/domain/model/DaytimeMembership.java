package app.domain.model;

import app.domain.exception.BusinessRuleException;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@DiscriminatorValue("DAYTIME")
public class DaytimeMembership extends UnlimitedMembership {
    private static final LocalTime START_TIME = LocalTime.of(7, 0);
    private static final LocalTime END_TIME = LocalTime.of(17, 0);

    public DaytimeMembership() {
    }

    public DaytimeMembership(Long id) {
        super(id);
    }

    public DaytimeMembership(Long id, Client client, MembershipStatus status,
                             LocalDate startDate, LocalDate endDate, int frozenDays,
                             LocalDate freezeStartDate, BigDecimal price, Instant createdAt) {
        super(id, client, status, startDate, endDate, frozenDays, freezeStartDate, price, createdAt);
        this.type = MembershipType.DAYTIME;
    }

    public DaytimeMembership(Long id, Long clientId, MembershipStatus status,
                             LocalDate startDate, LocalDate endDate, int frozenDays,
                             LocalDate freezeStartDate, BigDecimal price, Instant createdAt) {
        this(id, new Client(clientId), status, startDate, endDate, frozenDays, freezeStartDate, price, createdAt);
    }

    public static DaytimeMembership create(Client client, LocalDate startDate, int durationDays, BigDecimal price) {
        UnlimitedMembership u = UnlimitedMembership.create(client, startDate, durationDays, price);
        return new DaytimeMembership(null, client, MembershipStatus.ACTIVE, u.getStartDate(), u.getEndDate(), 0, null, price, Instant.now());
    }

    public static DaytimeMembership create(Long clientId, LocalDate startDate, int durationDays, BigDecimal price) {
        return create(new Client(clientId), startDate, durationDays, price);
    }

    @Override
    public void validateCheckIn(LocalDateTime visitTime) {
        super.validateCheckIn(visitTime);
        LocalTime time = visitTime.toLocalTime();
        if (time.isBefore(START_TIME) || time.isAfter(END_TIME)) {
            throw new BusinessRuleException("Daytime membership is only valid between " + START_TIME + " and " + END_TIME);
        }
    }
}
