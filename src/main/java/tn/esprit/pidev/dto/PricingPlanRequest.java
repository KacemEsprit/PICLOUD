package tn.esprit.pidev.dto;

import tn.esprit.pidev.entity.PricingType;

// ===== REQUEST DTO (Operator → créer/modifier un plan) =====
public class PricingPlanRequest {

    private String nom;
    private String description;
    private Double prix;
    private Integer dureeEnMois;
    private PricingType type;

    public PricingPlanRequest() {}

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Double getPrix() { return prix; }
    public void setPrix(Double prix) { this.prix = prix; }
    public Integer getDureeEnMois() { return dureeEnMois; }
    public void setDureeEnMois(Integer dureeEnMois) { this.dureeEnMois = dureeEnMois; }
    public PricingType getType() { return type; }
    public void setType(PricingType type) { this.type = type; }
}
