package com.clinicadmin.service.impl;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.clinicadmin.dto.ResetPasswordDTO;
import com.clinicadmin.dto.Response;
import com.clinicadmin.entity.DoctorAndStaffLoginCredentials;
import com.clinicadmin.feignclient.AdminServiceClient;
import com.clinicadmin.repository.DoctorLoginCredentialsRepository;
import com.clinicadmin.service.EmailService;
import com.clinicadmin.service.ForgotPasswordService;

@Service
public class ForgotPasswordServiceImpl implements ForgotPasswordService {

    @Autowired
    private AdminServiceClient adminServiceClient;

    @Autowired
    private DoctorLoginCredentialsRepository loginRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final long OTP_VALID_MILLIS = 10 * 60 * 1000; // 10 minutes

    // ================= FORGOT PASSWORD =================
    @Override
    public Response forgotPassword(String mobileNumber, String role) {

        Response response = new Response();

        if (mobileNumber == null || mobileNumber.isBlank()) {
            response.setSuccess(false);
            response.setStatus(400);
            response.setMessage("Mobile number is required");
            return response;
        }

        if (role == null || role.isBlank()) {
            response.setSuccess(false);
            response.setStatus(400);
            response.setMessage("Role is required");
            return response;
        }

        // NEW: normalize role to uppercase, trimmed, right at entry — every
        // downstream check and log line uses this normalized value.
        String normalizedRole = role.trim().toUpperCase();

        // ---------------- ADMIN → delegate to AdminService ----------------
        if ("ADMIN".equals(normalizedRole)) {
            return adminServiceClient.forgotPassword(mobileNumber, normalizedRole).getBody();
        }

        // ---------------- STAFF (Doctor/Therapist/Receptionist/etc.) ----------------
        // NEW: case-insensitive lookup — existing DB rows may have inconsistent
        // casing from before this normalization existed, so an exact-match query
        // against the uppercased input would miss them.
        DoctorAndStaffLoginCredentials user =
                loginRepository.findByMobilenumberAndRoleIgnoreCase(mobileNumber, normalizedRole);

        if (user == null) {
            response.setSuccess(false);
            response.setStatus(404);
            response.setMessage("No account found with this mobile number and role");
            return response;
        }

        if (user.getEmailId() == null || user.getEmailId().isBlank()) {
            response.setSuccess(false);
            response.setStatus(404);
            response.setMessage("No email registered for this account");
            return response;
        }

        // NEW: self-heal — if the stored role wasn't already uppercase,
        // normalize it now so future exact-match lookups work too.
        if (!normalizedRole.equals(user.getRole())) {
            user.setRole(normalizedRole);
        }

        String otp = generateOtp();
        long expiry = System.currentTimeMillis() + OTP_VALID_MILLIS;

        user.setOtp(otp);
        user.setOtpExpiryMillis(expiry);
        loginRepository.save(user);

        // clinicName passed dynamically so the OTP email shows this staff
        // member's actual clinic instead of a hardcoded brand name
        emailService.sendForgotPasswordOtp(user.getEmailId(), otp, user.getStaffName(), user.getHospitalName());

        response.setSuccess(true);
        response.setStatus(200);
        response.setMessage("OTP sent to registered email");
        return response;
    }

    // ================= VERIFY OTP =================
    @Override
    public Response verifyOtp(String mobileNumber, String role, String otp) {

        Response response = new Response();

        if (role == null || role.isBlank()) {
            response.setSuccess(false);
            response.setStatus(400);
            response.setMessage("Role is required");
            return response;
        }

        String normalizedRole = role.trim().toUpperCase();

        if ("ADMIN".equals(normalizedRole)) {
            return adminServiceClient.verifyOtp(mobileNumber, normalizedRole, otp).getBody();
        }

        DoctorAndStaffLoginCredentials user =
                loginRepository.findByMobilenumberAndRoleIgnoreCase(mobileNumber, normalizedRole);

        if (user == null) {
            response.setSuccess(false);
            response.setStatus(404);
            response.setMessage("No account found with this mobile number and role");
            return response;
        }

        return checkOtpValidity(user.getOtp(), user.getOtpExpiryMillis(), otp, response);
    }

    // ================= RESET PASSWORD =================
    @Override
    public Response resetPassword(String mobileNumber, String role, ResetPasswordDTO dto) {

        Response response = new Response();

        if (dto.getNewPassword() == null || !dto.getNewPassword().equals(dto.getConfirmPassword())) {
            response.setSuccess(false);
            response.setStatus(400);
            response.setMessage("New password and confirm password do not match");
            return response;
        }

        if (role == null || role.isBlank()) {
            response.setSuccess(false);
            response.setStatus(400);
            response.setMessage("Role is required");
            return response;
        }

        String normalizedRole = role.trim().toUpperCase();

        if ("ADMIN".equals(normalizedRole)) {
            return adminServiceClient.resetPassword(normalizedRole, mobileNumber, dto).getBody();
        }

        DoctorAndStaffLoginCredentials user =
                loginRepository.findByMobilenumberAndRoleIgnoreCase(mobileNumber, normalizedRole);

        if (user == null) {
            response.setSuccess(false);
            response.setStatus(404);
            response.setMessage("No account found with this mobile number and role");
            return response;
        }

        Response otpCheck = checkOtpValidity(user.getOtp(), user.getOtpExpiryMillis(), dto.getOtp(), new Response());

        if (!otpCheck.isSuccess()) {
            return otpCheck;
        }

        // NEW: self-heal role casing here too
        if (!normalizedRole.equals(user.getRole())) {
            user.setRole(normalizedRole);
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        user.setOtp(null);
        user.setOtpExpiryMillis(null);
        loginRepository.save(user);

        response.setSuccess(true);
        response.setStatus(200);
        response.setMessage("Password reset successfully");
        return response;
    }

    // ---------------- helpers ----------------
    private Response checkOtpValidity(String storedOtp, Long expiryMillis, String suppliedOtp, Response response) {

        if (storedOtp == null || expiryMillis == null) {
            response.setSuccess(false);
            response.setStatus(400);
            response.setMessage("No OTP requested for this account. Please request one first.");
            return response;
        }

        if (System.currentTimeMillis() > expiryMillis) {
            response.setSuccess(false);
            response.setStatus(410);
            response.setMessage("OTP has expired. Please request a new one.");
            return response;
        }

        if (!storedOtp.equals(suppliedOtp)) {
            response.setSuccess(false);
            response.setStatus(401);
            response.setMessage("Invalid OTP");
            return response;
        }

        response.setSuccess(true);
        response.setStatus(200);
        response.setMessage("OTP verified successfully");
        return response;
    }

    private String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // always 6 digits
        return String.valueOf(otp);
    }
}