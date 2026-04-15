package tn.esprit.pidev.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * Service for sending emails (password reset, notifications, etc.)
 */
@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false)
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username:noreply@pidev.com}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    /**
     * Send password reset email with reset link
     * @param toEmail Recipient email
     * @param username User's username
     * @param resetToken Reset token
     */
    public void sendPasswordResetEmail(String toEmail, String username, String resetToken) {
        try {
            if (javaMailSender == null) {
                logger.warn("Ã¢ÂÂ Ã¯Â¸Â Email service not configured. Password reset token for user '{}': {}", username, resetToken);
                return;
            }

            String resetLink = frontendUrl + "/reset-password?token=" + resetToken;

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Password Reset Request - PIDEV");
            
            // Add headers to improve email deliverability
            message.setHeader("X-Mailer", "PIDEV");
            message.setHeader("X-Priority", "3");
            message.setHeader("X-MSMail-Priority", "Normal");
            message.setHeader("Importance", "Normal");

            String htmlContent = buildPasswordResetEmailContent(username, resetLink);
            helper.setText(htmlContent, true); // true indicates HTML content

            javaMailSender.send(message);
            logger.info("Ã¢ÂÂ Password reset email sent to: {}", toEmail);

        } catch (MessagingException e) {
            logger.error("Failed to send password reset email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    /**
     * Send password change confirmation email
     * @param toEmail Recipient email
     * @param username User's username
     */
    public void sendPasswordChangeConfirmationEmail(String toEmail, String username) {
        try {
            if (javaMailSender == null) {
                logger.warn("Ã¢ÂÂ Ã¯Â¸Â Email service not configured. Password changed for user: {}", username);
                return;
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Password Change Confirmation - PIDEV");
            message.setText("Hi " + username + ",\n\n" +
                    "Your password has been successfully changed.\n\n" +
                    "If you did not make this change, please contact support immediately.\n\n" +
                    "Best regards,\n" +
                    "PIDEV Team");

            javaMailSender.send(message);
            logger.info("Ã¢ÂÂ Password change confirmation email sent to: {}", toEmail);

        } catch (Exception e) {
            logger.error("Failed to send password change confirmation email to: {}", toEmail, e);
        }
    }

    /**
     * Build HTML content for password reset email
     * Simplified to avoid Gmail spam filters while maintaining professional appearance
     */
    private String buildPasswordResetEmailContent(String username, String resetLink) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "</head>\n" +
                "<body style=\"margin: 0; padding: 0; background-color: #f0f2f5; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;\">\n" +
                "    <center style=\"width: 100%; table-layout: fixed; background-color: #f0f2f5;\">\n" +
                "        <table align=\"center\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"width: 100%; max-width: 600px; background-color: #ffffff; border-collapse: collapse; mso-table-lspace: 0; mso-table-rspace: 0;\">\n" +
                "            <tr>\n" +
                "                <td style=\"padding: 0;\">\n" +
                "                    <!-- Header -->\n" +
                "                    <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"background-color: #3b3b8f; border-collapse: collapse;\">\n" +
                "                        <tr>\n" +
                "                            <td align=\"center\" style=\"padding: 40px 30px;\">\n" +
                "                                <h1 style=\"margin: 0 0 8px 0; font-size: 28px; font-weight: 700; color: #ffffff;\">Password Reset</h1>\n" +
                "                                <p style=\"margin: 0; font-size: 16px; color: #c7d2fe;\">Secure your account</p>\n" +
                "                            </td>\n" +
                "                        </tr>\n" +
                "                    </table>\n" +
                "                    \n" +
                "                    <!-- Content -->\n" +
                "                    <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse: collapse;\">\n" +
                "                        <tr>\n" +
                "                            <td style=\"padding: 30px 30px 20px 30px;\">\n" +
                "                                <p style=\"margin: 0 0 20px 0; font-size: 16px; line-height: 1.5; color: #1f2937;\">Hello <strong>" + username + "</strong>,</p>\n" +
                "                                <p style=\"margin: 0 0 25px 0; font-size: 15px; line-height: 1.6; color: #4b5563;\">We received a request to reset the password for your PIDEV account. Click the button below to create a new password:</p>\n" +
                "                            </td>\n" +
                "                        </tr>\n" +
                "                        \n" +
                "                        <!-- Button -->\n" +
                "                        <tr>\n" +
                "                            <td align=\"center\" style=\"padding: 0 30px 20px 30px;\">\n" +
                "                                <table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"margin: 0 auto; border-collapse: collapse;\">\n" +
                "                                    <tr>\n" +
                "                                        <td align=\"center\" bgcolor=\"#4f46e5\" style=\"padding: 14px 40px; border-radius: 8px; background-color: #4f46e5;\">\n" +
                "                                            <a href=\"" + resetLink + "\" style=\"display: inline-block; color: #ffffff; font-size: 16px; font-weight: 600; text-decoration: none; line-height: 1.4;\">Reset Password</a>\n" +
                "                                        </td>\n" +
                "                                    </tr>\n" +
                "                                </table>\n" +
                "                            </td>\n" +
                "                        </tr>\n" +
                "                        \n" +
                "                        <tr>\n" +
                "                            <td style=\"padding: 0 30px 15px 30px;\">\n" +
                "                                <p style=\"margin: 0 0 10px 0; font-size: 13px; font-weight: 600; color: #6b7280;\">Or copy this link Ã¢ÂÂ</p>\n" +
                "                                <p style=\"margin: 0; padding: 12px; background-color: #f3f4f6; border-left: 4px solid #4f46e5; font-size: 12px; word-break: break-all; color: #374151; font-family: monospace;\">" + resetLink + "</p>\n" +
                "                            </td>\n" +
                "                        </tr>\n" +
                "                        \n" +
                "                        <!-- Time Limit Warning -->\n" +
                "                        <tr>\n" +
                "                            <td style=\"padding: 15px 30px 0 30px;\">\n" +
                "                                <table width=\"100%\" cellpadding=\"12\" cellspacing=\"0\" border=\"0\" style=\"background-color: #fffbeb; border-left: 4px solid #f59e0b; border-collapse: collapse;\">\n" +
                "                                    <tr>\n" +
                "                                        <td style=\"padding: 12px;\">\n" +
                "                                            <p style=\"margin: 0; font-size: 13px; color: #92400e;\"><strong style=\"color: #d97706;\">Ã¢ÂÂ±Ã¯Â¸Â Time Limit:</strong> This link will expire in 24 hours.</p>\n" +
                "                                        </td>\n" +
                "                                    </tr>\n" +
                "                                </table>\n" +
                "                            </td>\n" +
                "                        </tr>\n" +
                "                        \n" +
                "                        <!-- Security Notice -->\n" +
                "                        <tr>\n" +
                "                            <td style=\"padding: 15px 30px 0 30px;\">\n" +
                "                                <table width=\"100%\" cellpadding=\"12\" cellspacing=\"0\" border=\"0\" style=\"background-color: #eff6ff; border-left: 4px solid #3b82f6; border-collapse: collapse;\">\n" +
                "                                    <tr>\n" +
                "                                        <td style=\"padding: 12px;\">\n" +
                "                                            <p style=\"margin: 0; font-size: 13px; color: #1e40af;\"><strong style=\"color: #2563eb;\">Ã°ÂÂÂ Security:</strong> If you didn't request this, ignore this email. Your account is secure.</p>\n" +
                "                                        </td>\n" +
                "                                    </tr>\n" +
                "                                </table>\n" +
                "                            </td>\n" +
                "                        </tr>\n" +
                "                        \n" +
                "                        <!-- Footer -->\n" +
                "                        <tr>\n" +
                "                            <td style=\"padding: 30px 30px 40px 30px;\">\n" +
                "                                <hr style=\"border: none; border-top: 1px solid #e5e7eb; margin: 0 0 25px 0;\">\n" +
                "                                <p style=\"margin: 0 0 5px 0; text-align: center; font-size: 13px; font-weight: 600; color: #4f46e5;\">PIDEV</p>\n" +
                "                                <p style=\"margin: 0 0 5px 0; text-align: center; font-size: 12px; color: #6b7280;\">Security Team</p>\n" +
                "                                <p style=\"margin: 15px 0 0 0; text-align: center; font-size: 11px; color: #9ca3af;\">This is an automated email. Please do not reply.</p>\n" +
                "                            </td>\n" +
                "                        </tr>\n" +
                "                    </table>\n" +
                "                </td>\n" +
                "            </tr>\n" +
                "        </table>\n" +
                "    </center>\n" +
                "</body>\n" +
                "</html>";
    }
    /**
     * Send contract expiry reminder email
     */
    public void sendContractExpiryReminderEmail(String toEmail, String partnerName,
            String orgName, Long contractId, int daysLeft, java.util.Date endDate) {
        try {
            if (javaMailSender == null) {
                logger.warn("Email service not available. Contract expiry reminder for: {}", toEmail);
                return;
            }

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("?? Rappel : Contrat expirant dans " + daysLeft + " jours - TransitTN");

            String urgencyColor = daysLeft <= 3 ? "#b71c1c" : daysLeft <= 7 ? "#d32f2f" : daysLeft <= 15 ? "#f57c00" : "#1a73e8";
            String urgencyLabel = daysLeft <= 3 ? "CRITIQUE - DERNIÃRE CHANCE" : daysLeft <= 7 ? "URGENT" : daysLeft <= 15 ? "IMPORTANT" : "RAPPEL";
            
            // Special template for 3 days
            if (daysLeft <= 3) {
                sendCriticalContractExpiryEmail(toEmail, partnerName, orgName, contractId, daysLeft, endDate);
                return;
            }

            String html = """
                <!DOCTYPE html>
                <html>
                <head><meta charset="UTF-8"></head>
                <body style="font-family:Arial,sans-serif; margin:0; padding:0; background:#f5f5f5;">
                  <div style="max-width:600px; margin:30px auto; background:white; border-radius:12px; overflow:hidden; box-shadow:0 4px 20px rgba(0,0,0,0.1);">
                    
                    <!-- Header -->
                    <div style="background:linear-gradient(135deg,#1a237e,#1a73e8); padding:30px; text-align:center;">
                      <div style="font-size:2rem; margin-bottom:8px;">??</div>
                      <h1 style="color:white; margin:0; font-size:1.5rem;">TransitTN</h1>
                      <p style="color:rgba(255,255,255,0.8); margin:5px 0 0;">Plateforme de Transport Tunisien</p>
                    </div>

                    <!-- Alert Banner -->
                    <div style="background:%s; padding:15px; text-align:center;">
                      <span style="color:white; font-weight:bold; font-size:1.1rem;">?? %s - CONTRAT EXPIRANT BIENTÃT</span>
                    </div>

                    <!-- Content -->
                    <div style="padding:30px;">
                      <p style="font-size:1rem; color:#333;">Bonjour <strong>%s</strong>,</p>
                      <p style="color:#555;">Nous vous informons que votre contrat de partenariat avec <strong>%s</strong> expire dans <strong style="color:%s;">%d jours</strong>.</p>

                      <!-- Contract Info Box -->
                      <div style="background:#f8f9ff; border-left:4px solid %s; border-radius:8px; padding:20px; margin:20px 0;">
                        <h3 style="color:#1a237e; margin:0 0 15px;">?? DÃ©tails du Contrat</h3>
                        <table style="width:100%%; border-collapse:collapse;">
                          <tr><td style="padding:8px 0; color:#666; width:40%%;">?? ID Contrat:</td><td style="font-weight:bold; color:#333;">#%d</td></tr>
                          <tr><td style="padding:8px 0; color:#666;">?? Organisation:</td><td style="font-weight:bold; color:#333;">%s</td></tr>
                          <tr><td style="padding:8px 0; color:#666;">?? Partenaire:</td><td style="font-weight:bold; color:#333;">%s</td></tr>
                          <tr><td style="padding:8px 0; color:#666;">?? Date d'expiration:</td><td style="font-weight:bold; color:%s;">%s</td></tr>
                          <tr><td style="padding:8px 0; color:#666;">? Jours restants:</td><td style="font-weight:bold; color:%s;">%d jours</td></tr>
                        </table>
                      </div>

                      <!-- Action Button -->
                      <div style="text-align:center; margin:25px 0;">
                        <a href="%s/admin/contracts" 
                           style="background:linear-gradient(135deg,#1a73e8,#0d47a1); color:white; padding:14px 35px; border-radius:25px; text-decoration:none; font-weight:bold; font-size:1rem; display:inline-block;">
                          ?? Renouveler le Contrat
                        </a>
                      </div>

                      <p style="color:#888; font-size:0.85rem; text-align:center; border-top:1px solid #eee; padding-top:20px;">
                        Cet email a Ã©tÃ© envoyÃ© automatiquement par TransitTN.<br>
                        &copy; 2026 TransitTN - Plateforme de Transport Tunisien
                      </p>
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(
                    urgencyColor, urgencyLabel,
                    partnerName, orgName, urgencyColor, daysLeft,
                    urgencyColor,
                    contractId, orgName, partnerName,
                    urgencyColor, new java.text.SimpleDateFormat("dd/MM/yyyy").format(endDate),
                    urgencyColor, daysLeft,
                    frontendUrl
                );

            helper.setText(html, true);
            javaMailSender.send(message);
            logger.info("Contract expiry reminder sent to: {} ({} days left)", toEmail, daysLeft);

        } catch (Exception e) {
            logger.error("Error sending contract expiry reminder: {}", e.getMessage());
        }
    }

    /**
     * Send CRITICAL contract expiry email (3 days or less)
     * Special personalized template for TransitTN startup
     */
    public void sendCriticalContractExpiryEmail(String toEmail, String partnerName,
            String orgName, Long contractId, int daysLeft, java.util.Date endDate) {
        try {
            if (javaMailSender == null) {
                logger.warn("Email service not available. Critical reminder for: {}", toEmail);
                return;
            }

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("?? ACTION REQUISE : Votre contrat expire dans " + daysLeft + " jour(s) - TransitTN");

            String daysText = daysLeft == 1 ? "DEMAIN" : "dans " + daysLeft + " jours";
            String dateFormatted = new java.text.SimpleDateFormat("dd/MM/yyyy").format(endDate);

            String html = """
                <!DOCTYPE html>
                <html>
                <head><meta charset="UTF-8"></head>
                <body style="font-family:Arial,sans-serif; margin:0; padding:0; background:#1a0000;">
                  <div style="max-width:650px; margin:20px auto; border-radius:16px; overflow:hidden; box-shadow:0 8px 40px rgba(0,0,0,0.4);">

                    <!-- Critical Header -->
                    <div style="background:linear-gradient(135deg,#b71c1c,#d32f2f); padding:0; position:relative; overflow:hidden;">
                      <div style="background:rgba(0,0,0,0.1); padding:40px 30px; text-align:center;">
                        <div style="font-size:4rem; margin-bottom:10px;">??</div>
                        <h1 style="color:white; margin:0; font-size:1.8rem; font-weight:800; letter-spacing:2px;">
                          ACTION URGENTE REQUISE
                        </h1>
                        <div style="background:rgba(255,255,255,0.2); border-radius:25px; padding:8px 25px; display:inline-block; margin-top:15px;">
                          <span style="color:white; font-size:1.1rem; font-weight:700;">? Expire %s</span>
                        </div>
                      </div>
                    </div>

                    <!-- TransitTN Branding -->
                    <div style="background:#1a237e; padding:15px 30px; display:flex; align-items:center; gap:15px;">
                      <div style="font-size:1.8rem;">??</div>
                      <div>
                        <div style="color:white; font-weight:800; font-size:1.1rem;">TransitTN</div>
                        <div style="color:rgba(255,255,255,0.7); font-size:0.8rem;">Plateforme Nationale de Transport Tunisien</div>
                      </div>
                      <div style="margin-left:auto; background:#d32f2f; color:white; padding:4px 15px; border-radius:20px; font-size:0.85rem; font-weight:700;">
                        CRITIQUE
                      </div>
                    </div>

                    <!-- Main Content -->
                    <div style="background:white; padding:35px 30px;">
                      
                      <p style="font-size:1.1rem; color:#333; margin-bottom:20px;">
                        Cher(e) <strong style="color:#b71c1c;">%s</strong>,
                      </p>

                      <div style="background:#fff8f8; border:2px solid #ffcdd2; border-radius:12px; padding:20px; margin-bottom:25px;">
                        <p style="color:#c62828; font-weight:600; margin:0 0 10px; font-size:1rem;">
                          ?? Votre contrat de partenariat avec <strong>%s</strong> expire <strong>%s</strong>.
                        </p>
                        <p style="color:#555; margin:0; font-size:0.95rem;">
                          Sans renouvellement immÃ©diat, votre accÃ¨s aux services TransitTN sera automatiquement suspendu 
                          et votre partenariat sera marquÃ© comme <strong>EXPIRÃ</strong>.
                        </p>
                      </div>

                      <!-- Contract Details -->
                      <div style="background:#f8f9ff; border-left:5px solid #b71c1c; border-radius:0 12px 12px 0; padding:20px; margin-bottom:25px;">
                        <h3 style="color:#1a237e; margin:0 0 15px; font-size:1rem;">?? DÃ©tails du Contrat #%d</h3>
                        <div style="display:grid; grid-template-columns:1fr 1fr; gap:10px;">
                          <div>
                            <div style="color:#999; font-size:0.75rem; text-transform:uppercase; font-weight:600;">Organisation</div>
                            <div style="font-weight:700; color:#333;">%s</div>
                          </div>
                          <div>
                            <div style="color:#999; font-size:0.75rem; text-transform:uppercase; font-weight:600;">Partenaire</div>
                            <div style="font-weight:700; color:#333;">%s</div>
                          </div>
                          <div>
                            <div style="color:#999; font-size:0.75rem; text-transform:uppercase; font-weight:600;">Date Expiration</div>
                            <div style="font-weight:700; color:#b71c1c;">%s</div>
                          </div>
                          <div>
                            <div style="color:#999; font-size:0.75rem; text-transform:uppercase; font-weight:600;">Jours Restants</div>
                            <div style="font-weight:700; color:#b71c1c; font-size:1.2rem;">%d jour(s) ?</div>
                          </div>
                        </div>
                      </div>

                      <!-- What happens if not renewed -->
                      <div style="background:#fff3cd; border:1px solid #ffc107; border-radius:8px; padding:15px; margin-bottom:25px;">
                        <h4 style="color:#856404; margin:0 0 10px;">?? ConsÃ©quences si non renouvelÃ© :</h4>
                        <ul style="color:#664d03; margin:0; padding-left:20px; font-size:0.9rem;">
                          <li>AccÃ¨s aux services TransitTN suspendu</li>
                          <li>Statut du contrat ? EXPIRED</li>
                          <li>Retrait de la liste des partenaires actifs</li>
                          <li>NÃ©cessitÃ© de signer un nouveau contrat</li>
                        </ul>
                      </div>

                      <!-- CTA Buttons -->
                      <div style="text-align:center; margin:30px 0 20px;">
                        <a href="%s/admin/contracts"
                           style="background:linear-gradient(135deg,#b71c1c,#d32f2f); color:white; padding:16px 40px; border-radius:30px; text-decoration:none; font-weight:800; font-size:1rem; display:inline-block; box-shadow:0 4px 15px rgba(183,28,28,0.4); margin-right:10px;">
                          ?? Renouveler Maintenant
                        </a>
                        <a href="mailto:%s"
                           style="background:white; color:#1a237e; padding:16px 40px; border-radius:30px; text-decoration:none; font-weight:600; font-size:1rem; display:inline-block; border:2px solid #1a237e;">
                          ?? Contacter TransitTN
                        </a>
                      </div>

                      <!-- Footer -->
                      <div style="border-top:2px solid #f0f0f0; margin-top:25px; padding-top:20px; text-align:center;">
                        <div style="color:#1a237e; font-weight:700; margin-bottom:5px;">?? TransitTN - Startup Tunisienne</div>
                        <div style="color:#888; font-size:0.8rem;">
                          Plateforme nationale de gestion du transport public<br>
                          Cet email a Ã©tÃ© envoyÃ© automatiquement Â Ne pas rÃ©pondre directement<br>
                          &copy; 2026 TransitTN. Tous droits rÃ©servÃ©s.
                        </div>
                      </div>
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(
                    daysText,
                    partnerName,
                    orgName, daysText,
                    contractId,
                    orgName, partnerName,
                    dateFormatted, daysLeft,
                    frontendUrl, fromEmail
                );

            helper.setText(html, true);
            javaMailSender.send(message);
            logger.info("CRITICAL contract expiry email sent to: {} ({} days left)", toEmail, daysLeft);

        } catch (Exception e) {
            logger.error("Error sending critical contract expiry email: {}", e.getMessage());
        }
    }
}


