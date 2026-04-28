package tn.esprit.pidev.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pidev.dto.PricingPlanRequest;
import tn.esprit.pidev.dto.PricingPlanResponse;
import tn.esprit.pidev.entity.PricingType;
import tn.esprit.pidev.service.IPricingPlanService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pricing-plans")
@Tag(name = "Pricing Plan", description = "Gestion des plans tarifaires — réservé aux OPERATOR")
public class PricingPlanController {

    private static final Logger logger = LoggerFactory.getLogger(PricingPlanController.class);
    private final IPricingPlanService service;

    public PricingPlanController(IPricingPlanService service) {
        this.service = service;
    }

    @PostMapping("/operator/{operatorId}")
    @PreAuthorize("hasRole('OPERATOR') or hasRole('ADMIN')")
    @Operation(summary = "Créer un plan tarifaire (OPERATOR)")
    public ResponseEntity<?> create(
            @RequestBody PricingPlanRequest request,
            @PathVariable Long operatorId) {
        try {
            return new ResponseEntity<>(service.create(request, operatorId), HttpStatus.CREATED);
        } catch (RuntimeException e) {
            logger.error("Authorization error for create: {}", e.getMessage());
            return buildErrorResponse(403, "Forbidden: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('OPERATOR') or hasRole('ADMIN') or hasRole('PASSENGER') or hasRole('AGENT')")
    @Operation(summary = "Récupérer un plan par ID")
    public ResponseEntity<PricingPlanResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('OPERATOR') or hasRole('ADMIN') or hasRole('PASSENGER') or hasRole('AGENT')")
    @Operation(summary = "Récupérer tous les plans — accessible à tous")
    public ResponseEntity<List<PricingPlanResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @PutMapping("/{id}/operator/{operatorId}")
    @PreAuthorize("hasRole('OPERATOR') or hasRole('ADMIN')")
    @Operation(summary = "Modifier un plan tarifaire (OPERATOR)")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @RequestBody PricingPlanRequest request,
            @PathVariable Long operatorId) {
        try {
            return ResponseEntity.ok(service.update(id, request, operatorId));
        } catch (RuntimeException e) {
            logger.error("Authorization error for update: {}", e.getMessage());
            return buildErrorResponse(403, "Forbidden: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}/operator/{operatorId}")
    @PreAuthorize("hasRole('OPERATOR') or hasRole('ADMIN')")
    @Operation(summary = "Supprimer un plan tarifaire (OPERATOR)")
    public ResponseEntity<?> delete(
            @PathVariable Long id,
            @PathVariable Long operatorId) {
        try {
            service.delete(id, operatorId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            logger.error("Authorization error for delete: {}", e.getMessage());
            return buildErrorResponse(403, "Forbidden: " + e.getMessage());
        }
    }

    @GetMapping("/by-type/{type}")
    @Operation(summary = "Filtrer par type (FREE / BASIC / PREMIUM)")
    public ResponseEntity<List<PricingPlanResponse>> byType(@PathVariable PricingType type) {
        return ResponseEntity.ok(service.getByType(type));
    }

    @GetMapping("/by-max-price/{max}")
    @Operation(summary = "Filtrer par prix maximum")
    public ResponseEntity<List<PricingPlanResponse>> byMaxPrice(@PathVariable Double max) {
        return ResponseEntity.ok(service.getByMaxPrice(max));
    }

    @GetMapping("/operator/{operatorId}")
    @PreAuthorize("hasRole('OPERATOR') or hasRole('ADMIN')")
    @Operation(summary = "Plans créés par un opérateur donné")
    public ResponseEntity<?> byOperator(@PathVariable Long operatorId) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            logger.info("User {} accessing pricing plans for operator {}",
                    auth.getName(), operatorId);
            return ResponseEntity.ok(service.getByOperator(operatorId));
        } catch (Exception e) {
            logger.error("Error fetching pricing plans for operator {}: {}",
                    operatorId, e.getMessage(), e);
            return buildErrorResponse(403, "Access denied or invalid operator ID");
        }
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(int status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", status);
        body.put("message", message);
        body.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.status(status).body(body);
    }
}

