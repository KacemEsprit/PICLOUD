package tn.esprit.pidev.service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false)
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username:noreply@transittn.tn}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    /**
     * Send contract expiry reminder email
     */
    @Async
    public void sendContractExpiryReminderEmail(String toEmail, String partnerName,
            String orgName, Long contractId, int daysLeft, java.util.Date endDate) {
        try {
            if (javaMailSender == null) {
                logger.warn("Email service not available for: {}", toEmail);
                return;
            }

            // For 3 days or less, send critical template
            if (daysLeft <= 3) {
                sendCriticalContractExpiryEmail(toEmail, partnerName, orgName, contractId, daysLeft, endDate);
                return;
            }

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("[TransitTN] Contract Expiry Reminder - " + daysLeft + " days remaining");

            String urgencyColor = daysLeft <= 7 ? "#d32f2f" : daysLeft <= 15 ? "#f57c00" : "#1a73e8";
            String urgencyLabel = daysLeft <= 7 ? "URGENT" : daysLeft <= 15 ? "IMPORTANT" : "REMINDER";
            String dateStr = new java.text.SimpleDateFormat("dd/MM/yyyy").format(endDate);

            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\"></head>");
            html.append("<body style=\"font-family:Arial,sans-serif;margin:0;padding:0;background:#f5f5f5;\">");
            html.append("<div style=\"max-width:600px;margin:30px auto;background:white;border-radius:12px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,0.1);\">");
            html.append("<div style=\"background:linear-gradient(135deg,#1a237e,#1a73e8);padding:30px;text-align:center;\">");
            html.append("<div style=\"display:inline-block;background:white;padding:12px 20px;border-radius:8px;margin-bottom:15px;\">");
            html.append("<span style=\"color:#1a237e;font-size:1.5rem;font-weight:bold;letter-spacing:2px;\">TransitTN</span></div>");
            html.append("<h1 style=\"color:white;margin:0;font-size:1.4rem;\">National Tunisian Transport Platform</h1>");
            html.append("<p style=\"color:rgba(255,255,255,0.9);margin:8px 0 0;font-size:0.9rem;\">Contract Management System</p></div>");
            html.append("<div style=\"background:").append(urgencyColor).append(";padding:15px;text-align:center;\">");
            html.append("<span style=\"color:white;font-weight:bold;font-size:1.1rem;letter-spacing:1px;\">").append(urgencyLabel).append(" - CONTRACT EXPIRING SOON</span></div>");
            html.append("<div style=\"padding:30px;\">");
            html.append("<p style=\"font-size:1rem;color:#333;\">Dear <strong>").append(partnerName).append("</strong>,</p>");
            html.append("<p style=\"color:#555;line-height:1.6;\">We would like to inform you that your partnership contract with <strong>").append(orgName);
            html.append("</strong> will expire in <strong style=\"color:").append(urgencyColor).append(";font-size:1.1rem;\">").append(daysLeft).append(" days</strong>.</p>");
            html.append("<div style=\"background:#f8f9ff;border-left:4px solid ").append(urgencyColor).append(";border-radius:8px;padding:20px;margin:20px 0;\">");
            html.append("<h3 style=\"color:#1a237e;margin:0 0 15px;font-size:1.1rem;\">Contract Details</h3>");
            html.append("<table style=\"width:100%;border-collapse:collapse;font-size:0.95rem;\">");
            html.append("<tr><td style=\"padding:8px 0;color:#666;width:45%;\">Contract ID</td><td style=\"font-weight:bold;color:#333;\">#").append(contractId).append("</td></tr>");
            html.append("<tr><td style=\"padding:8px 0;color:#666;\">Organization</td><td style=\"font-weight:bold;color:#333;\">").append(orgName).append("</td></tr>");
            html.append("<tr><td style=\"padding:8px 0;color:#666;\">Partner</td><td style=\"font-weight:bold;color:#333;\">").append(partnerName).append("</td></tr>");
            html.append("<tr><td style=\"padding:8px 0;color:#666;\">Expiration Date</td><td style=\"font-weight:bold;color:").append(urgencyColor).append(";\">").append(dateStr).append("</td></tr>");
            html.append("<tr><td style=\"padding:8px 0;color:#666;\">Days Remaining</td><td style=\"font-weight:bold;color:").append(urgencyColor).append(";\">").append(daysLeft).append(" days</td></tr>");
            html.append("</table></div>");
            html.append("<div style=\"background:#fff8e1;border:1px solid #ffe082;border-radius:8px;padding:15px;margin:20px 0;\">");
            html.append("<p style=\"color:#e65100;margin:0;font-size:0.9rem;\"><strong>Action Required:</strong> Please contact TransitTN to renew your contract before the expiration date.</p></div>");
            html.append("<div style=\"text-align:center;margin:25px 0;\">");
            html.append("<a href=\"").append(frontendUrl).append("/admin/contracts\" style=\"background:linear-gradient(135deg,#1a73e8,#0d47a1);color:white;padding:14px 35px;border-radius:25px;text-decoration:none;font-weight:bold;display:inline-block;\">Renew Contract Now</a></div>");
            html.append("<p style=\"color:#888;font-size:0.85rem;text-align:center;border-top:1px solid #eee;padding-top:20px;margin-bottom:0;\">");
            html.append("Automated email from TransitTN.<br>&copy; 2026 TransitTN - National Tunisian Transport Platform</p>");
            html.append("</div></div></body></html>");

            helper.setText(html.toString(), true);
            javaMailSender.send(message);
            logger.info("Expiry reminder sent to: {} ({} days left)", toEmail, daysLeft);
        } catch (Exception e) {
            logger.error("Error sending expiry reminder: {}", e.getMessage());
        }
    }

    /**
     * Send CRITICAL contract expiry email (3 days or less)
     */
    public void sendCriticalContractExpiryEmail(String toEmail, String partnerName,
            String orgName, Long contractId, int daysLeft, java.util.Date endDate) {
        try {
            if (javaMailSender == null) return;

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("[CRITICAL] Contract Expiring in " + daysLeft + " Days - Immediate Action Required");

            String dateStr = new java.text.SimpleDateFormat("dd/MM/yyyy").format(endDate);

            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\"></head>");
            html.append("<body style=\"font-family:Arial,sans-serif;margin:0;padding:0;background:#f5f5f5;\">");
            html.append("<div style=\"max-width:600px;margin:30px auto;background:white;border-radius:12px;overflow:hidden;box-shadow:0 4px 30px rgba(183,28,28,0.3);\">");
            html.append("<div style=\"background:linear-gradient(135deg,#b71c1c,#d32f2f);padding:35px;text-align:center;\">");
            html.append("<div style=\"display:inline-block;background:white;padding:15px 25px;border-radius:10px;margin-bottom:15px;\">");
            html.append("<span style=\"color:#b71c1c;font-size:1.8rem;font-weight:bold;letter-spacing:2px;\">TransitTN</span></div>");
            html.append("<h1 style=\"color:white;margin:0;font-size:1.8rem;letter-spacing:1px;\">URGENT ACTION REQUIRED</h1>");
            html.append("<p style=\"color:rgba(255,255,255,0.9);margin:10px 0 0;\">Contract expires in ").append(daysLeft).append(" day(s)</p></div>");
            html.append("<div style=\"background:#1a237e;padding:12px;text-align:center;\">");
            html.append("<span style=\"color:white;font-weight:bold;\">TransitTN - National Tunisian Transport Platform</span></div>");
            html.append("<div style=\"padding:35px;\">");
            html.append("<p style=\"font-size:1.05rem;color:#333;\">Dear <strong style=\"color:#b71c1c;\">").append(partnerName).append("</strong>,</p>");
            html.append("<div style=\"background:#ffebee;border:2px solid #ffcdd2;border-radius:10px;padding:20px;margin:20px 0;\">");
            html.append("<p style=\"color:#b71c1c;margin:0;font-weight:bold;font-size:1.05rem;\">CRITICAL WARNING</p>");
            html.append("<p style=\"color:#c62828;margin:8px 0 0;line-height:1.6;\">Your partnership contract with <strong>").append(orgName);
            html.append("</strong> expires in <strong>").append(daysLeft).append(" day(s)</strong>. Without immediate renewal, your TransitTN services will be <strong>automatically suspended</strong>.</p></div>");
            html.append("<div style=\"background:#f8f9ff;border-left:5px solid #b71c1c;border-radius:8px;padding:20px;margin:20px 0;\">");
            html.append("<h3 style=\"color:#1a237e;margin:0 0 15px;\">Contract #").append(contractId).append(" Details</h3>");
            html.append("<table style=\"width:100%;border-collapse:collapse;\">");
            html.append("<tr><td style=\"padding:8px 0;color:#666;width:45%;\">Organization</td><td style=\"font-weight:bold;color:#333;\">").append(orgName).append("</td></tr>");
            html.append("<tr><td style=\"padding:8px 0;color:#666;\">Partner</td><td style=\"font-weight:bold;color:#333;\">").append(partnerName).append("</td></tr>");
            html.append("<tr><td style=\"padding:8px 0;color:#666;\">Expiration Date</td><td style=\"font-weight:bold;color:#b71c1c;\">").append(dateStr).append("</td></tr>");
            html.append("<tr><td style=\"padding:8px 0;color:#666;\">Days Remaining</td><td style=\"font-weight:bold;color:#b71c1c;font-size:1.2rem;\">").append(daysLeft).append(" day(s)</td></tr>");
            html.append("</table></div>");
            html.append("<div style=\"background:#fff9c4;border:1px solid #fff176;border-radius:8px;padding:18px;margin:20px 0;\">");
            html.append("<p style=\"color:#f57f17;margin:0 0 10px;font-weight:bold;\">Consequences of Non-Renewal:</p>");
            html.append("<ul style=\"margin:0;padding-left:20px;color:#555;line-height:1.8;\">");
            html.append("<li>Suspension of TransitTN services</li>");
            html.append("<li>Contract status: EXPIRED</li>");
            html.append("<li>Removal from active partners list</li>");
            html.append("<li>Need to sign a new contract</li></ul></div>");
            html.append("<div style=\"text-align:center;margin:30px 0;\">");
            html.append("<a href=\"").append(frontendUrl).append("/admin/contracts\" style=\"background:linear-gradient(135deg,#b71c1c,#d32f2f);color:white;padding:16px 40px;border-radius:30px;text-decoration:none;font-weight:bold;display:inline-block;margin-right:10px;\">Renew Now</a>");
            html.append("<a href=\"mailto:contact@transittn.tn\" style=\"background:white;color:#1a237e;padding:16px 40px;border:2px solid #1a237e;border-radius:30px;text-decoration:none;font-weight:bold;display:inline-block;\">Contact TransitTN</a></div>");
            html.append("<p style=\"color:#888;font-size:0.85rem;text-align:center;border-top:1px solid #eee;padding-top:20px;margin-bottom:0;\">");
            html.append("TransitTN - Tunisian Startup<br>National platform for public transport management<br>");
            html.append("&copy; 2026 TransitTN - All rights reserved</p>");
            html.append("</div></div></body></html>");

            helper.setText(html.toString(), true);
            javaMailSender.send(message);
            logger.info("CRITICAL expiry email sent to: {} ({} days left)", toEmail, daysLeft);
        } catch (Exception e) {
            logger.error("Error sending critical expiry email: {}", e.getMessage());
        }
    }

    /**
     * Send password reset email
     */
    public void sendPasswordResetEmail(String toEmail, String username, String resetToken) {
        try {
            if (javaMailSender == null) {
                logger.warn("Email service not available. Reset link for {}: {}", toEmail, resetToken);
                return;
            }
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("[TransitTN] Password Reset Request");

            String html = "<!DOCTYPE html><html><body style=\"font-family:Arial,sans-serif;background:#f5f5f5;padding:30px;\">"
                + "<div style=\"max-width:600px;margin:auto;background:white;border-radius:12px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,0.1);\">"
                + "<div style=\"background:linear-gradient(135deg,#1a237e,#1a73e8);padding:30px;text-align:center;\">"
                + "<h1 style=\"color:white;margin:0;\">TransitTN</h1>"
                + "<p style=\"color:rgba(255,255,255,0.9);margin:5px 0 0;\">Password Reset Request</p></div>"
                + "<div style=\"padding:30px;\">"
                + "<p>Hello,</p>"
                + "<p>You requested to reset your password. Click the button below to proceed:</p>"
                + "<div style=\"text-align:center;margin:25px 0;\">"
                + "<a href=\"" + frontendUrl + "/reset-password?token=" + resetToken + "\" style=\"background:linear-gradient(135deg,#1a73e8,#0d47a1);color:white;padding:14px 35px;border-radius:25px;text-decoration:none;font-weight:bold;display:inline-block;\">Reset Password</a></div>"
                + "<p style=\"color:#666;font-size:0.9rem;\">If you did not request this, please ignore this email.</p>"
                + "<p style=\"color:#888;font-size:0.85rem;text-align:center;border-top:1px solid #eee;padding-top:20px;\">&copy; 2026 TransitTN</p>"
                + "</div></div></body></html>";

            helper.setText(html, true);
            javaMailSender.send(message);
            logger.info("Password reset email sent to: {}", toEmail);
        } catch (Exception e) {
            logger.error("Error sending password reset email: {}", e.getMessage());
        }
    }

    /**
     * Send password change confirmation email
     */
    public void sendPasswordChangeConfirmationEmail(String toEmail, String username) {
        try {
            if (javaMailSender == null) return;
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("[TransitTN] Password Changed Successfully");

            String html = "<!DOCTYPE html><html><body style=\"font-family:Arial,sans-serif;background:#f5f5f5;padding:30px;\">"
                + "<div style=\"max-width:600px;margin:auto;background:white;border-radius:12px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,0.1);\">"
                + "<div style=\"background:linear-gradient(135deg,#1b5e20,#2e7d32);padding:30px;text-align:center;\">"
                + "<h1 style=\"color:white;margin:0;\">TransitTN</h1>"
                + "<p style=\"color:rgba(255,255,255,0.9);margin:5px 0 0;\">Password Changed</p></div>"
                + "<div style=\"padding:30px;\">"
                + "<p>Hello,</p>"
                + "<p>Your password has been changed successfully.</p>"
                + "<div style=\"background:#e8f5e9;border:1px solid #c8e6c9;border-radius:8px;padding:15px;margin:20px 0;\">"
                + "<p style=\"color:#1b5e20;margin:0;font-weight:bold;\">If you did not make this change, please contact us immediately.</p></div>"
                + "<p style=\"color:#888;font-size:0.85rem;text-align:center;border-top:1px solid #eee;padding-top:20px;\">&copy; 2026 TransitTN</p>"
                + "</div></div></body></html>";

            helper.setText(html, true);
            javaMailSender.send(message);
            logger.info("Password change confirmation sent to: {}", toEmail);
        } catch (Exception e) {
            logger.error("Error sending password change email: {}", e.getMessage());
        }
    }
}


