package app;

import app.domain.repository.ClientRepository;
import app.domain.repository.MembershipRepository;
import app.domain.repository.VisitRepository;
import app.domain.service.ClientService;
import app.domain.service.GymOperationService;
import app.domain.service.MembershipService;
import app.http.handler.ClientHandler;
import app.http.handler.MembershipHandler;
import app.http.handler.VisitHandler;
import app.storage.InMemoryClientRepository;
import app.storage.InMemoryMembershipRepository;
import app.storage.InMemoryVisitRepository;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class Main {
    private static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) throws IOException {
        int port = getPort(args);

        ClientRepository clientRepo = new InMemoryClientRepository();
        MembershipRepository membershipRepo = new InMemoryMembershipRepository();
        VisitRepository visitRepo = new InMemoryVisitRepository();

        ClientService clientService = new ClientService(clientRepo, membershipRepo, visitRepo);
        MembershipService membershipService = new MembershipService(membershipRepo, visitRepo, clientService);
        GymOperationService gymOpService = new GymOperationService(membershipRepo, visitRepo, clientService);

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/ping", new PingHandler());
        server.createContext("/api/clients", new ClientHandler(clientService));
        server.createContext("/api/memberships", new MembershipHandler(membershipService, gymOpService));
        server.createContext("/api/visits", new VisitHandler(gymOpService));
        server.setExecutor(Executors.newFixedThreadPool(16));

        server.start();
        System.out.println("Gym HTTP Server started on port " + port);
    }

    private static int getPort(String[] args) {
        if (args != null && args.length > 0) {
            try { return Integer.parseInt(args[0]); } catch (NumberFormatException ignored) {}
        }
        String envPort = System.getenv("PORT");
        if (envPort != null && !envPort.isBlank()) {
            try { return Integer.parseInt(envPort); } catch (NumberFormatException ignored) {}
        }
        return DEFAULT_PORT;
    }
}
