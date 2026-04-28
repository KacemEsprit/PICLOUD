package tn.esprit.pidev.service.IncNot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.pidev.dto.IncNot.IncidentNotificationDTO;
import tn.esprit.pidev.entity.*;
import tn.esprit.pidev.repository.NotificationRepository;
import tn.esprit.pidev.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger logger = Logger.getLogger(NotificationServiceImpl.class.getName());

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MailingService mailingService;

    @Override
    public Notification saveNotification(Notification notification) {
        return notificationRepository.save(notification);
    }

    @Override
    public void deleteNotification(Long id) {
        notificationRepository.deleteById(id);
    }

    @Override
    public Notification getNotificationById(Long id) {
        return notificationRepository.findById(id).orElse(null);
    }

    @Override
    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }

    // ✅ INTERNAL — save notification in DB for each AGENT
    @Override
    public void sendInternalNotificationsToAgents(IncidentNotificationDTO dto) {
        List<User> agents = userRepository.findByRole(RoleEnum.AGENT);
        for (User agent : agents) {
            String details = "Location: " + dto.getLocation() +
                            " | Severity: " + dto.getSeverity() +
                            " | Reported by: " + dto.getReportedByName();
            if (dto.getEstimatedDelayMinutes() != null) {
                details += " | Est. Delay: " + dto.getEstimatedDelayMinutes() + " min";
            }
            if (dto.getConfidencePercent() != null) {
                details += " | Confidence: " + dto.getConfidencePercent() + "%";
            }
            if (dto.getIncidentType() != null) {
                details += " | Type: " + dto.getIncidentType();
            }

            Notification notification = new Notification(
                    "🚨 New Incident: " + dto.getTitle(),
                    details,
                    NotifStatusEnum.SENT,
                    agent,
                    LocalDateTime.now()
            );
            notificationRepository.save(notification);
            logger.info("Internal notification saved for agent: " + agent.getUsername());
        }
    }

    // ✅ EXTERNAL — send email to each AGENT
    @Override
    public void sendExternalNotificationsToAgents(IncidentNotificationDTO dto) {
        List<User> agents = userRepository.findByRole(RoleEnum.AGENT);
        for (User agent : agents) {
            // Do not reveal severity to agents via email — send a copy with severity null
            IncidentNotificationDTO agentDto = new IncidentNotificationDTO(
                    dto.getTitle(),
                    null,
                    dto.getLocation(),
                    dto.getReportedByName(),
                    dto.getEstimatedDelayMinutes(),
                    dto.getConfidencePercent(),
                    dto.getIncidentType()
            );
            mailingService.sendIncidentNotificationEmail(
                    agent.getEmail(),
                    agent.getName(),
                    agentDto
            );
        }
    }

    // ✅ INTERNAL + EXTERNAL — notify each PASSENGER
    @Override
    public void sendDelayNotificationsToPassengers(IncidentNotificationDTO dto) {
        List<User> passengers = userRepository.findByRole(RoleEnum.PASSENGER);
        for (User passenger : passengers) {
            String details = "Delay at: " + dto.getLocation() + " | Severity: " + dto.getSeverity();
            if (dto.getEstimatedDelayMinutes() != null) {
                details += " | Est. Delay: " + dto.getEstimatedDelayMinutes() + " min";
            }
            if (dto.getConfidencePercent() != null) {
                details += " | Confidence: " + dto.getConfidencePercent() + "%";
            }
            Notification notification = new Notification(
                    "⚠️ Transport Delay",
                    details,
                    NotifStatusEnum.SENT,
                    passenger,
                    LocalDateTime.now()
            );
            notificationRepository.save(notification);
            logger.info("Internal delay notification saved for passenger: " + passenger.getUsername());

            mailingService.sendTransportDelayEmail(
                    passenger.getEmail(),
                    passenger.getName(),
                    dto
            );
        }
    }

    // ✅ Get notifications by User object (not String)
    @Override
    public List<Notification> getNotificationsForUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        return notificationRepository.findByUser(user);
    }

    // ✅ Mark as READ
    @Override
    public Notification markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));
        notification.setStatus(NotifStatusEnum.READ);
        notification.setReadAt(LocalDateTime.now());
        return notificationRepository.save(notification);
    }
}