package tn.esprit.pidev.dto.IncNot;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class IncidentNotificationDTO {
    @NotBlank(message = "Le titre de l'incident est obligatoire")
    @Size(min = 3, max = 100, message = "Le titre doit contenir entre 3 et 100 caractères")
    private String title;

    @NotNull(message = "La sévérité est obligatoire")
    private String severity; // ou String si c'est un enum

    @NotBlank(message = "La localisation est obligatoire")
    @Size(max = 255, message = "La localisation ne peut pas dépasser 255 caractères")
    private String location;

    @Size(max = 100, message = "Le nom du déclarant ne peut pas dépasser 100 caractères")
    private String reportedByName;

    // AI enrichment fields
    private Integer estimatedDelayMinutes;
    private Double confidencePercent;
    private String incidentType;

    public IncidentNotificationDTO() {}

    public IncidentNotificationDTO(String title, String severity,
                                   String location, String reportedByName) {
        this.title = title;
        this.severity = severity;
        this.location = location;
        this.reportedByName = reportedByName;
    }

    public IncidentNotificationDTO(String title, String severity,
                                   String location, String reportedByName,
                                   Integer estimatedDelayMinutes, Double confidencePercent,
                                   String incidentType) {
        this.title = title;
        this.severity = severity;
        this.location = location;
        this.reportedByName = reportedByName;
        this.estimatedDelayMinutes = estimatedDelayMinutes;
        this.confidencePercent = confidencePercent;
        this.incidentType = incidentType;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getReportedByName() { return reportedByName; }
    public void setReportedByName(String reportedByName) { this.reportedByName = reportedByName; }

    public Integer getEstimatedDelayMinutes() { return estimatedDelayMinutes; }
    public void setEstimatedDelayMinutes(Integer estimatedDelayMinutes) { this.estimatedDelayMinutes = estimatedDelayMinutes; }

    public Double getConfidencePercent() { return confidencePercent; }
    public void setConfidencePercent(Double confidencePercent) { this.confidencePercent = confidencePercent; }

    public String getIncidentType() { return incidentType; }
    public void setIncidentType(String incidentType) { this.incidentType = incidentType; }
}