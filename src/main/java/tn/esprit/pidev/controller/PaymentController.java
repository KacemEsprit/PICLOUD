package tn.esprit.pidev.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pidev.dto.PaymentInitRequest;
import tn.esprit.pidev.dto.PaymentInitResponse;
import tn.esprit.pidev.dto.SubscriptionResponse;
import tn.esprit.pidev.repository.UserRepository;
import tn.esprit.pidev.service.IStripePaymentService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@Tag(name = "Paiement Stripe",
        description = "Initiation et confirmation des paiements via Stripe")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);
    private final IStripePaymentService paymentService;
    private final UserRepository userRepository;

    public PaymentController(IStripePaymentService paymentService, UserRepository userRepository) {
        this.paymentService = paymentService;
        this.userRepository = userRepository;
    }

    @PostMapping("/initiate")
    @PreAuthorize("hasRole('PASSENGER')")
    @Operation(summary = "Initier un paiement Stripe — retourne l'URL checkout")
    public ResponseEntity<?> initiate(
            @RequestBody PaymentInitRequest request) {
        try {
            return ResponseEntity.ok(paymentService.initiatePayment(request));
        } catch (Exception e) {
            logger.error("Error initiating payment: {}", e.getMessage());
            return buildErrorResponse(500, "Error initiating payment: " + e.getMessage());
        }
    }

    @PostMapping("/initiate/me")
    @PreAuthorize("hasRole('PASSENGER')")
    @Operation(summary = "Initier un paiement pour l'utilisateur actuel — retourne l'URL checkout")
    public ResponseEntity<?> initiatMe(
            @RequestBody PaymentInitRequest request) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName(); // Get username from authentication
            logger.info("Initiating payment for user: {}", username);

            // Lookup user by username to get the ID
            Long userId = userRepository.findByUsername(username)
                    .map(user -> user.getId())
                    .orElseThrow(() -> new RuntimeException("User not found: " + username));

            logger.info("Found user ID for username '{}': {}", username, userId);

            // Set the passenger ID from user lookup
            request.setPassengerId(userId);
            return ResponseEntity.ok(paymentService.initiatePayment(request));
        } catch (Exception e) {
            logger.error("Error initiating payment for current user: {}", e.getMessage());
            return buildErrorResponse(500, "Error initiating payment: " + e.getMessage());
        }
    }

    @GetMapping("/success")
    @Operation(summary = "Callback Stripe après paiement réussi — active la subscription")
    public ResponseEntity<?> success(
            @RequestParam String session_id) {
        try {
            return ResponseEntity.ok(paymentService.confirmPayment(session_id));
        } catch (Exception e) {
            logger.error("Error confirming payment: {}", e.getMessage());
            return buildErrorResponse(500, "Error confirming payment: " + e.getMessage());
        }
    }

    @GetMapping("/cancel")
    @Operation(summary = "Callback Stripe après annulation")
    public ResponseEntity<Map<String, String>> cancel() {
        return ResponseEntity.ok(Map.of(
                "status", "CANCELLED",
                "message", "Paiement annulé par l'utilisateur."
        ));
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(int status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", status);
        body.put("message", message);
        body.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.status(status).body(body);
    }
}



