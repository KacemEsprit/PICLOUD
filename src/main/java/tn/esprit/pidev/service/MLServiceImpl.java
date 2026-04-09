package tn.esprit.pidev.service;

import org.springframework.stereotype.Service;
import tn.esprit.pidev.dto.ChurnPredictionResponse;
import tn.esprit.pidev.dto.PlanRecommendationResponse;
import tn.esprit.pidev.entity.*;
import tn.esprit.pidev.repository.LoyaltyAccountRepository;
import tn.esprit.pidev.repository.SubscriptionRepository;
import tn.esprit.pidev.repository.UserRepository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service ML maison — sans bibliothèque externe.
 *
 * Algorithme 1 — Recommandation : Scoring pondéré sur 4 features normalisées
 * Algorithme 2 — Churn : Régression logistique avec fonction sigmoid sur 5 features
 */
@Service
public class MLServiceImpl implements IMLService {

    private final SubscriptionRepository subscriptionRepo;
    private final LoyaltyAccountRepository loyaltyRepo;
    private final UserRepository userRepo;

    // Poids Recommandation
    private static final double W_FIDELITE   = 0.30;
    private static final double W_PRIX_MOY   = 0.35;
    private static final double W_POINTS     = 0.25;
    private static final double W_ANNULATION = 0.10;

    // Poids Churn (régression logistique)
    private static final double W0 = -1.5;
    private static final double W1 = -2.0;
    private static final double W2 =  2.5;
    private static final double W3 =  1.8;
    private static final double W4 =  2.0;
    private static final double W5 =  1.2;

    public MLServiceImpl(SubscriptionRepository subscriptionRepo,
                         LoyaltyAccountRepository loyaltyRepo,
                         UserRepository userRepo) {
        this.subscriptionRepo = subscriptionRepo;
        this.loyaltyRepo = loyaltyRepo;
        this.userRepo = userRepo;
    }

