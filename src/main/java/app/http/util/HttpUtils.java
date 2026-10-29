package app.http.util;

import app.domain.exception.BusinessRuleException;
import app.domain.exception.ConflictException;
import app.domain.exception.NotFoundException;
import app.domain.exception.ValidationException;
import app.http.dto.ErrorResponse;
import com.sun.net.httpserver.HttpExchange;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

public final class HttpUtils {
    private static final ObjectMapper MAPPER = JsonMapper.builder().build();

    private HttpUtils() {}

    public static ObjectMapper getMapper() {
        return MAPPER;
    }

    public static <T> T readJson(HttpExchange exchange, Class<T> clazz) throws IOException {
        byte[] bytes = readRequestBody(exchange);
        if (bytes.length == 0) {
            throw new ValidationException("Request body must not be empty");
        }
        try {
            return MAPPER.readValue(bytes, clazz);
        } catch (JacksonException ex) {
            throw new ValidationException("Malformed JSON request body: " + ex.getOriginalMessage());
        }
    }

    public static byte[] readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            return is.readAllBytes();
        }
    }

    public static void sendJson(HttpExchange exchange, int statusCode, Object data) throws IOException {
        byte[] body = MAPPER.writeValueAsBytes(data);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body);
        }
    }

    public static void sendEmpty(HttpExchange exchange, int statusCode) throws IOException {
        exchange.sendResponseHeaders(statusCode, -1);
        exchange.close();
    }

    public static void sendMethodNotAllowed(HttpExchange exchange, String allowedMethods) throws IOException {
        exchange.getResponseHeaders().set("Allow", allowedMethods);
        sendJson(exchange, 405, ErrorResponse.of(405, "Method Not Allowed", "Allowed methods: " + allowedMethods));
    }

    public static void handleException(HttpExchange exchange, Throwable ex) {
        try {
            if (ex instanceof NotFoundException) {
                sendJson(exchange, 404, ErrorResponse.of(404, "Not Found", ex.getMessage()));
            } else if (ex instanceof ValidationException || ex instanceof IllegalArgumentException
                    || ex instanceof NumberFormatException || ex instanceof DateTimeParseException) {
                sendJson(exchange, 400, ErrorResponse.of(400, "Bad Request", ex.getMessage()));
            } else if (ex instanceof ConflictException) {
                sendJson(exchange, 409, ErrorResponse.of(409, "Conflict", ex.getMessage()));
            } else if (ex instanceof BusinessRuleException) {
                sendJson(exchange, 422, ErrorResponse.of(422, "Unprocessable Entity", ex.getMessage()));
            } else {
                sendJson(exchange, 500, ErrorResponse.of(500, "Internal Server Error", ex.getMessage()));
            }
        } catch (Exception ioEx) {
            exchange.close();
        }
    }

    public static Map<String, String> parseQueryParams(URI uri) {
        Map<String, String> params = new HashMap<>();
        String rawQuery = uri.getRawQuery();
        if (rawQuery == null || rawQuery.isEmpty()) return params;
        for (String pair : rawQuery.split("&")) {
            int idx = pair.indexOf("=");
            if (idx > 0) {
                params.put(URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8),
                        URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8));
            } else if (!pair.isEmpty()) {
                params.put(URLDecoder.decode(pair, StandardCharsets.UTF_8), "");
            }
        }
        return params;
    }

    public static Long parseLong(String val) {
        if (val == null || val.trim().isEmpty()) return null;
        try {
            return Long.parseLong(val.trim());
        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid integer parameter: " + val);
        }
    }
}
