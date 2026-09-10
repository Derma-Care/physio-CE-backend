package com.AdminService.service;

import java.util.List;

import com.AdminService.dto.*;
import com.AdminService.util.ResponseStructure;
import org.springframework.http.ResponseEntity;

//import com.AdminService.dto.CategoryDto;
//import com.AdminService.dto.CustomerDTO;
//import com.AdminService.dto.ServicesDto;
//import com.AdminService.dto.SubServicesDto;
//import com.AdminService.dto.SubServicesInfoDto;
import com.AdminService.util.Response;

public interface AdminService {

//ADMIN

	public Response adminRegister(AdminHelper helperAdmin);

	public Response adminLogin(String userName, String password);

//CLINIC MANAGEMENT
	public Response createClinic(ClinicDTO clinic);

	Response getClinicById(String clinicId);

	public Response getAllClinics();

	Response updateClinic(String clinicId, ClinicDTO clinic);

	Response deleteClinic(String clinicId);
///public String findEmailByMobileNumber(String mobileNumber);
////================= CLINIC VERIFICATION FLOW =================
//
////Start verification (Admin action)
//Response startVerificationProcess(String clinicId);
//
////Verify clinic (Approve)
//Response verifyClinic(String clinicId);
//
////Reject clinic
//Response rejectClinic(String clinicId, String reason);

//CLINIC CREDENTIALS
	public Response getClinicCredentials(String userName);

	public Response updateClinicCredentials(UpdateClinicCredentials credentials, String userName);

	public Response deleteClinicCredentials(String userName);

	public Response login(ClinicCredentialsDTO credentials);

//CUSTOMER MANAGEMENT
	//public Response saveCustomerBasicDetails(CustomerDTO customerDTO);

	//public ResponseEntity<?> getCustomerByUsernameMobileEmail(String input);

	//public Response getCustomerBasicDetails(String mobileNumber);

	//public Response getAllCustomers();

	//public Response updateCustomerBasicDetails(CustomerDTO customerDTO, String mobileNumber);

	//public Response deleteCustomerBasicDetails(String mobileNumber);

//SUBSERVICES
//public Response getAllSubServicesFromClincAdmin();

//BOOKINGS

//DOCTORS
//	public Response getDoctorInfoByDoctorId(String doctorId);
//
//	public Response getClinicsByRecommondation();

	Response getAllRecommendClinicThenAnotherClincs();

	Response rejectClinic(String clinicId, String reason);

	Response verifyClinic(String clinicId);

	Response startVerificationProcess(String clinicId);

    ResponseStructure<List<BookingResponse>> getAllBookedServices();
//---------------Forgot Password---------------------------------------

	Response resetPasswordWithOtp(ResetPasswordDTO dto);

	Response forgotPassword(String mobileNumber, String role);

	Response verifyForgotPasswordOtp(String mobileNumber, String otp, String role);
	public List<String> getFcmTokens(String cId,String bId);
	Response updateClinicCredentialsWithUserNameAndRole(UpdateClinicCredentialsWithUserNameAndRole credentials);

	public int getFreeFollowUpsByHospitalId(String hospitalId);
}
