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
import tn.esprit.pidev.dto.ReductionRequest;
import tn.esprit.pidev.dto.ReductionResponse;
import tn.esprit.pidev.service.IReductionService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reductions")
@Tag(name = "Reduction", description = "Gestion des réductions — réservé aux OPERATOR")
public class ReductionController {

    private static final Logger logger = LoggerFactory.getLogger(ReductionController.class);
    private final IReductionService service;

    public ReductionController(IReductionService service) {
        this.service = service;
    }

    @PostMapping("/operator/{operatorId}")
    @PreAuthorize("hasRole('OPERATOR') or hasRole('ADMIN')")
    @Operation(summary = "Créer une réduction (OPERATOR)")
    public ResponseEntity<?> create(
            @RequestBody ReductionRequest request,
            @PathVariable Long operatorId) {
        try {
            return new ResponseEntity<>(service.create(request, operatorId), HttpStatus.CREATED);
        } catch (RuntimeException e) {
            logger.error("Authorization error for create: {}", e.getMessage());
            return buildErrorResponse(403, "Forbidden: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer une réduction par ID")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.getById(id));
        } catch (Exception e) {
            logger.error("Error fetching reduction {}: {}", id, e.getMessage());
            return buildErrorResponse(404, "Reduction not found: " + id);
        }
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Récupérer une réduction par code promo")
    public ResponseEntity<?> byCode(@PathVariable String code) {
        try {
            return ResponseEntity.ok(service.getByCode(code));
        } catch (Exception e) {
            logger.error("Error fetching reduction by code {}: {}", code, e.getMessage());
            return buildErrorResponse(404, "Reduction code not found: " + code);
        }
    }

    @GetMapping
    @Operation(summary = "Toutes les réductions")
    public ResponseEntity<?> getAll() {
        try {
            return ResponseEntity.ok(service.getAll());
        } catch (Exception e) {
            logger.error("Error fetching all reductions: {}", e.getMessage());
            return buildErrorResponse(500, "Error fetching reductions: " + e.getMessage());
        }
    }

    // ...existing code...

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OPERATOR') or hasRole('ADMIN')")
    @Operation(summary = "Modifier une réduction (OPERATOR)")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @RequestBody ReductionRequest request) {
        try {
            return ResponseEntity.ok(service.update(id, request));
        } catch (RuntimeException e) {
            logger.error("Authorization error for update: {}", e.getMessage());
            return buildErrorResponse(403, "Forbidden: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OPERATOR') or hasRole('ADMIN')")
    @Operation(summary = "Supprimer une réduction")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            service.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            logger.error("Authorization error for delete: {}", e.getMessage());
            return buildErrorResponse(403, "Forbidden: " + e.getMessage());
        }
    }

    @GetMapping("/valides")
    @Operation(summary = "Réductions non expirées")
    public ResponseEntity<List<ReductionResponse>> valides() {
        return ResponseEntity.ok(service.getValides());
    }

    @GetMapping("/accessibles/{points}")
    @Operation(summary = "Réductions accessibles selon les points disponibles (PASSENGER)")
    public ResponseEntity<List<ReductionResponse>> accessibles(@PathVariable Integer points) {
        return ResponseEntity.ok(service.getAccessibles(points));
    }

    @GetMapping("/operator/{operatorId}")
    @PreAuthorize("hasRole('OPERATOR') or hasRole('ADMIN')")
    @Operation(summary = "Réductions créées par un opérateur")
    public ResponseEntity<?> byOperator(@PathVariable Long operatorId) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            logger.info("User {} accessing reductions for operator {}",
                    auth.getName(), operatorId);
            return ResponseEntity.ok(service.getByOperator(operatorId));
        } catch (Exception e) {
            logger.error("Error fetching reductions for operator {}: {}",
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

