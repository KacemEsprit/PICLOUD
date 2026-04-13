package tn.esprit.pidev.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pidev.dto.SubscriptionRequest;
import tn.esprit.pidev.dto.SubscriptionResponse;
import tn.esprit.pidev.entity.SubscriptionStatus;
import tn.esprit.pidev.service.ISubscriptionService;

import java.util.List;

@RestController
@RequestMapping("/subscriptions")
@Tag(name = "Subscription", description = "Gestion des abonnements — réservé aux PASSENGER")
public class SubscriptionController {

    private final ISubscriptionService service;

    public SubscriptionController(ISubscriptionService service) {
        this.service = service;
    }

    @PostMapping("/passenger/{passengerId}")
    @Operation(summary = "Souscrire à un plan (PASSENGER) — génère des points loyalty automatiquement")
    public ResponseEntity<SubscriptionResponse> subscribe(
            @RequestBody SubscriptionRequest request,
            @PathVariable Long passengerId) {
        return new ResponseEntity<>(service.subscribe(request, passengerId), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un abonnement par ID")
    public ResponseEntity<SubscriptionResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    @Operation(summary = "Tous les abonnements — ADMIN / OPERATOR")
    public ResponseEntity<List<SubscriptionResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/passenger/{passengerId}")
    @Operation(summary = "Abonnements d'un passager")
    public ResponseEntity<List<SubscriptionResponse>> byPassenger(@PathVariable Long passengerId) {
        return ResponseEntity.ok(service.getByPassenger(passengerId));
    }

    @GetMapping("/by-statut/{statut}")
    @Operation(summary = "Filtrer par statut (ACTIVE / EXPIRED / CANCELLED)")
    public ResponseEntity<List<SubscriptionResponse>> byStatut(@PathVariable SubscriptionStatus statut) {
        return ResponseEntity.ok(service.getByStatut(statut));
    }

    @PutMapping("/{id}/cancel/passenger/{passengerId}")
    @Operation(summary = "Annuler un abonnement (PASSENGER)")
    public ResponseEntity<SubscriptionResponse> cancel(
            @PathVariable Long id,
            @PathVariable Long passengerId) {
        return ResponseEntity.ok(service.cancel(id, passengerId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un abonnement — ADMIN")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
