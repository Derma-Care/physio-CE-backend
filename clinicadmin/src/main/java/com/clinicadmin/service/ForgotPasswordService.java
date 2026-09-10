package com.clinicadmin.service;

import com.clinicadmin.dto.ResetPasswordDTO;
import com.clinicadmin.dto.Response;

public interface ForgotPasswordService {

	Response forgotPassword(String mobileNumber, String role);

	Response verifyOtp(String mobileNumber, String role, String otp);

	Response resetPassword(String mobileNumber, String role, ResetPasswordDTO dto);
}
