package tn.esprit.pidev.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pidev.dto.PricingPlanRequest;
import tn.esprit.pidev.dto.PricingPlanResponse;
import tn.esprit.pidev.entity.PricingPlan;
import tn.esprit.pidev.entity.PricingType;
import tn.esprit.pidev.entity.RoleEnum;
import tn.esprit.pidev.entity.User;
import tn.esprit.pidev.repository.PricingPlanRepository;
import tn.esprit.pidev.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PricingPlanServiceImpl implements IPricingPlanService {

    private static final Logger logger = LoggerFactory.getLogger(PricingPlanServiceImpl.class);

    private final PricingPlanRepository planRepo;
    private final UserRepository userRepo;

    public PricingPlanServiceImpl(PricingPlanRepository planRepo, UserRepository userRepo) {
        this.planRepo = planRepo;
        this.userRepo = userRepo;
    }

    @Override
    public PricingPlanResponse create(PricingPlanRequest request, Long operatorId) {
        validateOperatorRole(operatorId);
        User operator = getOperatorUser(operatorId);
        PricingPlan plan = new PricingPlan();
        plan.setNom(request.getNom());
        plan.setDescription(request.getDescription());
        plan.setPrix(request.getPrix());
        plan.setDureeEnMois(request.getDureeEnMois());
        plan.setType(request.getType());
        plan.setCreatedBy(operator);
        return PricingPlanResponse.fromEntity(planRepo.save(plan));
    }

    @Override
    @Transactional(readOnly = true)
    public PricingPlanResponse getById(Long id) {
        return PricingPlanResponse.fromEntity(findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PricingPlanResponse> getAll() {
        return planRepo.findAll().stream()
                .map(PricingPlanResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public PricingPlanResponse update(Long id, PricingPlanRequest request, Long operatorId) {
        validateOperatorRole(operatorId);
        PricingPlan plan = findById(id);
        plan.setNom(request.getNom());
        plan.setDescription(request.getDescription());
        plan.setPrix(request.getPrix());
        plan.setDureeEnMois(request.getDureeEnMois());
        plan.setType(request.getType());
        return PricingPlanResponse.fromEntity(planRepo.save(plan));
    }

    @Override
    public void delete(Long id, Long operatorId) {
        validateOperatorRole(operatorId);
        findById(id);
        planRepo.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PricingPlanResponse> getByType(PricingType type) {
        return planRepo.findByType(type).stream()
                .map(PricingPlanResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PricingPlanResponse> getByMaxPrice(Double maxPrix) {
        return planRepo.findByPrixLessThanEqual(maxPrix).stream()
                .map(PricingPlanResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PricingPlanResponse> getByOperator(Long operatorId) {
        logger.info("Fetching pricing plans for operator: {}", operatorId);
        List<PricingPlanResponse> plans = planRepo.findByCreatedById(operatorId).stream()
                .map(PricingPlanResponse::fromEntity)
                .collect(Collectors.toList());
        logger.info("Found {} pricing plans for operator {}", plans.size(), operatorId);
        return plans;
    }

    // ===== Helpers =====

    private PricingPlan findById(Long id) {
        return planRepo.findById(id)
                .orElseThrow(() -> {
                    logger.warn("PricingPlan not found: {}", id);
                    return new RuntimeException("PricingPlan introuvable : " + id);
                });
    }

    /**
     * Validates that the user calling this operation has OPERATOR or ADMIN role.
     * Returns the operator user if valid, otherwise throws an exception.
     */
    private void validateOperatorRole(Long operatorId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : "UNKNOWN";

        logger.debug("Validating operator role for user: {} on operatorId: {}", username, operatorId);

        User user = userRepo.findById(operatorId)
                .orElseThrow(() -> {
                    logger.error("User not found: {} (attempt by: {})", operatorId, username);
                    return new RuntimeException("Utilisateur introuvable : " + operatorId);
                });

        if (user.getRole() != RoleEnum.OPERATOR && user.getRole() != RoleEnum.ADMIN) {
            logger.error("Access denied: User {} tried to manage plans for user {}, but {} has role {}",
                    username, operatorId, user.getUsername(), user.getRole());
            throw new RuntimeException("Accès refusé : seuls les OPERATOR et ADMIN peuvent gérer les plans (user has role: " + user.getRole() + ")");
        }

        logger.info("Operator role validation passed for user: {} with operatorId: {}", username, operatorId);
    }

    private User getOperatorUser(Long operatorId) {
        return userRepo.findById(operatorId)
                .orElseThrow(() -> {
                    logger.error("Operator user not found: {}", operatorId);
                    return new RuntimeException("Utilisateur introuvable : " + operatorId);
                });
    }
}

