package tn.esprit.pidev.service;

import org.springframework.stereotype.Service;
import tn.esprit.pidev.dto.ReductionRequest;
import tn.esprit.pidev.dto.ReductionResponse;
import tn.esprit.pidev.entity.Reduction;
import tn.esprit.pidev.entity.RoleEnum;
import tn.esprit.pidev.entity.User;
import tn.esprit.pidev.repository.ReductionRepository;
import tn.esprit.pidev.repository.UserRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReductionServiceImpl implements IReductionService {

    private final ReductionRepository reductionRepo;
    private final UserRepository userRepo;

    public ReductionServiceImpl(ReductionRepository reductionRepo, UserRepository userRepo) {
        this.reductionRepo = reductionRepo;
        this.userRepo = userRepo;
    }

    @Override
    public ReductionResponse create(ReductionRequest request, Long operatorId) {
        User operator = getOperator(operatorId);
        Reduction r = new Reduction(
                request.getCode().toUpperCase(),
                request.getPourcentage(),
                request.getDateExpiration(),
                request.getPointsRequis(),
                operator
        );
        return ReductionResponse.fromEntity(reductionRepo.save(r));
    }

    @Override
    public ReductionResponse getById(Long id) {
        return ReductionResponse.fromEntity(findById(id));
    }

    @Override
    public ReductionResponse getByCode(String code) {
        return ReductionResponse.fromEntity(
                reductionRepo.findByCode(code.toUpperCase())
                        .orElseThrow(() -> new RuntimeException("Réduction introuvable : " + code))
        );
    }

    @Override
    public List<ReductionResponse> getAll() {
        return reductionRepo.findAll().stream()
                .map(ReductionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public ReductionResponse update(Long id, ReductionRequest request) {
        Reduction r = findById(id);
        r.setCode(request.getCode().toUpperCase());
        r.setPourcentage(request.getPourcentage());
        r.setDateExpiration(request.getDateExpiration());
        r.setPointsRequis(request.getPointsRequis());
        return ReductionResponse.fromEntity(reductionRepo.save(r));
    }

    @Override
    public void delete(Long id) {
        findById(id);
        reductionRepo.deleteById(id);
    }

    @Override
    public List<ReductionResponse> getValides() {
        return reductionRepo.findByDateExpirationAfter(LocalDate.now()).stream()
                .map(ReductionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReductionResponse> getAccessibles(Integer points) {
        return reductionRepo.findByPointsRequisLessThanEqual(points).stream()
                .map(ReductionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReductionResponse> getByOperator(Long operatorId) {
        return reductionRepo.findByCreatedById(operatorId).stream()
                .map(ReductionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    private Reduction findById(Long id) {
        return reductionRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Réduction introuvable : " + id));
    }

    private User getOperator(Long operatorId) {
        User user = userRepo.findById(operatorId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable : " + operatorId));
        if (user.getRole() != RoleEnum.OPERATOR && user.getRole() != RoleEnum.ADMIN) {
            throw new RuntimeException("Accès refusé : seuls les OPERATOR et ADMIN peuvent gérer les réductions.");
        }
        return user;
    }
}
