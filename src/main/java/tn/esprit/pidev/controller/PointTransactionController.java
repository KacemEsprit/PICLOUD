package tn.esprit.pidev.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pidev.entity.PointTransaction;
import tn.esprit.pidev.repository.PointTransactionRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/point-transactions")
@Tag(name = "Point Transactions", description = "Gestion des transactions de points de fidélité")
@RequiredArgsConstructor
public class PointTransactionController {

    private static final Logger logger = LoggerFactory.getLogger(PointTransactionController.class);
    private final PointTransactionRepository pointTransactionRepository;

    @GetMapping("/account/{accountId}")
    @PreAuthorize("hasRole('PASSENGER') or hasRole('ADMIN') or hasRole('OPERATOR')")
    @Operation(summary = "Récupérer les transactions de points pour un compte de fidélité")
    public ResponseEntity<?> getTransactionsByAccount(@PathVariable Long accountId) {
        try {
            List<PointTransaction> transactions = pointTransactionRepository.findByLoyaltyAccountId(accountId);
            logger.info("Retrieved {} transactions for loyalty account {}", transactions.size(), accountId);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            logger.error("Error fetching point transactions for account {}: {}", accountId, e.getMessage());
            return buildErrorResponse(500, "Error fetching point transactions: " + e.getMessage());
        }
    }

    @GetMapping("/account/{accountId}/type/{type}")
    @PreAuthorize("hasRole('PASSENGER') or hasRole('ADMIN') or hasRole('OPERATOR')")
    @Operation(summary = "Récupérer les transactions filtrées par type pour un compte")
    public ResponseEntity<?> getTransactionsByAccountAndType(
            @PathVariable Long accountId,
            @PathVariable String type) {
        try {
            List<PointTransaction> transactions = pointTransactionRepository
                    .findByLoyaltyAccountIdAndType(accountId, Enum.valueOf(
                            tn.esprit.pidev.entity.TransactionType.class, type.toUpperCase()));
            logger.info("Retrieved {} {} transactions for loyalty account {}",
                    transactions.size(), type, accountId);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            logger.error("Error fetching point transactions for account {}: {}", accountId, e.getMessage());
            return buildErrorResponse(500, "Error fetching point transactions: " + e.getMessage());
        }
    }

    @GetMapping("/passenger/{passengerId}")
    @PreAuthorize("hasRole('PASSENGER') or hasRole('ADMIN') or hasRole('OPERATOR')")
    @Operation(summary = "Récupérer les transactions de points pour un passager")
    public ResponseEntity<?> getTransactionsByPassenger(@PathVariable Long passengerId) {
        try {
            List<PointTransaction> transactions = pointTransactionRepository
                    .findByLoyaltyAccountPassengerId(passengerId);
            logger.info("Retrieved {} transactions for passenger {}", transactions.size(), passengerId);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            logger.error("Error fetching point transactions for passenger {}: {}", passengerId, e.getMessage());
            return buildErrorResponse(500, "Error fetching point transactions: " + e.getMessage());
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

