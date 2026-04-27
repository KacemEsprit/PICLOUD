package tn.esprit.pidev.service;


import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tn.esprit.pidev.dto.PaymentInitRequest;
import tn.esprit.pidev.dto.PaymentInitResponse;
import tn.esprit.pidev.dto.SubscriptionResponse;
import tn.esprit.pidev.entity.*;
import tn.esprit.pidev.repository.*;
import tn.esprit.pidev.service.IStripePaymentService;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class StripePaymentServiceImpl implements IStripePaymentService {

    @Value("${stripe.secret-key}")
    private String secretKey;

    @Value("${stripe.success-url}")
    private String successUrl;

    @Value("${stripe.cancel-url}")
    private String cancelUrl;

    private final PendingPaymentRepository pendingRepo;
    private final PricingPlanRepository planRepo;
    private final ReductionRepository reductionRepo;
    private final SubscriptionRepository subscriptionRepo;
    private final UserRepository userRepo;
    private final LoyaltyAccountRepository loyaltyRepo;
    private final PointTransactionRepository txRepo;

    public StripePaymentServiceImpl(PendingPaymentRepository pendingRepo,
                                    PricingPlanRepository planRepo,
                                    ReductionRepository reductionRepo,
                                    SubscriptionRepository subscriptionRepo,
                                    UserRepository userRepo,
                                    LoyaltyAccountRepository loyaltyRepo,
                                    PointTransactionRepository txRepo) {
        this.pendingRepo = pendingRepo;
        this.planRepo = planRepo;
        this.reductionRepo = reductionRepo;
        this.subscriptionRepo = subscriptionRepo;
        this.userRepo = userRepo;
        this.loyaltyRepo = loyaltyRepo;
        this.txRepo = txRepo;
    }

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
    }

    @Override
    public PaymentInitResponse initiatePayment(PaymentInitRequest request) {
        // 1. Récupérer le plan
        PricingPlan plan = planRepo.findById(request.getPricingPlanId())
                .orElseThrow(() -> new RuntimeException("Plan introuvable"));

        // 2. Calcul montant avec réduction éventuelle
        double montant = plan.getPrix();
        if (request.getCodeReduction() != null
                && !request.getCodeReduction().isBlank()) {
            Reduction red = reductionRepo
                    .findByCode(request.getCodeReduction().toUpperCase())
                    .orElseThrow(() -> new RuntimeException("Code invalide"));
            montant = montant - (montant * red.getPourcentage() / 100);
        }

        // 3. Créer session Stripe Checkout
        try {
            long montantCentimes = Math.round(montant * 100);

            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl(cancelUrl)
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency("eur")
                                                    .setUnitAmount(montantCentimes)
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData
                                                                    .ProductData.builder()
                                                                    .setName("Abonnement : " + plan.getNom())
                                                                    .setDescription(plan.getDescription())
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    )
                    .build();

            Session session = Session.create(params);

            // 4. Sauvegarder paiement en attente
            PendingPayment pending = new PendingPayment();
            pending.setStripeSessionId(session.getId());
            pending.setPassengerId(request.getPassengerId());
            pending.setPricingPlanId(request.getPricingPlanId());
            pending.setCodeReduction(request.getCodeReduction());
            pending.setMontantDT(montant);
            pendingRepo.save(pending);

            // 5. Retourner l'URL checkout
            PaymentInitResponse response = new PaymentInitResponse();
            response.setCheckoutUrl(session.getUrl());
            response.setSessionId(session.getId());
            response.setMontantDT(montant);
            response.setPlanNom(plan.getNom());
            return response;

        } catch (Exception e) {
            throw new RuntimeException("Erreur Stripe : " + e.getMessage());
        }
    }

    @Override
    public SubscriptionResponse confirmPayment(String sessionId) {
        try {
            // 1. Vérifier le statut chez Stripe
            Session session = Session.retrieve(sessionId);
            if (!"complete".equals(session.getStatus())
                    && !"paid".equals(session.getPaymentStatus())) {
                throw new RuntimeException("Paiement non confirmé par Stripe");
            }

            // 2. Récupérer le paiement en attente
            PendingPayment pending = pendingRepo
                    .findByStripeSessionId(sessionId)
                    .orElseThrow(() -> new RuntimeException(
                            "Session introuvable : " + sessionId));

            if (pending.getStatus() == PendingPayment.PaymentStatus.SUCCESS) {
                throw new RuntimeException("Ce paiement a déjà été traité.");
            }

            // 3. Créer la subscription
            User passenger = userRepo.findById(pending.getPassengerId())
                    .orElseThrow(() -> new RuntimeException("Passenger introuvable"));
            PricingPlan plan = planRepo.findById(pending.getPricingPlanId())
                    .orElseThrow(() -> new RuntimeException("Plan introuvable"));

            LocalDate debut = LocalDate.now();
            LocalDate fin = debut.plusMonths(plan.getDureeEnMois());
            Subscription sub = new Subscription(
                    debut, fin, SubscriptionStatus.ACTIVE, passenger, plan);
            subscriptionRepo.save(sub);

            // 4. Générer les points loyalty automatiquement
            int points = (int) Math.floor(pending.getMontantDT());
            LoyaltyAccount account = loyaltyRepo
                    .findByPassengerId(pending.getPassengerId())
                    .orElseGet(() -> loyaltyRepo.save(new LoyaltyAccount(passenger)));
            account.setPointsCumules(account.getPointsCumules() + points);
            account.setNiveau(calculerTier(account.getPointsCumules()));
            loyaltyRepo.save(account);

            txRepo.save(new PointTransaction(
                    points, TransactionType.EARNED,
                    LocalDateTime.now(),
                    "Paiement Stripe — " + plan.getNom(),
                    account));

            // 5. Marquer le paiement comme SUCCESS
            pending.setStatus(PendingPayment.PaymentStatus.SUCCESS);
            pendingRepo.save(pending);

            return SubscriptionResponse.fromEntity(sub);

        } catch (Exception e) {
            throw new RuntimeException("Erreur confirmation Stripe : " + e.getMessage());
        }
    }

    private LoyaltyTier calculerTier(int pts) {
        if (pts >= 500) return LoyaltyTier.GOLD;
        if (pts >= 200) return LoyaltyTier.SILVER;
        return LoyaltyTier.BRONZE;
    }
}