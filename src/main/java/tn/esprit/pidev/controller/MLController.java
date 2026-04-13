package tn.esprit.pidev.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.pidev.dto.ChurnPredictionResponse;
import tn.esprit.pidev.dto.PlanRecommendationResponse;
import tn.esprit.pidev.service.IMLService;

import java.util.List;

@RestController
@RequestMapping("/ml")
@Tag(name = "IA & ML", description = "Recommandation de plan et prédiction de churn — algorithmes maison")
public class MLController {

    private final IMLService service;

    public MLController(IMLService service) {
        this.service = service;
    }

    @GetMapping("/recommend/{passengerId}")
    @Operation(summary = "Recommander un plan pour un passager (scoring pondéré)")
    public ResponseEntity<PlanRecommendationResponse> recommend(@PathVariable Long passengerId) {
        return ResponseEntity.ok(service.recommendPlan(passengerId));
    }

    @GetMapping("/churn/{passengerId}")
    @Operation(summary = "Prédire le risque de churn d'un passager (régression logistique)")
    public ResponseEntity<ChurnPredictionResponse> churn(@PathVariable Long passengerId) {
        return ResponseEntity.ok(service.predictChurn(passengerId));
    }

    @GetMapping("/churn/all")
    @Operation(summary = "Prédire le churn de tous les passagers — OPERATOR / ADMIN")
    public ResponseEntity<List<ChurnPredictionResponse>> churnAll() {
        return ResponseEntity.ok(service.predictChurnAll());
    }
}
