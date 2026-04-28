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
                logger.warn("⚠️ Email service not configured. Password reset token for user '{}': {}", username, resetToken);
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
            logger.info("✓ Password reset email sent to: {}", toEmail);

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
                logger.warn("⚠️ Email service not configured. Password changed for user: {}", username);
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
            logger.info("✓ Password change confirmation email sent to: {}", toEmail);

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
                "                                <p style=\"margin: 0 0 10px 0; font-size: 13px; font-weight: 600; color: #6b7280;\">Or copy this link ↓</p>\n" +
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
                "                                            <p style=\"margin: 0; font-size: 13px; color: #92400e;\"><strong style=\"color: #d97706;\">⏱️ Time Limit:</strong> This link will expire in 24 hours.</p>\n" +
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
                "                                            <p style=\"margin: 0; font-size: 13px; color: #1e40af;\"><strong style=\"color: #2563eb;\">🔒 Security:</strong> If you didn't request this, ignore this email. Your account is secure.</p>\n" +
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
}


