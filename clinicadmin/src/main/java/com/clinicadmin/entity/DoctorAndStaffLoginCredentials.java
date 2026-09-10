package com.clinicadmin.entity;

import lombok.*;

import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "clinic_staff_login_credentials")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorAndStaffLoginCredentials {

	@Id
	private String id;
	private String mobilenumber;
	private String staffId;
	private String staffName;
	private String clinicId;
	private String hospitalId;
	private String hospitalName;
	private String branchId;
	private String branchName;
	private String username;
	private String password;
	private String role;
	private String deviceId;
	private String emailId;
	private Map<String, List<String>> permissions;
	 // ===== Added for Forgot Password =====
    private String otp;
    private Long otpExpiryMillis;


}
