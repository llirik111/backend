package app.http.dto;

import app.domain.model.MembershipType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MembershipRequest(
        Long clientId,
        MembershipType type,
        LocalDate startDate,
        Integer durationDays,
        Integer totalVisits,
        BigDecimal price
) {}
