package tn.esprit.pidev.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.pidev.dto.ChurnPredictionResponse;
import tn.esprit.pidev.dto.PlanRecommendationResponse;
import tn.esprit.pidev.service.IMLService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ml")
@Tag(name = "IA & ML", description = "Recommandation de plan et prédiction de churn — algorithmes maison")
public class MLController {

    private static final Logger logger = LoggerFactory.getLogger(MLController.class);
    private final IMLService service;

    public MLController(IMLService service) {
        this.service = service;
    }

    @GetMapping("/recommend/{passengerId}")
    @PreAuthorize("hasRole('PASSENGER') or hasRole('OPERATOR') or hasRole('ADMIN')")
    @Operation(summary = "Recommander un plan pour un passager (scoring pondéré)")
    public ResponseEntity<?> recommend(@PathVariable Long passengerId) {
        try {
            logger.info("Recommending plan for passenger: {}", passengerId);
            return ResponseEntity.ok(service.recommendPlan(passengerId));
        } catch (Exception e) {
            logger.error("Error recommending plan: {}", e.getMessage());
            return buildErrorResponse(500, "Error generating recommendation: " + e.getMessage());
        }
    }

    @GetMapping("/churn/{passengerId}")
    @PreAuthorize("hasRole('OPERATOR') or hasRole('ADMIN')")
    @Operation(summary = "Prédire le risque de churn d'un passager (régression logistique)")
    public ResponseEntity<?> churn(@PathVariable Long passengerId) {
        try {
            logger.info("Predicting churn for passenger: {}", passengerId);
            return ResponseEntity.ok(service.predictChurn(passengerId));
        } catch (Exception e) {
            logger.error("Error predicting churn: {}", e.getMessage());
            return buildErrorResponse(500, "Error predicting churn: " + e.getMessage());
        }
    }

    @GetMapping("/churn/all")
    @PreAuthorize("hasRole('OPERATOR') or hasRole('ADMIN')")
    @Operation(summary = "Prédire le churn de tous les passagers — OPERATOR / ADMIN")
    public ResponseEntity<?> churnAll() {
        try {
            logger.info("Predicting churn for all passengers");
            return ResponseEntity.ok(service.predictChurnAll());
        } catch (Exception e) {
            logger.error("Error predicting churn for all: {}", e.getMessage());
            return buildErrorResponse(500, "Error predicting churn: " + e.getMessage());
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

