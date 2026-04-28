package tn.esprit.pidev.controller.IncNot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pidev.dto.IncNot.IncidentNotificationDTO;
import tn.esprit.pidev.entity.Incident;
import tn.esprit.pidev.service.IncNot.IncidentService;

import java.util.List;

@RestController
@RequestMapping("/incidents")
public class IncidentController {

    @Autowired
    private IncidentService incidentService;

    @PostMapping("/add")
    public IncidentNotificationDTO createIncident(@RequestBody Incident incident,
                                                  Authentication authentication) {
        return incidentService.saveIncident(incident, authentication.getName());
    }

    @PutMapping("/update/{id}")
    public IncidentNotificationDTO updateIncident(@PathVariable Long id,
                                                  @RequestBody Incident incident,
                                                  Authentication authentication) {
        incident.setId(id);
        return incidentService.saveIncident(incident, authentication.getName());
    }

    @DeleteMapping("/delete/{id}")
    public void deleteIncident(@PathVariable Long id) {
        incidentService.deleteIncident(id);
    }

    @GetMapping("/get/{id}")
    public IncidentNotificationDTO getIncidentById(@PathVariable Long id,
                                                   Authentication authentication) {
        IncidentNotificationDTO dto = incidentService.getIncidentById(id);
        if (dto != null && authentication != null) {
            boolean isAgent = authentication.getAuthorities().stream()
                    .anyMatch(a -> "ROLE_AGENT".equals(a.getAuthority()));
            if (isAgent) {
                dto.setSeverity(null);
            }
        }
        return dto;
    }

    @GetMapping
    public List<IncidentNotificationDTO> getAllIncidents(Authentication authentication) {
        List<IncidentNotificationDTO> list = incidentService.getAllIncidents();
        if (authentication != null) {
            boolean isAgent = authentication.getAuthorities().stream()
                    .anyMatch(a -> "ROLE_AGENT".equals(a.getAuthority()));
            if (isAgent) {
                for (IncidentNotificationDTO dto : list) {
                    dto.setSeverity(null);
                }
            }
        }
        return list;
    }
}