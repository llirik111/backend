package app.http.handler;

import app.domain.model.Visit;
import app.domain.model.VisitStatus;
import app.domain.service.GymOperationService;
import app.http.dto.CheckOutRequest;
import app.http.dto.VisitResponse;
import app.http.util.HttpUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VisitHandler implements HttpHandler {
    private static final Pattern COLLECTION_PATTERN = Pattern.compile("^/api/visits/?$");
    private static final Pattern ITEM_PATTERN = Pattern.compile("^/api/visits/(\\d+)/?$");
    private static final Pattern CHECK_OUT_PATTERN = Pattern.compile("^/api/visits/(\\d+)/check-out/?$");

    private final GymOperationService gymOperationService;

    public VisitHandler(GymOperationService gymOperationService) {
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
            } else if ((m = CHECK_OUT_PATTERN.matcher(path)).matches()) {
                handleCheckOut(exchange, method, Long.parseLong(m.group(1)));
            } else {
                HttpUtils.sendJson(exchange, 404, Map.of("error", "Not Found", "message", "Endpoint does not exist: " + path));
            }
        } catch (Throwable ex) {
            HttpUtils.handleException(exchange, ex);
        }
    }

    private void handleCollection(HttpExchange exchange, String method) throws IOException {
        if (!"GET".equalsIgnoreCase(method)) {
            HttpUtils.sendMethodNotAllowed(exchange, "GET");
            return;
        }
        Map<String, String> query = HttpUtils.parseQueryParams(exchange.getRequestURI());
        Long clientId = HttpUtils.parseLong(query.get("clientId"));
        String s = query.get("status");
        VisitStatus status = (s != null && !s.isBlank()) ? VisitStatus.valueOf(s.toUpperCase()) : null;
        List<VisitResponse> list = gymOperationService.listVisits(clientId, status).stream().map(VisitResponse::fromDomain).toList();
        HttpUtils.sendJson(exchange, 200, list);
    }

    private void handleItem(HttpExchange exchange, String method, Long id) throws IOException {
        if (!"GET".equalsIgnoreCase(method)) {
            HttpUtils.sendMethodNotAllowed(exchange, "GET");
            return;
        }
        HttpUtils.sendJson(exchange, 200, VisitResponse.fromDomain(gymOperationService.getVisit(id)));
    }

    private void handleCheckOut(HttpExchange exchange, String method, Long id) throws IOException {
        if (!"POST".equalsIgnoreCase(method)) {
            HttpUtils.sendMethodNotAllowed(exchange, "POST");
            return;
        }
        byte[] body = HttpUtils.readRequestBody(exchange);
        LocalDateTime exitTime = LocalDateTime.now();
        if (body.length > 0) {
            CheckOutRequest req = HttpUtils.getMapper().readValue(body, CheckOutRequest.class);
            if (req.exitTime() != null) exitTime = req.exitTime();
        }
        Visit visit = gymOperationService.checkOut(id, exitTime);
        HttpUtils.sendJson(exchange, 200, VisitResponse.fromDomain(visit));
    }
}
