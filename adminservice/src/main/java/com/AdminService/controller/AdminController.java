package com.AdminService.controller;

import java.util.List;
import java.util.Map;

import com.AdminService.dto.*;
import com.AdminService.util.ResponseStructure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

//import com.AdminService.dto.CategoryDto;
//import com.AdminService.dto.CustomerDTO;
//import com.AdminService.dto.ServicesDto;
//import com.AdminService.dto.SubServicesDto;
//import com.AdminService.dto.SubServicesInfoDto;
import com.AdminService.service.AdminService;
import com.AdminService.util.PermissionsUtil;
import com.AdminService.util.Response;

import jakarta.validation.Valid;

@RestController

@RequestMapping("/admin")

//@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})

public class AdminController {

	@Autowired
	private AdminService serviceImpl;

	@PostMapping("/adminRegister")

	private ResponseEntity<?> adminRegister(@RequestBody @Valid AdminHelper helperAdmin) {

		Response response = serviceImpl.adminRegister(helperAdmin);

		if (response != null && response.getStatus() != 0) {

			return ResponseEntity.status(response.getStatus()).body(response);

		} else {

			return null;
		}

	}

	@PostMapping("/adminLogin")

	public ResponseEntity<?> adminLogin(@RequestBody AdminHelper helperAdmin) {

		Response response = serviceImpl.adminLogin(helperAdmin.getUserName(), helperAdmin.getPassword());

		if (response != null && response.getStatus() != 0) {

			return ResponseEntity.status(response.getStatus()).body(response);

		} else {

			return null;
		}

	}

//	  @GetMapping("/password/{mobileNumber}")
//	  public String findEmailByMobileNumber(String mobileNumber) {
//		  
//		  return serviceImpl.findEmailByMobileNumber(mobileNumber);
//	  }

	@PostMapping("/CreateClinic")
	public ResponseEntity<?> clinicRegistration(@RequestBody @Valid ClinicDTO clinic) {

		Response response = serviceImpl.createClinic(clinic);

		if (response != null && response.getStatus() != 0) {
			return ResponseEntity.status(response.getStatus()).body(response);
		} else {
			return ResponseEntity.internalServerError().build();
		}
	}

