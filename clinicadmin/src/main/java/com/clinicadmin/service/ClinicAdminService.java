package com.clinicadmin.service;

import org.springframework.http.ResponseEntity;

import com.clinicadmin.dto.ClinicDTO;
import com.clinicadmin.dto.ClinicLoginRequestDTO;
import com.clinicadmin.dto.ResetPasswordDTO;
import com.clinicadmin.dto.Response;
import com.clinicadmin.dto.UpdateClinicLoginCredentialsDTO;

public interface ClinicAdminService {

	public Response login(ClinicLoginRequestDTO credentials);

	public Response updateClinicCredentials(UpdateClinicLoginCredentialsDTO updatedCredentials, String userName);

	public Response getClinicById(String hospitalId);

	public Response updateClinic(String hospitalId, ClinicDTO dto);

	public Response deleteClinic(String hospitalId);

	ResponseEntity<?> getBranchesByClinicId(String clinicId);

	public Response getStaffInfo(String hospitalId, String branchId);

	public String getDeviceId(String clinicId, String branchId);

	public Response forgotPassword(String mobileNumber, String role);

	public Response verifyOtp(String mobileNumber, String role, String otp);

	public Response resetPassword(String mobileNumber, String role, ResetPasswordDTO dto);

}
