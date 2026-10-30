package app.http.handler;

import app.domain.model.Membership;
import app.domain.model.MembershipStatus;
import app.domain.model.MembershipType;
import app.domain.model.Visit;
import app.domain.service.GymOperationService;
import app.domain.service.MembershipService;
import app.http.dto.CheckInRequest;
import app.http.dto.FreezeRequest;
import app.http.dto.MembershipRequest;
import app.http.dto.MembershipResponse;
import app.http.dto.VisitResponse;
import app.http.util.HttpUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MembershipHandler implements HttpHandler {
    private static final Pattern COLLECTION_PATTERN = Pattern.compile("^/api/memberships/?$");
    private static final Pattern ITEM_PATTERN = Pattern.compile("^/api/memberships/(\\d+)/?$");
    private static final Pattern FREEZE_PATTERN = Pattern.compile("^/api/memberships/(\\d+)/freeze/?$");
    private static final Pattern UNFREEZE_PATTERN = Pattern.compile("^/api/memberships/(\\d+)/unfreeze/?$");
    private static final Pattern CHECK_IN_PATTERN = Pattern.compile("^/api/memberships/(\\d+)/check-in/?$");
    private static final Pattern CANCEL_PATTERN = Pattern.compile("^/api/memberships/(\\d+)/cancel/?$");

    private final MembershipService membershipService;
    private final GymOperationService gymOperationService;

    public MembershipHandler(MembershipService membershipService, GymOperationService gymOperationService) {
        this.membershipService = membershipService;
        this.gymOperationService = gymOperationService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        try {
            Matcher m;
            if ((m = COLLECTION_PATTERN.matcher(path)).matches()) {
                handleCollection(exchange, method);
            } else if ((m = ITEM_PATTERN.matcher(path)).matches()) {
                handleItem(exchange, method, Long.parseLong(m.group(1)));
            } else if ((m = FREEZE_PATTERN.matcher(path)).matches()) {
                handleFreeze(exchange, method, Long.parseLong(m.group(1)));
            } else if ((m = UNFREEZE_PATTERN.matcher(path)).matches()) {
                handleUnfreeze(exchange, method, Long.parseLong(m.group(1)));
            } else if ((m = CHECK_IN_PATTERN.matcher(path)).matches()) {
                handleCheckIn(exchange, method, Long.parseLong(m.group(1)));
            } else if ((m = CANCEL_PATTERN.matcher(path)).matches()) {
                handleCancel(exchange, method, Long.parseLong(m.group(1)));
            } else {
                HttpUtils.sendJson(exchange, 404, Map.of("error", "Not Found", "message", "Endpoint does not exist: " + path));
            }
        } catch (Throwable ex) {
            HttpUtils.handleException(exchange, ex);
        }
    }

    private void handleCollection(HttpExchange exchange, String method) throws IOException {
        switch (method) {
            case "GET" -> {
                Map<String, String> q = HttpUtils.parseQueryParams(exchange.getRequestURI());
                Long clientId = HttpUtils.parseLong(q.get("clientId"));
                String t = q.get("type");
                String s = q.get("status");
                MembershipType type = (t != null && !t.isBlank()) ? MembershipType.valueOf(t.toUpperCase()) : null;
                MembershipStatus status = (s != null && !s.isBlank()) ? MembershipStatus.valueOf(s.toUpperCase()) : null;
                List<MembershipResponse> list = membershipService.listMemberships(clientId, type, status).stream()
                        .map(MembershipResponse::fromDomain).toList();
                HttpUtils.sendJson(exchange, 200, list);
            }
            case "POST" -> {
                MembershipRequest req = HttpUtils.readJson(exchange, MembershipRequest.class);
                int duration = req.durationDays() != null ? req.durationDays() : 30;
                Membership m = membershipService.createMembership(req.clientId(), req.type(), req.startDate(), duration, req.totalVisits(), req.price());
                exchange.getResponseHeaders().set("Location", "/api/memberships/" + m.getId());
                HttpUtils.sendJson(exchange, 201, MembershipResponse.fromDomain(m));
            }
            default -> HttpUtils.sendMethodNotAllowed(exchange, "GET, POST");
        }
    }

    private void handleItem(HttpExchange exchange, String method, Long id) throws IOException {
        switch (method) {
            case "GET" -> HttpUtils.sendJson(exchange, 200, MembershipResponse.fromDomain(membershipService.getMembership(id)));
            case "PUT" -> {
                MembershipRequest req = HttpUtils.readJson(exchange, MembershipRequest.class);
                Membership m = membershipService.updateMembership(id, req.startDate(), req.price());
                HttpUtils.sendJson(exchange, 200, MembershipResponse.fromDomain(m));
            }
            case "DELETE" -> {
                membershipService.deleteMembership(id);
                HttpUtils.sendEmpty(exchange, 204);
            }
            default -> HttpUtils.sendMethodNotAllowed(exchange, "GET, PUT, DELETE");
        }
    }

    private void handleFreeze(HttpExchange exchange, String method, Long id) throws IOException {
        if (!"POST".equalsIgnoreCase(method)) {
            HttpUtils.sendMethodNotAllowed(exchange, "POST");
            return;
        }
        FreezeRequest req = HttpUtils.readJson(exchange, FreezeRequest.class);
        int days = req.days() != null ? req.days() : 7;
        LocalDate date = req.requestDate() != null ? req.requestDate() : LocalDate.now();
        HttpUtils.sendJson(exchange, 200, MembershipResponse.fromDomain(gymOperationService.freezeMembership(id, days, date)));
    }

    private void handleUnfreeze(HttpExchange exchange, String method, Long id) throws IOException {
        if (!"POST".equalsIgnoreCase(method)) {
            HttpUtils.sendMethodNotAllowed(exchange, "POST");
            return;
        }
        byte[] body = HttpUtils.readRequestBody(exchange);
        LocalDate date = LocalDate.now();
        if (body.length > 0) {
            FreezeRequest req = HttpUtils.getMapper().readValue(body, FreezeRequest.class);
            if (req.requestDate() != null) date = req.requestDate();
        }
        HttpUtils.sendJson(exchange, 200, MembershipResponse.fromDomain(gymOperationService.unfreezeMembership(id, date)));
    }

    private void handleCheckIn(HttpExchange exchange, String method, Long id) throws IOException {
        if (!"POST".equalsIgnoreCase(method)) {
            HttpUtils.sendMethodNotAllowed(exchange, "POST");
            return;
        }
        byte[] body = HttpUtils.readRequestBody(exchange);
        LocalDateTime time = LocalDateTime.now();
        String locker = null;
        if (body.length > 0) {
            CheckInRequest req = HttpUtils.getMapper().readValue(body, CheckInRequest.class);
            if (req.visitTime() != null) time = req.visitTime();
            locker = req.preferredLocker();
        }
        Visit visit = gymOperationService.checkIn(id, time, locker);
        exchange.getResponseHeaders().set("Location", "/api/visits/" + visit.getId());
        HttpUtils.sendJson(exchange, 201, VisitResponse.fromDomain(visit));
    }

    private void handleCancel(HttpExchange exchange, String method, Long id) throws IOException {
        if (!"POST".equalsIgnoreCase(method)) {
            HttpUtils.sendMethodNotAllowed(exchange, "POST");
            return;
        }
        HttpUtils.sendJson(exchange, 200, MembershipResponse.fromDomain(membershipService.cancelMembership(id)));
    }
}
