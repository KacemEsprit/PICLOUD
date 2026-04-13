package tn.esprit.pidev.service;

import org.springframework.stereotype.Service;
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

    private final PricingPlanRepository planRepo;
    private final UserRepository userRepo;

    public PricingPlanServiceImpl(PricingPlanRepository planRepo, UserRepository userRepo) {
        this.planRepo = planRepo;
        this.userRepo = userRepo;
    }

    @Override
    public PricingPlanResponse create(PricingPlanRequest request, Long operatorId) {
        User operator = getOperator(operatorId);
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
    public PricingPlanResponse getById(Long id) {
        return PricingPlanResponse.fromEntity(findById(id));
    }

    @Override
    public List<PricingPlanResponse> getAll() {
        return planRepo.findAll().stream()
                .map(PricingPlanResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public PricingPlanResponse update(Long id, PricingPlanRequest request, Long operatorId) {
        getOperator(operatorId); // vérification du rôle
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
        getOperator(operatorId);
        findById(id);
        planRepo.deleteById(id);
    }

    @Override
    public List<PricingPlanResponse> getByType(PricingType type) {
        return planRepo.findByType(type).stream()
                .map(PricingPlanResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<PricingPlanResponse> getByMaxPrice(Double maxPrix) {
        return planRepo.findByPrixLessThanEqual(maxPrix).stream()
                .map(PricingPlanResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<PricingPlanResponse> getByOperator(Long operatorId) {
        return planRepo.findByCreatedById(operatorId).stream()
                .map(PricingPlanResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // ===== Helpers =====
    private PricingPlan findById(Long id) {
        return planRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("PricingPlan introuvable : " + id));
    }

    private User getOperator(Long operatorId) {
        User user = userRepo.findById(operatorId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable : " + operatorId));
        if (user.getRole() != RoleEnum.OPERATOR && user.getRole() != RoleEnum.ADMIN) {
            throw new RuntimeException("Accès refusé : seuls les OPERATOR et ADMIN peuvent gérer les plans.");
        }
        return user;
    }
}
