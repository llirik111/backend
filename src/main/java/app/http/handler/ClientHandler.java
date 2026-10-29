package app.http.handler;

import app.domain.model.Client;
import app.domain.model.ClientStatus;
import app.domain.service.ClientService;
import app.http.dto.ClientRequest;
import app.http.dto.ClientResponse;
import app.http.util.HttpUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientHandler implements HttpHandler {
    private static final Pattern COLLECTION_PATTERN = Pattern.compile("^/api/clients/?$");
    private static final Pattern ITEM_PATTERN = Pattern.compile("^/api/clients/(\\d+)/?$");
    private static final Pattern BLOCK_PATTERN = Pattern.compile("^/api/clients/(\\d+)/block/?$");
    private static final Pattern UNBLOCK_PATTERN = Pattern.compile("^/api/clients/(\\d+)/unblock/?$");

    private final ClientService clientService;

    public ClientHandler(ClientService clientService) {
        this.clientService = clientService;
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
            } else if ((m = BLOCK_PATTERN.matcher(path)).matches()) {
                handleBlock(exchange, method, Long.parseLong(m.group(1)));
            } else if ((m = UNBLOCK_PATTERN.matcher(path)).matches()) {
                handleUnblock(exchange, method, Long.parseLong(m.group(1)));
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
                Map<String, String> query = HttpUtils.parseQueryParams(exchange.getRequestURI());
                String q = query.get("query");
                String s = query.get("status");
                ClientStatus status = (s != null && !s.isBlank()) ? ClientStatus.valueOf(s.toUpperCase()) : null;
                List<ClientResponse> list = clientService.listClients(q, status).stream().map(ClientResponse::fromDomain).toList();
                HttpUtils.sendJson(exchange, 200, list);
            }
            case "POST" -> {
                ClientRequest req = HttpUtils.readJson(exchange, ClientRequest.class);
                Client client = clientService.createClient(req.fullName(), req.phone(), req.email(), req.birthDate());
                exchange.getResponseHeaders().set("Location", "/api/clients/" + client.getId());
                HttpUtils.sendJson(exchange, 201, ClientResponse.fromDomain(client));
            }
            default -> HttpUtils.sendMethodNotAllowed(exchange, "GET, POST");
        }
    }

    private void handleItem(HttpExchange exchange, String method, Long id) throws IOException {
        switch (method) {
            case "GET" -> HttpUtils.sendJson(exchange, 200, ClientResponse.fromDomain(clientService.getClient(id)));
            case "PUT" -> {
                ClientRequest req = HttpUtils.readJson(exchange, ClientRequest.class);
                Client client = clientService.updateClient(id, req.fullName(), req.phone(), req.email(), req.birthDate());
                HttpUtils.sendJson(exchange, 200, ClientResponse.fromDomain(client));
            }
            case "DELETE" -> {
                clientService.deleteClient(id);
                HttpUtils.sendEmpty(exchange, 204);
            }
            default -> HttpUtils.sendMethodNotAllowed(exchange, "GET, PUT, DELETE");
        }
    }

    private void handleBlock(HttpExchange exchange, String method, Long id) throws IOException {
        if (!"POST".equalsIgnoreCase(method)) {
            HttpUtils.sendMethodNotAllowed(exchange, "POST");
            return;
        }
        HttpUtils.sendJson(exchange, 200, ClientResponse.fromDomain(clientService.blockClient(id)));
    }

    private void handleUnblock(HttpExchange exchange, String method, Long id) throws IOException {
        if (!"POST".equalsIgnoreCase(method)) {
            HttpUtils.sendMethodNotAllowed(exchange, "POST");
            return;
        }
        HttpUtils.sendJson(exchange, 200, ClientResponse.fromDomain(clientService.unblockClient(id)));
    }
}
