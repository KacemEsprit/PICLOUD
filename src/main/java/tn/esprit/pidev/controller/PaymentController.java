package tn.esprit.pidev.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pidev.dto.PaymentInitRequest;
import tn.esprit.pidev.dto.PaymentInitResponse;
import tn.esprit.pidev.dto.SubscriptionResponse;
import tn.esprit.pidev.service.IStripePaymentService;

import java.util.Map;

@RestController
@RequestMapping("/payment")
@Tag(name = "Paiement Stripe",
        description = "Initiation et confirmation des paiements via Stripe")
public class PaymentController {

    private final IStripePaymentService paymentService;

    public PaymentController(IStripePaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/initiate")
    @Operation(summary = "Initier un paiement Stripe — retourne l'URL checkout")
    public ResponseEntity<PaymentInitResponse> initiate(
            @RequestBody PaymentInitRequest request) {
        return ResponseEntity.ok(paymentService.initiatePayment(request));
    }

    @GetMapping("/success")
    @Operation(summary = "Callback Stripe après paiement réussi — active la subscription")
    public ResponseEntity<SubscriptionResponse> success(
            @RequestParam String session_id) {
        return ResponseEntity.ok(paymentService.confirmPayment(session_id));
    }

    @GetMapping("/cancel")
    @Operation(summary = "Callback Stripe après annulation")
    public ResponseEntity<Map<String, String>> cancel() {
        return ResponseEntity.ok(Map.of(
                "status", "CANCELLED",
                "message", "Paiement annulé par l'utilisateur."
        ));
    }
}