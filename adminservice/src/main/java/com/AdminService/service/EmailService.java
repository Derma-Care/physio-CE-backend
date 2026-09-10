package com.AdminService.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final String fromAddress;
    private final String clinicLoginUrl;
    private final String defaultBrandName;

    public EmailService(JavaMailSender mailSender, Environment env) {
        this.mailSender = mailSender;
        this.fromAddress = env.getProperty(
                "notification.default-from-email",
                "no-reply@glowkart.com"
        );
        this.clinicLoginUrl = env.getProperty(
                "notification.clinic-login-url",
                "https://clinic.ccmstestserver.online"
        );
        // Only used as a last-resort fallback when a clinic name isn't supplied
        this.defaultBrandName = env.getProperty(
                "notification.default-brand-name",
                "Kinetix Wellness Care"
        );
    }

    // ================= SEND EMAIL =================
    // Callers should put the clinic's actual name into data.put("clinicName", clinic.getName())
    // before calling this, so every template below can render it dynamically.
    public void sendEmail(String to, Map<String, String> data) {
        try {
            if (to == null || to.isBlank()) {
                logger.warn("Email not sent: recipient address is blank");
                return;
            }

            String clinicName = resolveClinicName(data);
            String subject = data.getOrDefault("subject", clinicName);

            // Emoji support
            String emoji = "";
            if (subject.contains("Verified")) emoji = "🎉";
            else if (subject.contains("Pending")) emoji = "⏳";
            else if (subject.contains("Review")) emoji = "🔍";
            else if (subject.contains("Rejected")) emoji = "❌";
            else if (subject.contains("OTP")) emoji = "🔒";

            // Fix subject branding — replace any generic "CCMS Notification" placeholder
            // with the actual clinic name instead of a fixed brand string
            subject = subject.replace("CCMS Notification", clinicName);

            String subjectWithEmoji = emoji.isEmpty() ? subject : emoji + " " + subject;

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(to);
            helper.setFrom(fromAddress);
            helper.setSubject(subjectWithEmoji);

            if (subject.contains("OTP")) {
                helper.setText(buildOtpMessageBody(data, clinicName), true);
            } else if (subject.contains("Rejected")) {
                helper.setText(buildRejectionMessageBody(data, clinicName), true);
            } else {
                helper.setText(buildMessageBody(data, clinicName), true);
            }

            mailSender.send(mimeMessage);
            logger.info("Email sent successfully to {}", to);

        } catch (Exception e) {
            logger.error("Failed to send email to {}: {}", to, e.getMessage(), e);
        }
    }

    // Small helper: every method below reads clinicName the same way, so if a caller
    // forgets to set it, we fall back to the default brand instead of silently
    // rendering "null" in the email.
    private String resolveClinicName(Map<String, String> data) {
        String clinicName = data.get("clinicName");
        return (clinicName != null && !clinicName.isBlank()) ? clinicName : defaultBrandName;
    }

    // ================= COMMON TEMPLATE =================
    private String buildMessageBody(Map<String, String> data, String clinicName) {

        String bodyMessage = data.getOrDefault("message", "");
        String username = data.get("username");
        String password = data.get("password");

        // Normalize Admin message to match Doctor format, using the real clinic name
        if (bodyMessage != null && bodyMessage.contains("clinic has been verified")) {
            String clinicId = username != null ? username : "";
            bodyMessage =
                    "Welcome to " + clinicName + "!\n\n" +
                    "Your account has been created successfully.\n" +
                    "Please use the below credentials to login.\n\n" +
                    "Clinic ID: " + clinicId;
        }

        if (bodyMessage != null) {
            // Remove any existing "Welcome..." line completely, then prepend
            // the dynamic clinic-name greeting
            bodyMessage = bodyMessage.replaceAll("Welcome to .*", "").trim();
            bodyMessage = "Welcome to " + clinicName + "!\n\n" + bodyMessage;
        }

        return """
            <html>
            <body style="margin:0; padding:0; font-family: Arial, sans-serif; background:#f5f7fa;">

            <div style="max-width:600px; margin:30px auto; background:#ffffff; border-radius:10px;
                        border:1px solid #e0e0e0; overflow:hidden;">

                <!-- Header -->
                <div style="background:linear-gradient(135deg, #0f2027, #203a43, #2c5364); padding:18px; text-align:center;">
                    <h2 style="color:#ffffff; margin:0;">%s</h2>
                </div>

                <!-- Body -->
                <div style="padding:20px; color:#333;">

                    <p>👋 Hello,</p>

                    <p style="line-height:1.6;">
                        %s
                    </p>

                    %s

                    %s

                    <p style="margin-top:20px;">
                        Regards,<br>
                        %s Team
                    </p>

                </div>

                <!-- Footer -->
                <div style="background:#f1f3f6; padding:12px; text-align:center; font-size:12px; color:#777;">
                    © 2026 %s. All rights reserved.
                </div>

            </div>

            </body>
            </html>
            """.formatted(

                clinicName,

                bodyMessage.replace("\n", "<br>"),

                (username != null && password != null)
                        ? """
                        <div style="background:#e8f0fe; padding:15px; border-radius:6px; margin-top:15px;">
                            <h3 style="margin-top:0; color:#1a73e8;">Login Credentials</h3>
                            <p><b>Username:</b> %s</p>
                            <p><b>Password:</b> %s</p>
                        </div>
                        """.formatted(username, password)
                        : "",

                (username != null && password != null)
                        ? """
                        <div style="text-align:center; margin-top:20px;">
                            <a href="%s"
                               style="background:#28a745; color:white; padding:10px 22px;
                                      border-radius:6px; text-decoration:none; font-weight:bold;">
                               Login Now
                            </a>
                        </div>
                        """.formatted(clinicLoginUrl)
                        : "",

                clinicName,
                clinicName
        );
    }

    // ================= OTP TEMPLATE =================
    private String buildOtpMessageBody(Map<String, String> data, String clinicName) {
        String bodyMessage = data.getOrDefault("message", "");

        return """
            <html>
            <body style="font-family: Arial, sans-serif; background:#f5f7fa; padding:20px;">
                <div style="max-width:600px; margin:auto; background:#fff; padding:20px; border-radius:10px;">
                    <h3 style="color:#0f2027;">🔒 %s OTP Verification</h3>
                    <p>%s</p>
                    <p style="font-size:24px; font-weight:bold; color:#28a745;">%s</p>
                    <p>This OTP is valid for 10 minutes.</p>
                </div>
            </body>
            </html>
            """.formatted(clinicName, bodyMessage, bodyMessage.replaceAll("\\D+", ""));
    }

    // ================= REJECTION TEMPLATE =================
    private String buildRejectionMessageBody(Map<String, String> data, String clinicName) {
        String bodyMessage = data.getOrDefault("message", "");
        String reason = data.getOrDefault("reason", "Not specified");

        return """
            <html>
            <body style="font-family: Arial, sans-serif; background:#f5f7fa; padding:20px;">
                <div style="max-width:600px; margin:auto; background:#fff; padding:20px; border-radius:10px;">
                    <h3 style="color:#d32f2f;">❌ %s - Rejected</h3>
                    <p>%s</p>
                    <p style="background:#fdecea; padding:10px; border-radius:5px;">
                        <b>Reason:</b> %s
                    </p>
                </div>
            </body>
            </html>
            """.formatted(clinicName, bodyMessage, reason);
    }

    // ================= FORGOT PASSWORD OTP =================
    // Kept as a dedicated public entry point since it takes otp/accountName directly
    // rather than a data map, but it now also accepts a dynamic clinic name.
    public void sendForgotPasswordOtp(String to, String otp, String accountName, String clinicName) {
        try {
            if (to == null || to.isBlank()) {
                logger.warn("Email not sent: recipient address is blank");
                return;
            }

            String resolvedClinicName = (clinicName != null && !clinicName.isBlank())
                    ? clinicName
                    : defaultBrandName;

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(to);
            helper.setFrom(fromAddress);
            helper.setSubject("🔒 " + resolvedClinicName + " - Password Reset OTP");
            helper.setText(buildForgotPasswordOtpBody(otp, accountName, resolvedClinicName), true);

            mailSender.send(mimeMessage);
            logger.info("Forgot-password OTP sent successfully to {}", to);

        } catch (Exception e) {
            logger.error("Failed to send forgot-password OTP to {}: {}", to, e.getMessage(), e);
        }
    }

    private String buildForgotPasswordOtpBody(String otp, String accountName, String clinicName) {

        String greetingName = (accountName != null && !accountName.isBlank())
                ? accountName
                : "there";

        return """
            <html>
            <body style="margin:0; padding:0; font-family: Arial, sans-serif; background:#f5f7fa;">

            <div style="max-width:600px; margin:30px auto; background:#ffffff; border-radius:10px;
                        border:1px solid #e0e0e0; overflow:hidden;">

                <div style="background:linear-gradient(135deg, #0f2027, #203a43, #2c5364); padding:18px; text-align:center;">
                    <h2 style="color:#ffffff; margin:0;">%s - Password Reset Request</h2>
                </div>

                <div style="padding:20px; color:#333;">
                    <p>👋 Hello %s,</p>
                    <p style="line-height:1.6;">
                        We received a request to reset the password for your %s account.
                        Use the OTP below to proceed. If you didn't request this, you can safely ignore this email.
                    </p>

                    <div style="text-align:center; margin:25px 0;">
                        <span style="display:inline-block; font-size:28px; font-weight:bold;
                                     letter-spacing:6px; color:#28a745; background:#e8f0fe;
                                     padding:14px 28px; border-radius:8px;">
                            %s
                        </span>
                    </div>

                    <p style="color:#777; font-size:13px;">
                        This OTP is valid for 10 minutes. Do not share it with anyone.
                    </p>

                    <p style="margin-top:20px;">
                        Regards,<br>
                        %s Support Team
                    </p>
                </div>

                <div style="background:#f1f3f6; padding:12px; text-align:center; font-size:12px; color:#777;">
                    © 2026 %s. All rights reserved.
                </div>
            </div>

            </body>
            </html>
            """.formatted(clinicName, greetingName, clinicName, otp, clinicName, clinicName);
    }
}