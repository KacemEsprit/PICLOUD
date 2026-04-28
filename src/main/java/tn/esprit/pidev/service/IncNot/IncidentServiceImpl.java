package tn.esprit.pidev.service.IncNot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.pidev.dto.IncNot.IncidentNotificationDTO;
import tn.esprit.pidev.entity.Incident;
import tn.esprit.pidev.entity.RoleEnum;
import tn.esprit.pidev.entity.User;
import tn.esprit.pidev.exception.InvalidFileException;
import tn.esprit.pidev.repository.IncidentRepository;
import tn.esprit.pidev.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class IncidentServiceImpl implements IncidentService {

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AIIncidentService aiIncidentService;

    // ✅ converter — Incident entity → IncidentNotificationDTO
    private IncidentNotificationDTO convertToDTO(Incident incident) {
        return new IncidentNotificationDTO(
                incident.getTitle(),
                incident.getSeverity(),
                incident.getLocation(),
                incident.getReportedBy() != null ? incident.getReportedBy().getName() : null,
                incident.getEstimatedDelayMinutes(),
                incident.getConfidencePercent(),
                incident.getIncidentType()
        );
    }

    @Override
    public IncidentNotificationDTO saveIncident(Incident incident, String agentUsername) {
        User agent = userRepository.findByUsername(agentUsername)
                .orElseThrow(() -> new InvalidFileException("User not found: " + agentUsername));

        if (agent.getRole() != RoleEnum.AGENT) {
            throw new InvalidFileException("Only users with role AGENT can submit incidents");
        }

        incident.setReportedBy(agent);

        // Ignore any client-provided severity: AI will determine it
        incident.setSeverity(null);

        // Enrich incident using AI model (non-blocking fallback)
        AIIncidentService.AIIncidentAnalysis analysis = null;
        try {
            analysis = aiIncidentService.analyzeIncident(incident.getTitle(), incident.getDescription());
            if (analysis != null) {
                if (analysis.getSeverity() != null) {
                    incident.setSeverity(analysis.getSeverity());
                }
                try { incident.setEstimatedDelayMinutes(analysis.getEstimatedDelayMinutes()); } catch (Exception ignored) {}
                try { incident.setConfidencePercent(analysis.getConfidencePercent()); } catch (Exception ignored) {}
                try { incident.setIncidentType(analysis.getIncidentType()); } catch (Exception ignored) {}
            }
        } catch (Exception e) {
            // Keep original severity if AI fails; do not interrupt flow
        }

        Incident savedIncident = incidentRepository.save(incident);

        // ✅ Build DTO with only important fields
        IncidentNotificationDTO dto = new IncidentNotificationDTO(
                savedIncident.getTitle(),
                savedIncident.getSeverity(),
                savedIncident.getLocation(),
                agent.getName()
        );

        // Attach AI enrichment to DTO for the frontend
        if (analysis != null) {
            try {
                dto.setEstimatedDelayMinutes(analysis.getEstimatedDelayMinutes());
            } catch (Exception ignored) {}
            try {
                dto.setConfidencePercent(analysis.getConfidencePercent());
            } catch (Exception ignored) {}
            try {
                dto.setIncidentType(analysis.getIncidentType());
            } catch (Exception ignored) {}
        }

        // ✅ Pass DTO to notification service (use full dto so passengers receive severity)
        notificationService.sendInternalNotificationsToAgents(dto);
        notificationService.sendExternalNotificationsToAgents(dto);
        notificationService.sendDelayNotificationsToPassengers(dto);

        return dto; // return full AI result for the create response
    }

    @Override
    public void deleteIncident(Long id) {
        incidentRepository.deleteById(id);
    }

    @Override
    public IncidentNotificationDTO getIncidentById(Long id) {
        Incident incident = incidentRepository.findById(id).orElse(null);
        return incident != null ? convertToDTO(incident) : null;
    }

    @Override
    public List<IncidentNotificationDTO> getAllIncidents() {
        return incidentRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}