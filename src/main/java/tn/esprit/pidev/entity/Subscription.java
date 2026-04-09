package tn.esprit.pidev.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "subscription")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate dateDebut;

    @Column(nullable = false)
    private LocalDate dateFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus statut;

    // Lien avec User (Passenger qui a souscrit)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "passenger_id", nullable = false)
    private User passenger;

    // Lien avec le plan souscrit
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "pricing_plan_id", nullable = false)
    private PricingPlan pricingPlan;

    // Constructors
    public Subscription() {}

    public Subscription(LocalDate dateDebut, LocalDate dateFin, SubscriptionStatus statut,
                        User passenger, PricingPlan pricingPlan) {
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.statut = statut;
        this.passenger = passenger;
        this.pricingPlan = pricingPlan;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }
    public LocalDate getDateFin() { return dateFin; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }
    public SubscriptionStatus getStatut() { return statut; }
    public void setStatut(SubscriptionStatus statut) { this.statut = statut; }
    public User getPassenger() { return passenger; }
    public void setPassenger(User passenger) { this.passenger = passenger; }
    public PricingPlan getPricingPlan() { return pricingPlan; }
    public void setPricingPlan(PricingPlan pricingPlan) { this.pricingPlan = pricingPlan; }
}
