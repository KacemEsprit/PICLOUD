package tn.esprit.pidev.dto;

import tn.esprit.pidev.entity.PricingPlan;
import tn.esprit.pidev.entity.PricingType;

// ===== RESPONSE DTO (retourné au client) =====
public class PricingPlanResponse {

    private Long id;
    private String nom;
    private String description;
    private Double prix;
    private Integer dureeEnMois;
    private PricingType type;
    private String createdByUsername;

    public PricingPlanResponse() {}

    // Constructeur depuis entité
    public static PricingPlanResponse fromEntity(PricingPlan p) {
        PricingPlanResponse dto = new PricingPlanResponse();
        dto.setId(p.getId());
        dto.setNom(p.getNom());
        dto.setDescription(p.getDescription());
        dto.setPrix(p.getPrix());
        dto.setDureeEnMois(p.getDureeEnMois());
        dto.setType(p.getType());
        if (p.getCreatedBy() != null) {
            dto.setCreatedByUsername(p.getCreatedBy().getUsername());
        }
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public String getCreatedByUsername() { return createdByUsername; }
    public void setCreatedByUsername(String createdByUsername) { this.createdByUsername = createdByUsername; }
}
