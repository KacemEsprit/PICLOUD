package tn.esprit.pidev.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pidev.dto.LoyaltyAccountResponse;
import tn.esprit.pidev.dto.RedeemRequest;
import tn.esprit.pidev.entity.LoyaltyTier;
import tn.esprit.pidev.service.ILoyaltyAccountService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/loyalty-accounts")
@Tag(name = "Loyalty Account", description = "Programme de fidélité des passagers")
public class LoyaltyAccountController {

    private static final Logger logger = LoggerFactory.getLogger(LoyaltyAccountController.class);
    private final ILoyaltyAccountService service;

    public LoyaltyAccountController(ILoyaltyAccountService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un compte fidélité par ID")
    public ResponseEntity<LoyaltyAccountResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/passenger/{passengerId}")
    @PreAuthorize("hasRole('PASSENGER') or hasRole('ADMIN') or hasRole('OPERATOR')")
    @Operation(summary = "Récupérer le compte fidélité d'un passager")
    public ResponseEntity<?> byPassenger(@PathVariable Long passengerId) {
        try {
            return ResponseEntity.ok(service.getByPassenger(passengerId));
        } catch (Exception e) {
            logger.error("Error fetching loyalty account for passenger {}: {}", passengerId, e.getMessage());
            return buildErrorResponse(404, "Loyalty account not found for passenger: " + passengerId);
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    @Operation(summary = "Tous les comptes fidélité — ADMIN / OPERATOR")
    public ResponseEntity<?> getAll() {
        try {
            return ResponseEntity.ok(service.getAll());
        } catch (Exception e) {
            logger.error("Error fetching loyalty accounts: {}", e.getMessage());
            return buildErrorResponse(403, "Access denied or error: " + e.getMessage());
        }
    }

    @GetMapping("/by-tier/{tier}")
    @Operation(summary = "Filtrer par niveau (BRONZE / SILVER / GOLD)")
    public ResponseEntity<List<LoyaltyAccountResponse>> byTier(@PathVariable LoyaltyTier tier) {
        return ResponseEntity.ok(service.getByTier(tier));
    }

    @PostMapping("/redeem/passenger/{passengerId}")
    @PreAuthorize("hasRole('PASSENGER')")
    @Operation(summary = "Utiliser des points pour obtenir une réduction (PASSENGER)")
    public ResponseEntity<?> redeem(
            @PathVariable Long passengerId,
            @RequestBody RedeemRequest request) {
        try {
            return ResponseEntity.ok(service.redeemPoints(passengerId, request));
        } catch (Exception e) {
            logger.error("Error redeeming points: {}", e.getMessage());
            return buildErrorResponse(403, "Unable to redeem points: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Supprimer un compte fidélité — ADMIN")
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

