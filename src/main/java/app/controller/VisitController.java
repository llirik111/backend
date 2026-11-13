package app.controller;

import app.domain.model.Visit;
import app.domain.model.VisitStatus;
import app.domain.service.GymOperationService;
import app.http.dto.CheckOutRequest;
import app.http.dto.VisitResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/visits")
public class VisitController {
    private final GymOperationService gymOperationService;

    public VisitController(GymOperationService gymOperationService) {
        this.gymOperationService = gymOperationService;
    }

    @GetMapping
    public List<VisitResponse> list(@RequestParam(required = false) Long clientId, @RequestParam(required = false) VisitStatus status) {
        return gymOperationService.listVisits(clientId, status).stream().map(VisitResponse::fromDomain).toList();
    }

    @GetMapping("/{id}")
    public VisitResponse get(@PathVariable Long id) {
        return VisitResponse.fromDomain(gymOperationService.getVisit(id));
    }

    @PostMapping("/{id}/check-out")
    public VisitResponse checkOut(@PathVariable Long id, @RequestBody(required = false) CheckOutRequest req) {
        LocalDateTime time = (req != null && req.exitTime() != null) ? req.exitTime() : LocalDateTime.now();
        return VisitResponse.fromDomain(gymOperationService.checkOut(id, time));
    }
}
