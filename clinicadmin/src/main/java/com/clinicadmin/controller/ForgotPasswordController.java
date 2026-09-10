package com.clinicadmin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clinicadmin.dto.ResetPasswordDTO;
import com.clinicadmin.dto.Response;
import com.clinicadmin.service.ForgotPasswordService;

@RestController
@RequestMapping("/clinic-admin")
public class ForgotPasswordController {

	@Autowired
	private ForgotPasswordService forgotPasswordService;

	@GetMapping("/forgot-password/{mobileNumber}/{role}")
	public ResponseEntity<Response> forgotPassword(@PathVariable String mobileNumber, @PathVariable String role) {

		Response response = forgotPasswordService.forgotPassword(mobileNumber, role);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	@GetMapping("/verify-otp/{mobileNumber}/{role}/{otp}")
	public ResponseEntity<Response> verifyOtp(@PathVariable String mobileNumber, @PathVariable String role,
			@PathVariable String otp) {

		Response response = forgotPasswordService.verifyOtp(mobileNumber, role, otp);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	@PostMapping("/reset-password/{role}/{mobileNumber}")
	public ResponseEntity<Response> resetPassword(@PathVariable String role, @PathVariable String mobileNumber,
			@RequestBody ResetPasswordDTO dto) {

		dto.setMobileNumber(mobileNumber);
		dto.setRole(role);

		Response response = forgotPasswordService.resetPassword(mobileNumber, role, dto);
		return ResponseEntity.status(response.getStatus()).body(response);
	}
}
