package com.clinicadmin.service;

import java.io.InputStream;
import java.net.URI;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.clinicadmin.entity.TestOrder;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

	private final JavaMailSender mailSender;
	private final Logger logger = LoggerFactory.getLogger(EmailService.class);

	private final String fromAddress;
	private final String doctorLoginUrl;
	private final String therapistLoginUrl;
	private final String defaultBrandName;

	public EmailService(JavaMailSender mailSender, Environment env) {
		this.mailSender = mailSender;
		this.fromAddress = env.getProperty("notification.default-from-email", "no-reply@glowkart.com");
		this.doctorLoginUrl = env.getProperty("notification.doctor-login-url", "https://doctor.ccmstestserver.online" // ✅
																														// updated
		);
		this.therapistLoginUrl = env.getProperty("notification.therapist-login-url",
				"https://therapist.ccmstestserver.online" // ✅ updated
		);
		// Only used as a last-resort fallback when a clinic name isn't supplied
		this.defaultBrandName = env.getProperty("notification.default-brand-name", "Kinetix Wellness Care");
	}

	// ===================== RESOLVE LOGIN URL BY ROLE =====================
	private String resolveLoginUrl(String role) {
		if (role == null || role.isBlank())
			return doctorLoginUrl;
		return switch (role.toLowerCase()) {
		case "doctor" -> doctorLoginUrl; // ✅ renamed
		case "physiotherapist" -> therapistLoginUrl;
		// case "receptionist" -> receptionistLoginUrl;
		default -> doctorLoginUrl; // ✅ renamed
		};
	}

	// Every method below reads clinicName the same way, so if a caller forgets
	// to supply it, we fall back to the default brand instead of silently
	// rendering "null" in the email.
	private String resolveClinicName(Map<String, String> data) {
		String clinicName = data.get("clinicName");
		return (clinicName != null && !clinicName.isBlank()) ? clinicName : defaultBrandName;
	}

	// ===================== MAIN SEND METHOD =====================
	// Callers should put the clinic's actual name into data.put("clinicName", clinic.getName())
	// (or doctor.getHospitalName()) before calling this, so every template below can render it dynamically.
	public void sendEmail(String to, Map<String, String> data) {
		try {
			if (to == null || to.isBlank()) {
				logger.warn("Email not sent: recipient address is blank");
				return;
			}

			String clinicName = resolveClinicName(data);
			String subject = data.getOrDefault("subject", clinicName);
			String role = data.getOrDefault("role", "doctor");

			String emoji = "";
			if (subject.contains("Verified"))
				emoji = "🎉";
			else if (subject.contains("Pending"))
				emoji = "⏳";
			else if (subject.contains("Review"))
				emoji = "🔍";
			else if (subject.contains("Rejected"))
				emoji = "❌";
			else if (subject.contains("OTP"))
				emoji = "🔒";

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
				helper.setText(buildMessageBody(data, clinicName, resolveLoginUrl(role)), true);
			}

			mailSender.send(mimeMessage);
			logger.info("Email sent successfully to {} with role={}", to, role);

		} catch (Exception e) {
			logger.error("Failed to send email to {}: {}", to, e.getMessage(), e);
		}
	}

	// ===================== GENERAL EMAIL BODY =====================
	private String buildMessageBody(Map<String, String> data, String clinicName, String loginUrl) {

		String bodyMessage = data.getOrDefault("message", "");
		String username = data.get("username");
		String password = data.get("password");

		if (bodyMessage != null && !bodyMessage.toLowerCase().contains("welcome to")) {
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
				"""
				.formatted(

						clinicName,

						bodyMessage.replace("\n", "<br>"),

						// ✅ Credentials Box
						(username != null && password != null) ? """
								<div style="background:#e8f0fe; padding:15px; border-radius:6px; margin-top:15px;">
								    <h3 style="margin-top:0; color:#1a73e8;">Login Credentials</h3>
								    <p><b>Username:</b> %s</p>
								    <p><b>Password:</b> %s</p>
								</div>
								""".formatted(username, password) : "",

						// ✅ Login Button — resolved by role
						(username != null && password != null) ? """
								<div style="text-align:center; margin-top:20px;">
								    <a href="%s"
								       style="background:#28a745; color:white; padding:10px 22px;
								              border-radius:6px; text-decoration:none; font-weight:bold;">
								       Login Now
								    </a>
								</div>
								""".formatted(loginUrl) : "",

						clinicName,
						clinicName);
	}

	// ===================== OTP EMAIL BODY =====================
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

	// ===================== REJECTION EMAIL BODY =====================
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

	// clinicName/branchName were already passed in dynamically here — unchanged.
	public void sendPatientEmail(

			String to,

			String patientName,

			String clinicName,

			String branchName,

			String title,

			String body) {

		try {

			if (to == null || to.isBlank()) {

				logger.warn("Patient email not sent: recipient address is blank");

				return;
			}

			MimeMessage mimeMessage = mailSender.createMimeMessage();

			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

			helper.setTo(to);

			helper.setFrom(fromAddress);

			helper.setSubject(title);

			helper.setText(

					buildPatientMessageBody(

							patientName,

							clinicName,

							branchName,

							title,

							body),

					true);

			mailSender.send(mimeMessage);

			logger.info("Patient email sent successfully to {}", to);

		} catch (Exception e) {

			logger.error("Failed to send patient email to {} : {}", to, e.getMessage(), e);
		}
	}

	private String buildPatientMessageBody(

			String patientName,

			String clinicName,

			String branchName,

			String title,

			String body) {

		return """
				<html>

				<body style="
				    margin:0;
				    padding:0;
				    font-family:Arial,sans-serif;
				    background:#f5f7fa;">

				<div style="
				    max-width:600px;
				    margin:30px auto;
				    background:#ffffff;
				    border-radius:10px;
				    border:1px solid #e0e0e0;
				    overflow:hidden;">

				    <!-- Header -->

				    <div style="
				        background:linear-gradient(135deg,#0f2027,#203a43,#2c5364);
				        padding:12px 10px;
				        text-align:center;">

				        <div style="
				            color:#ffffff;
				            font-size:22px;
				            font-weight:700;
				            line-height:1.2;">

				            %s

				        </div>

				        <div style="
				            color:#d9e3ea;
				            font-size:12px;
				            margin-top:3px;
				            font-weight:400;">

				            %s

				        </div>

				    </div>

				    <!-- Body -->

				    <div style="
				        padding:25px;
				        color:#333;">

				        <p style="
				            margin-top:0;">

				            Dear %s,

				        </p>

				        <h3 style="
				            margin-top:10px;">

				            %s

				        </h3>

				        <p style="
				            line-height:1.8;">

				            %s

				        </p>

				        <br>

				        <p>

				            Regards,<br>

				            <strong>%s</strong>

				        </p>

				        <hr style="
				            border:none;
				            border-top:1px solid #e0e0e0;
				            margin-top:25px;">

				        <p style="
				            text-align:center;
				            font-size:12px;
				            color:#777;
				            margin-top:15px;">

				            Powered by %s

				        </p>

				    </div>

				</div>

				</body>

				</html>
				""".formatted(

				clinicName,

				branchName,

				patientName,

				title,

				body.replace("\n", "<br>"),

				clinicName,

				clinicName);
	}

	public void sendPatientPdfEmail(

			String to,

			String patientName,

			String title,

			String body,

			String pdfFile,

			String clinicName) {

		try {

			if (to == null || to.isBlank()) {

				logger.warn("Patient PDF email not sent: recipient address is blank");

				return;
			}

			String resolvedClinicName = (clinicName != null && !clinicName.isBlank()) ? clinicName : defaultBrandName;

			MimeMessage mimeMessage = mailSender.createMimeMessage();

			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

			helper.setTo(to);

			helper.setFrom(fromAddress);

			helper.setSubject(title);

			helper.setText(

					buildPatientPdfMessageBody(

							patientName,

							title,

							body,

							resolvedClinicName),

					true);

			// ✅ Attach PDF from S3 signed URL
			if (pdfFile != null && !pdfFile.isBlank()) {

				try (InputStream inputStream =

						URI.create(pdfFile).toURL().openStream()) {

					byte[] pdfBytes = inputStream.readAllBytes();

					helper.addAttachment(

							title + ".pdf",

							new ByteArrayResource(pdfBytes));
				}
			}

			mailSender.send(mimeMessage);

			logger.info("Patient PDF email sent successfully to {}", to);

		} catch (Exception e) {

			logger.error("Failed to send patient PDF email to {} : {}", to, e.getMessage(), e);
		}
	}

	private String buildPatientPdfMessageBody(

			String patientName,

			String title,

			String body,

			String clinicName) {

		return """
				<html>

				<body style="
				    font-family:Arial,sans-serif;
				    font-size:14px;
				    color:#333333;">

				    <h3>%s</h3>

				    <p>

				        Dear %s,

				    </p>

				    <p>

				        Please find the attached PDF document.

				    </p>

				    <br>

				    <p>

				        Regards,<br>

				        <strong>%s</strong>

				    </p>

				    <hr>

				    <p style="
				        font-size:12px;
				        color:#777777;">

				        This is a computer-generated email. Please do not reply to this email.

				    </p>

				    <p style="
				        font-size:12px;
				        color:#777777;">

				        Powered by <strong>%s</strong>

				    </p>

				</body>

				</html>
				""".formatted(

				title,

				patientName,

				clinicName,

				clinicName);
	}

// ================= FORGOT PASSWORD OTP =================
	public void sendForgotPasswordOtp(String to, String otp, String accountName, String clinicName) {
		try {
			if (to == null || to.isBlank()) {
				logger.warn("Email not sent: recipient address is blank");
				return;
			}

			String resolvedClinicName = (clinicName != null && !clinicName.isBlank()) ? clinicName : defaultBrandName;

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

		String greetingName = (accountName != null && !accountName.isBlank()) ? accountName : "there";

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
				"""
				.formatted(clinicName, greetingName, clinicName, otp, clinicName, clinicName);
	}
	public void sendTestOrderEmail(TestOrder order,
            String clinicName,
            String branchLocation,
            String clinicEmail,
            String clinicContactNumber) {

try {

MimeMessage mimeMessage = mailSender.createMimeMessage();
MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

helper.setTo(order.getVendorEmail());
helper.setFrom(fromAddress);
helper.setSubject("New Test Order Received");

String body = """
<!DOCTYPE html>
<html>
<head>
 <meta charset="UTF-8">
</head>

<body style="margin:0;padding:0;background:#f4f6f9;font-family:Arial,Helvetica,sans-serif;">

<table width="100%%" cellpadding="0" cellspacing="0" style="background:#f4f6f9;padding:30px 0;">

 <tr>
     <td align="center">

         <table width="700" cellpadding="0" cellspacing="0"
                style="background:#ffffff;border-radius:8px;
                overflow:hidden;
                box-shadow:0px 2px 10px rgba(0,0,0,0.15);">

             <!-- Header -->

             <tr>
                 <td align="center"
                     style="background:#183542;
                     color:white;
                     font-size:30px;
                     font-weight:bold;
                     padding:22px;">

                     %s

                 </td>
             </tr>

             <!-- Body -->

             <tr>

                 <td style="padding:35px;">

                     <p style="font-size:22px;margin-top:0;">
                         Welcome <b>%s!</b>
                     </p>

                     <p style="font-size:16px;">
                         A new laboratory test order has been assigned to your laboratory.
                     </p>

                     <table width="100%%"
                            cellpadding="8"
                            cellspacing="0"
                            style="border-collapse:collapse;
                            margin-top:20px;">

                         <tr>
                             <td style="border:1px solid #ddd;"><b>Clinic Name</b></td>
                             <td style="border:1px solid #ddd;">%s</td>
                         </tr>

                         <tr>
                             <td style="border:1px solid #ddd;"><b>Branch Location</b></td>
                             <td style="border:1px solid #ddd;">%s</td>
                         </tr>

                         <tr>
                             <td style="border:1px solid #ddd;"><b>Clinic Email</b></td>
                             <td style="border:1px solid #ddd;">%s</td>
                         </tr>

                         <tr>
                             <td style="border:1px solid #ddd;"><b>Clinic Contact Number</b></td>
                             <td style="border:1px solid #ddd;">%s</td>
                         </tr>

                         <tr>
                             <td style="border:1px solid #ddd;"><b>Order ID</b></td>
                             <td style="border:1px solid #ddd;">%s</td>
                         </tr>

                         <tr>
                             <td style="border:1px solid #ddd;"><b>Patient Name</b></td>
                             <td style="border:1px solid #ddd;">%s</td>
                         </tr>

                         <tr>
                             <td style="border:1px solid #ddd;"><b>Mobile Number</b></td>
                             <td style="border:1px solid #ddd;">%s</td>
                         </tr>

                         <tr>
                             <td style="border:1px solid #ddd;"><b>Doctor Name</b></td>
                             <td style="border:1px solid #ddd;">%s</td>
                         </tr>

                         <tr>
                             <td style="border:1px solid #ddd;"><b>Test Name</b></td>
                             <td style="border:1px solid #ddd;">%s</td>
                         </tr>

                         <tr>
                             <td style="border:1px solid #ddd;"><b>Ordered At</b></td>
                             <td style="border:1px solid #ddd;">%s</td>
                         </tr>

                     </table>

                     <br>

                     <p style="font-size:15px;">
                         Please process the laboratory test and update the status once completed.
                     </p>

                     <br>

                     <p style="font-size:15px;">
                         Regards,<br>
                         <b>%s Team</b>
                     </p>

                 </td>

             </tr>

         </table>

     </td>

 </tr>

</table>

</body>
</html>
""".formatted(
clinicName,
order.getVendorName(),
clinicName,
branchLocation,
clinicEmail,
clinicContactNumber,
order.getId(),
order.getPatientName(),
order.getMobileNumber(),
order.getDoctorName(),
order.getTestName(),
order.getOrderedAt(),
clinicName
);

helper.setText(body, true);

mailSender.send(mimeMessage);

logger.info("Test Order email sent successfully to {}", order.getVendorEmail());

} catch (Exception e) {
logger.error("Failed to send Test Order email", e);
}
}
}