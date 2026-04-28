package tn.esprit.pidev.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pidev.dto.SubscriptionRequest;
import tn.esprit.pidev.dto.SubscriptionResponse;
import tn.esprit.pidev.entity.SubscriptionStatus;
import tn.esprit.pidev.service.ISubscriptionService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subscriptions")
@Tag(name = "Subscription", description = "Gestion des abonnements — réservé aux PASSENGER")
public class SubscriptionController {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionController.class);
    private final ISubscriptionService service;

    public SubscriptionController(ISubscriptionService service) {
        this.service = service;
    }

    @PostMapping("/passenger/{passengerId}")
    @PreAuthorize("hasRole('PASSENGER')")
    @Operation(summary = "Souscrire à un plan (PASSENGER) — génère des points loyalty automatiquement")
    public ResponseEntity<?> subscribe(
            @RequestBody SubscriptionRequest request,
            @PathVariable Long passengerId) {
        try {
            return new ResponseEntity<>(service.subscribe(request, passengerId), HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error subscribing passenger {}: {}", passengerId, e.getMessage());
            return buildErrorResponse(400, "Subscription error: " + e.getMessage());
        }
    }

    @PostMapping("/operator/{operatorId}")
    @PreAuthorize("hasRole('OPERATOR') or hasRole('ADMIN')")
    @Operation(summary = "Créer une subscription pour un opérateur (OPERATOR/ADMIN)")
    public ResponseEntity<?> createForOperator(
            @RequestBody SubscriptionRequest request,
            @PathVariable Long operatorId) {
        try {
            logger.info("Creating subscription for operator: {}", operatorId);
            return new ResponseEntity<>(service.subscribe(request, operatorId), HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error creating subscription for operator {}: {}", operatorId, e.getMessage());
            return buildErrorResponse(400, "Subscription creation error: " + e.getMessage());
        }
    }

    @GetMapping("/operator/{operatorId}")
    @PreAuthorize("hasRole('OPERATOR') or hasRole('ADMIN')")
    @Operation(summary = "Récupérer les subscriptions d'un opérateur")
    public ResponseEntity<List<SubscriptionResponse>> byOperator(@PathVariable Long operatorId) {
        try {
            return ResponseEntity.ok(service.getByPassenger(operatorId));
        } catch (Exception e) {
            logger.error("Error fetching subscriptions for operator {}: {}", operatorId, e.getMessage());
            return ResponseEntity.status(404).build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un abonnement par ID")
    public ResponseEntity<SubscriptionResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    @Operation(summary = "Tous les abonnements — ADMIN / OPERATOR")
    public ResponseEntity<List<SubscriptionResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/passenger/{passengerId}")
    @PreAuthorize("hasRole('PASSENGER') or hasRole('ADMIN') or hasRole('OPERATOR')")
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
    @PreAuthorize("hasRole('PASSENGER')")
    @Operation(summary = "Annuler un abonnement (PASSENGER)")
    public ResponseEntity<?> cancel(
            @PathVariable Long id,
            @PathVariable Long passengerId) {
        try {
            return ResponseEntity.ok(service.cancel(id, passengerId));
        } catch (Exception e) {
            logger.error("Error cancelling subscription {}: {}", id, e.getMessage());
            return buildErrorResponse(400, "Cancellation error: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Supprimer un abonnement — ADMIN")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(int status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", status);
        body.put("message", message);
        body.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.status(status).body(body);
    }
}