	@PutMapping("/start-verification/{clinicId}")
	public ResponseEntity<Response> startVerification(@PathVariable String clinicId) {

		Response response = serviceImpl.startVerificationProcess(clinicId);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	@PutMapping("/verify/{clinicId}")
	public ResponseEntity<Response> verifyClinic(@PathVariable String clinicId) {

		Response response = serviceImpl.verifyClinic(clinicId);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	@PutMapping("/reject/{clinicId}")
	public ResponseEntity<Response> rejectClinic(@PathVariable String clinicId, @RequestParam String reason) {

		Response response = serviceImpl.rejectClinic(clinicId, reason);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	// Get Clinic by ID

	@GetMapping("/getClinicById/{clinicId}")

	public Response getClinicById(@PathVariable String clinicId) {

		Response response = serviceImpl.getClinicById(clinicId);

		return response;

	}

	// GET ALL CUSTOMERS

	@GetMapping("/getAllClinics")

	public ResponseEntity<?> getAllClinics() {

		Response response = serviceImpl.getAllClinics();

		if (response != null && response.getStatus() != 0) {

			return ResponseEntity.status(response.getStatus()).body(response);

		} else {

			return null;
		}

	}

	// Update Clinic

	@PutMapping("/updateClinic/{clinicId}")

	public Response updateClinic(@PathVariable String clinicId, @RequestBody ClinicDTO clinic) {

		Response response = serviceImpl.updateClinic(clinicId, clinic);

		return response;

	}

	// Delete Clinic

	@DeleteMapping("/deleteClinic/{clinicId}")

	public ResponseEntity<?> deleteClinic(@PathVariable String clinicId) {

		Response response = serviceImpl.deleteClinic(clinicId);

		if (response != null && response.getStatus() != 0) {

			return ResponseEntity.status(response.getStatus()).body(response);

		} else {

			return null;
		}

	}

	/// CLINIC CREDENTIALS

	// Get clinic credentials by hospitalId

	@GetMapping("/getClinicCredentials/{userName}")

	public Response getClinicCredentials(@PathVariable String userName) {

		Response response = serviceImpl.getClinicCredentials(userName);

		return response;

	}

	// Update clinic credentials

	@PutMapping("/updateClinicCredentials/{userName}")

	public Response updateClinicCredentials(@RequestBody UpdateClinicCredentials updatedCredentials

			, @PathVariable String userName) {

		Response response = serviceImpl.updateClinicCredentials(updatedCredentials, userName);

		return response;

	}

	// Delete clinic credentials

	@DeleteMapping("/deleteClinicCredentials/{userName}")

	public ResponseEntity<?> deleteClinicCredentials(@PathVariable String userName) {

		Response response = serviceImpl.deleteClinicCredentials(userName);

		if (response != null && response.getStatus() != 0) {

			return ResponseEntity.status(response.getStatus()).body(response);

		} else {

			return null;
		}

	}

	// clinic admin login

	@PostMapping("/login")

	public Response login(@RequestBody @Valid ClinicCredentialsDTO credentials) {

		Response response = serviceImpl.login(credentials);

		return response;

	}


//GETALLSUBSERVICES

//@GetMapping("/getAllSubservicesByClinicAdmin")
//
//public ResponseEntity<Object> getAllSubservicesByClinicAdmin(){
//
//	Response response = serviceImpl.getAllSubServicesFromClincAdmin();
//
//	if(response != null && response.getData() == null) {
//
//		 return ResponseEntity.status(response.getStatus()).body(response);
//
//	 }else if(response != null && response.getData() != null ) {
//
//		 return ResponseEntity.status(response.getStatus()).body(response.getData());
//
//	 }
//
//	else {
//
//	     return null;}
//
//}


	@GetMapping("/clinics/firstRecommendedTureClincs")

	public ResponseEntity<Response> firstRecommendedTureClincs() {

		Response response = serviceImpl.getAllRecommendClinicThenAnotherClincs();

		return ResponseEntity.status(response.getStatus()).body(response);

	}

	// ✅ API to fetch default Admin permissions
	@GetMapping("/getDefaultAdminPermissions")
	public ResponseEntity<Map<String, List<String>>> getDefaultAdminPermissions() {
		Map<String, List<String>> adminPermissions = PermissionsUtil.getAdminPermissions();
		return new ResponseEntity<>(adminPermissions, HttpStatus.OK);
	}

	// ---------------- FORGOT PASSWORD
	// -----------------------------------------------------------------------------------
	// GET /admin/forgot-password/{mobileNumber}/{role}
	@GetMapping("/forgot-password/{mobileNumber}/{role}")
	public ResponseEntity<Response> forgotPassword(@PathVariable String mobileNumber, @PathVariable String role) {

		Response response = serviceImpl.forgotPassword(mobileNumber, role);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	// ---------------- VERIFY OTP ----------------
	// GET /admin/verify-otp/{mobileNumber}/{role}/{otp}
	@GetMapping("/verify-otp/{mobileNumber}/{role}/{otp}")
	public ResponseEntity<Response> verifyOtp(@PathVariable String mobileNumber, @PathVariable String role,
			@PathVariable String otp) {

		Response response = serviceImpl.verifyForgotPasswordOtp(mobileNumber, otp, role);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	// ---------------- RESET PASSWORD ----------------
	// POST /admin/reset-password/{mobileNumber}/{role}
	// Body: { "otp": "...", "newPassword": "...", "confirmPassword": "..." }
	@PostMapping("/reset-password/{role}/{mobileNumber}")
	public ResponseEntity<Response> resetPassword(@PathVariable String mobileNumber, @PathVariable String role,
			@RequestBody ResetPasswordDTO dto) {

		dto.setMobileNumber(mobileNumber);
		dto.setRole(role);

		Response response = serviceImpl.resetPasswordWithOtp(dto);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

//	-----------------reset password with username and password-----------------------
	@PutMapping("/update-password")
	public ResponseEntity<Response> updateClinicCredentialsWithUserNameAndRole(
			@RequestBody @Valid UpdateClinicCredentialsWithUserNameAndRole credentials) {

		Response response = serviceImpl.updateClinicCredentialsWithUserNameAndRole(credentials);

		return ResponseEntity.status(response.getStatus()).body(response);
	}
	
	@GetMapping("/getFcmTokensByIds/{cId}/{bId}")
	public List<String> getFcmTokens(@PathVariable String cId,@PathVariable String bId){
		return serviceImpl.getFcmTokens(cId, bId);
	}


    @GetMapping("/getAllBookedServices")
    public ResponseEntity<ResponseStructure<List<BookingResponse>>> getAllBookedServices() {
        ResponseStructure<List<BookingResponse>> response = serviceImpl.getAllBookedServices();
        HttpStatus status = response.getHttpStatus() != null ? response.getHttpStatus() : HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status).body(response);
    }


	@GetMapping("/freeFollowUps/{id}")
	public int getFreeFollowUps(@PathVariable("id") String hospitalId) {
		return serviceImpl.getFreeFollowUpsByHospitalId(hospitalId);
	}

}
