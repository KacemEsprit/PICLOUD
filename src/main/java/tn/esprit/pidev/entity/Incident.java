package tn.esprit.pidev.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Incident")
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private String severity;
    private String location;

    @ManyToOne
    @JoinColumn(name = "agent_id", nullable = false)
    private User reportedBy;

    private LocalDateTime createdAt;

    // AI enrichment fields
    private Integer estimatedDelayMinutes;
    private Double confidencePercent;
    private String incidentType;

    public Incident() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public User getReportedBy() { return reportedBy; }
    public void setReportedBy(User reportedBy) { this.reportedBy = reportedBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Integer getEstimatedDelayMinutes() { return estimatedDelayMinutes; }
    public void setEstimatedDelayMinutes(Integer estimatedDelayMinutes) { this.estimatedDelayMinutes = estimatedDelayMinutes; }

    public Double getConfidencePercent() { return confidencePercent; }
    public void setConfidencePercent(Double confidencePercent) { this.confidencePercent = confidencePercent; }

    public String getIncidentType() { return incidentType; }
    public void setIncidentType(String incidentType) { this.incidentType = incidentType; }
}