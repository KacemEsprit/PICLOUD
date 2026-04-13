package tn.esprit.pidev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.pidev.entity.Subscription;
import tn.esprit.pidev.entity.SubscriptionStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findByPassengerId(Long passengerId);
    List<Subscription> findByStatut(SubscriptionStatus statut);
    Optional<Subscription> findByPassengerIdAndStatut(Long passengerId, SubscriptionStatus statut);
    List<Subscription> findByPricingPlanId(Long planId);
}
