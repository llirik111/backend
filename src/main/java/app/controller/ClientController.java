package app.controller;

import app.domain.exception.ValidationException;
import app.domain.model.Client;
import app.domain.model.ClientStatus;
import app.domain.service.ClientService;
import app.http.dto.ClientRequest;
import app.http.dto.ClientResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/clients")
public class ClientController {
    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping
    public List<ClientResponse> list(@RequestParam(required = false) String query, @RequestParam(required = false) ClientStatus status) {
        return clientService.listClients(query, status).stream().map(ClientResponse::fromDomain).toList();
    }

    @PostMapping
    public ResponseEntity<ClientResponse> create(@RequestBody(required = false) ClientRequest req) {
        if (req == null) throw new ValidationException("Request body must not be null");
        Client client = clientService.createClient(req.fullName(), req.phone(), req.email(), req.birthDate());
        return ResponseEntity.created(URI.create("/api/clients/" + client.getId())).body(ClientResponse.fromDomain(client));
    }

    @GetMapping("/{id}")
    public ClientResponse get(@PathVariable Long id) {
        return ClientResponse.fromDomain(clientService.getClient(id));
    }

    @PutMapping("/{id}")
    public ClientResponse update(@PathVariable Long id, @RequestBody(required = false) ClientRequest req) {
        if (req == null) throw new ValidationException("Request body must not be null");
        return ClientResponse.fromDomain(clientService.updateClient(id, req.fullName(), req.phone(), req.email(), req.birthDate()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        clientService.deleteClient(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/block")
    public ClientResponse block(@PathVariable Long id) {
        return ClientResponse.fromDomain(clientService.blockClient(id));
    }

    @PostMapping("/{id}/unblock")
    public ClientResponse unblock(@PathVariable Long id) {
        return ClientResponse.fromDomain(clientService.unblockClient(id));
    }
}
