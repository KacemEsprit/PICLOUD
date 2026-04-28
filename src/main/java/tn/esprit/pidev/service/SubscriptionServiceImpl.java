package tn.esprit.pidev.service;

import org.springframework.stereotype.Service;
import tn.esprit.pidev.dto.SubscriptionRequest;
import tn.esprit.pidev.dto.SubscriptionResponse;
import tn.esprit.pidev.entity.*;
import tn.esprit.pidev.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SubscriptionServiceImpl implements ISubscriptionService {

    private final SubscriptionRepository subscriptionRepo;
    private final PricingPlanRepository planRepo;
    private final UserRepository userRepo;
    private final LoyaltyAccountRepository loyaltyRepo;
    private final PointTransactionRepository txRepo;
    private final ReductionRepository reductionRepo;

    public SubscriptionServiceImpl(SubscriptionRepository subscriptionRepo,
                                   PricingPlanRepository planRepo,
                                   UserRepository userRepo,
                                   LoyaltyAccountRepository loyaltyRepo,
                                   PointTransactionRepository txRepo,
                                   ReductionRepository reductionRepo) {
        this.subscriptionRepo = subscriptionRepo;
        this.planRepo = planRepo;
        this.userRepo = userRepo;
        this.loyaltyRepo = loyaltyRepo;
        this.txRepo = txRepo;
        this.reductionRepo = reductionRepo;
    }

    @Override
    public SubscriptionResponse subscribe(SubscriptionRequest request, Long passengerId) {
        // Vérifier que l'utilisateur est bien un PASSENGER
        User passenger = getPassenger(passengerId);

        // Récupérer le plan
        PricingPlan plan = planRepo.findById(request.getPricingPlanId())
                .orElseThrow(() -> new RuntimeException("Plan introuvable : " + request.getPricingPlanId()));

        // Multiple active subscriptions are allowed by current product flow.

        // Appliquer réduction si code fourni
        double prixFinal = plan.getPrix();
        if (request.getCodeReduction() != null && !request.getCodeReduction().isBlank()) {
            Reduction red = reductionRepo.findByCode(request.getCodeReduction().toUpperCase())
                    .orElseThrow(() -> new RuntimeException("Code de réduction invalide."));
            if (red.getDateExpiration().isBefore(LocalDate.now())) {
                throw new RuntimeException("Ce code de réduction est expiré.");
            }
            LoyaltyAccount la = loyaltyRepo.findByPassengerId(passengerId).orElse(null);
            int pts = la != null ? la.getPointsCumules() : 0;
            if (pts < red.getPointsRequis()) {
                throw new RuntimeException("Points insuffisants pour ce code. Requis : "
                        + red.getPointsRequis() + ", vous avez : " + pts);
            }
            prixFinal = plan.getPrix() - (plan.getPrix() * red.getPourcentage() / 100);
        }

        // Créer l'abonnement
        LocalDate debut = LocalDate.now();
        LocalDate fin = debut.plusMonths(plan.getDureeEnMois());
        Subscription sub = new Subscription(debut, fin, SubscriptionStatus.ACTIVE, passenger, plan);
        Subscription saved = subscriptionRepo.save(sub);

        // === Générer les points loyalty automatiquement ===
        int pointsGagnes = (int) Math.floor(prixFinal);
        LoyaltyAccount account = loyaltyRepo.findByPassengerId(passengerId)
                .orElseGet(() -> {
                    LoyaltyAccount newAccount = new LoyaltyAccount(passenger);
                    return loyaltyRepo.save(newAccount);
                });
        account.setPointsCumules(account.getPointsCumules() + pointsGagnes);
        account.setNiveau(calculerTier(account.getPointsCumules()));
        loyaltyRepo.save(account);

        txRepo.save(new PointTransaction(
                pointsGagnes, TransactionType.EARNED, LocalDateTime.now(),
                "Points gagnés — abonnement : " + plan.getNom(), account));

        return SubscriptionResponse.fromEntity(saved);
    }

    @Override
    public SubscriptionResponse getById(Long id) {
        return SubscriptionResponse.fromEntity(findById(id));
    }

    @Override
    public List<SubscriptionResponse> getAll() {
        return subscriptionRepo.findAll().stream()
                .map(SubscriptionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<SubscriptionResponse> getByPassenger(Long passengerId) {
        return subscriptionRepo.findByPassengerId(passengerId).stream()
                .map(SubscriptionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<SubscriptionResponse> getByStatut(SubscriptionStatus statut) {
        return subscriptionRepo.findByStatut(statut).stream()
                .map(SubscriptionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public SubscriptionResponse cancel(Long id, Long passengerId) {
        Subscription sub = findById(id);
        if (!sub.getPassenger().getId().equals(passengerId)) {
            throw new RuntimeException("Vous ne pouvez annuler que vos propres abonnements.");
        }
        sub.setStatut(SubscriptionStatus.CANCELLED);
        return SubscriptionResponse.fromEntity(subscriptionRepo.save(sub));
    }

    @Override
    public void delete(Long id) {
        findById(id);
        subscriptionRepo.deleteById(id);
    }

    // ===== Helpers =====
    private Subscription findById(Long id) {
        return subscriptionRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Subscription introuvable : " + id));
    }

    private User getPassenger(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable : " + userId));
        if (user.getRole() != RoleEnum.PASSENGER) {
            throw new RuntimeException("Accès refusé : seuls les PASSENGER peuvent souscrire.");
        }
        return user;
    }

    private LoyaltyTier calculerTier(int pts) {
        if (pts >= 500) return LoyaltyTier.GOLD;
        if (pts >= 200) return LoyaltyTier.SILVER;
        return LoyaltyTier.BRONZE;
    }
}
