package tn.esprit.pidev.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ── 1. Email sent right after subscription with auto-renewal enabled (via payment) ──
    public void sendAutoRenewalActivatedEmail(String toEmail, String passengerName,
                                              String planName, LocalDate dateFin) {
        String subject = "TransitTN — Auto-renewal activated for your subscription";
        String body = baseLayout(
                "Auto-renewal activated",
                passengerName,
                "<p>Your subscription to the <strong>" + planName + "</strong> plan is now active.</p>"
                        + "<p>You have enabled <strong>auto-renewal</strong>. Your subscription will be automatically "
                        + "renewed on <strong>" + dateFin.format(FMT) + "</strong>.</p>"
                        + "<p>You will receive reminder emails 7 days and 1 day before the renewal date.</p>"
                        + "<p>To disable auto-renewal at any time, visit your subscriptions page:</p>"
                        + "<a href='" + frontendUrl + "/passenger/subscriptions' class='btn'>Manage my subscriptions</a>"
        );
        send(toEmail, subject, body);
    }

    // ── 2. Email when auto-renewal is toggled ON from the subscriptions list ──
    public void sendAutoRenewalEnabledEmail(String toEmail, String passengerName,
                                            String planName, LocalDate dateFin) {
        String subject = "TransitTN — Auto-renewal enabled";
        String body = baseLayout(
                "Auto-renewal enabled",
                passengerName,
                "<p>Auto-renewal has been <strong>enabled</strong> for your <strong>" + planName + "</strong> plan.</p>"
                        + "<p>Your subscription will be automatically renewed on <strong>" + dateFin.format(FMT) + "</strong>.</p>"
                        + "<p>You will receive reminder emails 7 days and 1 day before the renewal date.</p>"
                        + "<a href='" + frontendUrl + "/passenger/subscriptions' class='btn'>Manage my subscriptions</a>"
        );
        send(toEmail, subject, body);
    }

    // ── 3. Email when auto-renewal is toggled OFF from the subscriptions list ──
    public void sendAutoRenewalDisabledEmail(String toEmail, String passengerName,
                                             String planName, LocalDate dateFin) {
        String subject = "TransitTN — Auto-renewal disabled";
        String body = baseLayout(
                "Auto-renewal disabled",
                passengerName,
                "<p>Auto-renewal has been <strong>disabled</strong> for your <strong>" + planName + "</strong> plan.</p>"
                        + "<p>Your subscription will expire on <strong>" + dateFin.format(FMT) + "</strong> "
                        + "and will <strong>not</strong> be renewed automatically.</p>"
                        + "<p>You can re-enable it at any time from your subscriptions page:</p>"
                        + "<a href='" + frontendUrl + "/passenger/subscriptions' class='btn'>Manage my subscriptions</a>"
        );
        send(toEmail, subject, body);
    }

    // ── 4. Reminder email J-7 or J-1 ──
    public void sendRenewalReminderEmail(String toEmail, String passengerName,
                                         String planName, LocalDate dateFin,
                                         int daysLeft, boolean autoRenewEnabled) {
        String subject = "TransitTN — Your subscription expires in " + daysLeft + " day(s)";
        String renewalMsg = autoRenewEnabled
                ? "<div style='background:#e8f5e9;border-radius:8px;padding:12px;margin:16px 0;color:#2e7d32'>"
                + "<strong>✓ Auto-renewal is enabled.</strong> Your subscription will be renewed automatically.</div>"
                : "<div style='background:#fff8e1;border-radius:8px;padding:12px;margin:16px 0;color:#f57f17'>"
                + "<strong>⚠ Auto-renewal is disabled.</strong> "
                + "<a href='" + frontendUrl + "/passenger/subscriptions'>Enable it here</a> to avoid interruption.</div>";

        String body = baseLayout(
                "Subscription expiring soon",
                passengerName,
                "<p>Your subscription to the <strong>" + planName + "</strong> plan expires on "
                        + "<strong>" + dateFin.format(FMT) + "</strong> (in <strong>" + daysLeft + " day(s)</strong>).</p>"
                        + renewalMsg
                        + "<a href='" + frontendUrl + "/passenger/subscriptions' class='btn'>View my subscriptions</a>"
        );
        send(toEmail, subject, body);
    }

    // ── 5. Confirmation email after successful auto-renewal ──
    public void sendRenewalConfirmationEmail(String toEmail, String passengerName,
                                             String planName, LocalDate newDateFin) {
        String subject = "TransitTN — Your subscription has been renewed";
        String body = baseLayout(
                "Subscription renewed",
                passengerName,
                "<p>Your subscription to the <strong>" + planName + "</strong> plan has been successfully renewed.</p>"
                        + "<p>New expiry date: <strong>" + newDateFin.format(FMT) + "</strong>.</p>"
                        + "<a href='" + frontendUrl + "/passenger/subscriptions' class='btn'>View my subscriptions</a>"
        );
        send(toEmail, subject, body);
    }

    // ── 6. Already used — kept for StripePaymentServiceImpl compatibility ──
    public void sendAutoRenewalStatusChangedEmail(String toEmail, String passengerName,
                                                  String planName, LocalDate dateFin,
                                                  boolean enabled, String subscriptionsUrl) {
        if (enabled) {
            sendAutoRenewalEnabledEmail(toEmail, passengerName, planName, dateFin);
        } else {
            sendAutoRenewalDisabledEmail(toEmail, passengerName, planName, dateFin);
        }
    }



    // ── Add this method in EmailService.java (before the send() helper) ──

    public void sendActionPromoEmail(String toEmail, String passengerName,
                                     String promoCode, double discountPct,
                                     String actionMessage, LocalDate expiryDate) {
        String subject = "TransitTN — An exclusive offer just for you 🎁";
        String body = baseLayout(
                "Your personal promo code",
                passengerName,
                "<p>" + actionMessage + "</p>"
                        + "<p>As a valued member of TransitTN, we are pleased to offer you an exclusive discount:</p>"

                        // Big promo code display
                        + "<div style='text-align:center;margin:24px 0'>"
                        + "  <div style='display:inline-block;background:#e3f2fd;border:2px dashed #1a73e8;"
                        + "       border-radius:12px;padding:16px 32px'>"
                        + "    <div style='font-size:0.8rem;color:#555;margin-bottom:6px;letter-spacing:1px'>YOUR PROMO CODE</div>"
                        + "    <div style='font-size:2rem;font-weight:700;font-family:monospace;color:#1a237e;letter-spacing:4px'>"
                        +        promoCode
                        + "    </div>"
                        + "    <div style='font-size:1.1rem;font-weight:600;color:#2e7d32;margin-top:8px'>-" + (int) discountPct + "% discount</div>"
                        + "  </div>"
                        + "</div>"

                        + "<div style='background:#fff8e1;border-radius:8px;padding:12px 16px;margin-bottom:16px'>"
                        + "  <i>⏰ Valid until <strong>" + expiryDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "</strong> "
                        + "  (7 days only)</i>"
                        + "</div>"

                        + "<p>Apply this code when subscribing to any available plan:</p>"
                        + "<a href='" + frontendUrl + "/passenger/plans' class='btn'>Browse plans &amp; subscribe</a>"

                        + "<p style='margin-top:20px;font-size:0.8rem;color:#999'>"
                        + "This code was generated exclusively for your account and can only be used once.</p>"
        );
        send(toEmail, subject, body);
    }

    // ── Internal send helper ──────────────────────────────────────────────────
    private void send(String to, String subject, String htmlBody) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(msg);
            log.info("Email sent to {} — {}", to, subject);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    // ── HTML layout ───────────────────────────────────────────────────────────
    private String baseLayout(String title, String name, String content) {
        return """
            <div style="font-family:sans-serif;max-width:540px;margin:0 auto;background:#fff;border-radius:12px;overflow:hidden;border:1px solid #e0e0e0">
              <div style="background:#1a237e;padding:20px 28px;display:flex;align-items:center;gap:12px">
                <span style="font-size:22px">🚌</span>
                <span style="color:#fff;font-size:1.2rem;font-weight:700">TransitTN</span>
              </div>
              <div style="padding:28px">
                <h2 style="color:#1a237e;margin-bottom:8px">%s</h2>
                <p>Hello <strong>%s</strong>,</p>
                %s
              </div>
              <div style="background:#f5f5f5;padding:14px 28px;font-size:0.78rem;color:#999;text-align:center">
                TransitTN — Please do not reply to this email.
              </div>
            </div>
            <style>.btn{display:inline-block;margin-top:16px;padding:10px 20px;background:#1a237e;color:#fff;border-radius:8px;text-decoration:none;font-weight:600}</style>
            """.formatted(title, name, content);
    }


    // ── Password reset email ──────────────────────────────────────────────────
    public void sendPasswordResetEmail(String toEmail, String username, String resetToken) {
        String resetUrl = frontendUrl + "/reset-password?token=" + resetToken;
        String subject  = "TransitTN — Password reset request";
        String body = baseLayout(
                "Reset your password",
                username,
                "<p>We received a request to reset your password.</p>"
                        + "<p>Click the button below to set a new password. This link expires in <strong>24 hours</strong>.</p>"
                        + "<a href='" + resetUrl + "' class='btn'>Reset my password</a>"
                        + "<p style='margin-top:16px;font-size:0.82rem;color:#999'>"
                        + "If you did not request this, you can safely ignore this email.</p>"
        );
        send(toEmail, subject, body);
    }

    // ── Password change confirmation email ───────────────────────────────────
    public void sendPasswordChangeConfirmationEmail(String toEmail, String username) {
        String subject = "TransitTN — Password changed successfully";
        String body = baseLayout(
                "Password updated",
                username,
                "<p>Your password has been successfully changed.</p>"
                        + "<p>If you did not make this change, please contact us immediately.</p>"
                        + "<a href='" + frontendUrl + "/login' class='btn'>Sign in</a>"
        );
        send(toEmail, subject, body);
    }
}
