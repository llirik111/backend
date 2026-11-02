package app;

import app.http.util.HttpUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.Map;

public class PingHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpUtils.sendMethodNotAllowed(exchange, "GET");
            return;
        }
        HttpUtils.sendJson(exchange, 200, Map.of("message", "pong"));
    }
}