    // ===== RECOMMANDATION =====
    @Override
    public PlanRecommendationResponse recommendPlan(Long passengerId) {
        User user = userRepo.findById(passengerId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable : " + passengerId));

        List<Subscription> historique = subscriptionRepo.findByPassengerId(passengerId);

        double f1 = Math.min(historique.size() / 10.0, 1.0);

        double prixMoyen = historique.stream()
                .filter(s -> s.getPricingPlan() != null)
                .mapToDouble(s -> s.getPricingPlan().getPrix())
                .average().orElse(0.0);
        double f2 = Math.min(prixMoyen / 50.0, 1.0);

        int points = loyaltyRepo.findByPassengerId(passengerId)
                .map(LoyaltyAccount::getPointsCumules).orElse(0);
        double f3 = Math.min(points / 600.0, 1.0);

        long nbAnnulations = historique.stream()
                .filter(s -> s.getStatut() == SubscriptionStatus.CANCELLED).count();
        double f4 = 1.0 - Math.min(nbAnnulations / 5.0, 1.0);

        double score = (f1 * W_FIDELITE) + (f2 * W_PRIX_MOY) + (f3 * W_POINTS) + (f4 * W_ANNULATION);

        String plan; double confidence; String reason;
        if (score >= 0.65) {
            plan = PricingType.PREMIUM.name();
            confidence = Math.min(score * 100, 98.0);
            reason = "Utilisateur fidèle avec un fort engagement et un historique premium.";
        } else if (score >= 0.35) {
            plan = PricingType.BASIC.name();
            confidence = Math.min(score * 100 + 10, 85.0);
            reason = "Profil intermédiaire — le plan Basic correspond à vos habitudes.";
        } else {
            plan = PricingType.FREE.name();
            confidence = Math.max(100 - score * 100, 60.0);
            reason = "Nouvel utilisateur — commencez avec le plan gratuit.";
        }

        PlanRecommendationResponse dto = new PlanRecommendationResponse();
        dto.setUserId(passengerId);
        dto.setUsername(user.getUsername());
        dto.setRecommendedPlan(plan);
        dto.setConfidence(Math.round(confidence * 10.0) / 10.0);
        dto.setReason(reason);
        return dto;
    }

    // ===== CHURN =====
    @Override
    public ChurnPredictionResponse predictChurn(Long passengerId) {
        User user = userRepo.findById(passengerId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable : " + passengerId));

        List<Subscription> historique = subscriptionRepo.findByPassengerId(passengerId);

        if (historique.isEmpty()) {
            ChurnPredictionResponse dto = new ChurnPredictionResponse();
            dto.setUserId(passengerId);
            dto.setUsername(user.getUsername());
            dto.setChurnProbability(0.5);
            dto.setRiskLevel("INCONNU");
            dto.setSuggestedAction("Aucun historique disponible.");
            return dto;
        }

        // f1 : jours restants abonnement actif (normalisé)
        double f1 = historique.stream()
                .filter(s -> s.getStatut() == SubscriptionStatus.ACTIVE)
                .mapToLong(s -> ChronoUnit.DAYS.between(LocalDate.now(), s.getDateFin()))
                .filter(d -> d >= 0)
                .mapToDouble(d -> Math.min(d / 365.0, 1.0))
                .average().orElse(0.0);

        // f2 : taux d'annulation
        long nbAnn = historique.stream().filter(s -> s.getStatut() == SubscriptionStatus.CANCELLED).count();
        double f2 = Math.min((double) nbAnn / historique.size(), 1.0);

        // f3 : inactivité points
        int pts = loyaltyRepo.findByPassengerId(passengerId).map(LoyaltyAccount::getPointsCumules).orElse(0);
        double f3 = 1.0 - Math.min(pts / 600.0, 1.0);

        // f4 : expiré sans renouvellement
        boolean aExpire = historique.stream().anyMatch(s -> s.getStatut() == SubscriptionStatus.EXPIRED);
        boolean aActif  = historique.stream().anyMatch(s -> s.getStatut() == SubscriptionStatus.ACTIVE);
        double f4 = (aExpire && !aActif) ? 1.0 : 0.0;

        // f5 : courtes durées
        double dureeMoy = historique.stream()
                .mapToLong(s -> ChronoUnit.DAYS.between(s.getDateDebut(), s.getDateFin()))
                .average().orElse(180.0);
        double f5 = 1.0 - Math.min(dureeMoy / 360.0, 1.0);

        double z = W0 + W1 * f1 + W2 * f2 + W3 * f3 + W4 * f4 + W5 * f5;
        double prob = Math.round(sigmoid(z) * 1000.0) / 1000.0;

        String risk; String action; String promo;
        if (prob >= 0.70) {
            risk = "ÉLEVÉ"; action = "Contacter le client immédiatement avec une offre urgente."; promo = "FLASH15";
        } else if (prob >= 0.40) {
            risk = "MODÉRÉ"; action = "Envoyer un email d'engagement et proposer une réduction."; promo = "SUMMER20";
        } else {
            risk = "FAIBLE"; action = "Client fidèle — maintenir l'engagement avec des points bonus."; promo = null;
        }

        ChurnPredictionResponse dto = new ChurnPredictionResponse();
        dto.setUserId(passengerId);
        dto.setUsername(user.getUsername());
        dto.setChurnProbability(prob);
        dto.setRiskLevel(risk);
        dto.setSuggestedAction(action);
        dto.setSuggestedPromoCode(promo);
        return dto;
    }

    @Override
    public List<ChurnPredictionResponse> predictChurnAll() {
        return userRepo.findAll().stream()
                .filter(u -> u.getRole() == RoleEnum.PASSENGER)
                .map(u -> predictChurn(u.getId()))
                .collect(Collectors.toList());
    }

    private double sigmoid(double z) {
        return 1.0 / (1.0 + Math.exp(-z));
    }
}
