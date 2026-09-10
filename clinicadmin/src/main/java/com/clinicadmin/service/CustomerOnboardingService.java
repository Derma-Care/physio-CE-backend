package com.clinicadmin.service;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.clinicadmin.dto.BookingInfoByInput;
import com.clinicadmin.dto.CustomerLoginDTO;
import com.clinicadmin.dto.CustomerOnbordingDTO;
import com.clinicadmin.dto.Response;
import com.clinicadmin.entity.CustomerOnbording;

public interface CustomerOnboardingService {
	Response onboardCustomer(CustomerOnbordingDTO dto);

	Response getAllCustomers();

	Response getCustomerById(String customerId);

	Response updateCustomer(String customerId, CustomerOnbordingDTO dto);

	Response deleteCustomer(String customerId);

	Response getCustomersByHospitalId(String hospitalId,String branchId);

	Response getCustomersByBranchId(String branchId);

	Response getCustomersByHospitalIdAndBranchId(String hospitalId, String branchId);

	Response login(CustomerLoginDTO dto);
	
	public Response getCustomersByPatientId(String patientId,String clinicId);
	
	public CustomerOnbordingDTO getCustomerByToken(String token);
	public Response getCustomerByMobiileNumber(String mobilenumber);
	public Map<String,String> getCustomerByMobilenumberAndName(String mobilenumber,String name);	
	public CustomerOnbordingDTO getCustomerByMobileNumberAndClinicId(String mobilenumber,String clinicId);
	//public  List<CustomerOnbordingDTO> getCustomerByNameAndClinicId(String name,String clinicId);	
	public List<BookingInfoByInput> customersByInput(String input,String clinicId);
		
	public String customerDeviceId(String customerId);
			
	public String getCustomername(String patientId);
//
//	Response resetPassword(ChangeDoctorPasswordDTO dto);

	public String retrievePatientName(String patientId);
	
	public Response getCustomersByHospitalId(String clinicId, String branchId, String searchInput);

	Response importCustomersFromExcel(MultipartFile file);

	public ResponseEntity<Response> getCustomersByClinicId(String clinicId);

	public Response getCustomersByHospitalIdAndClinicId(String clinicId, String searchInput);
}
