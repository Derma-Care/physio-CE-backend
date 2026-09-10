package com.clinicadmin.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import com.clinicadmin.dto.BookingInfoByInput;
import com.clinicadmin.dto.CustomerLoginDTO;
import com.clinicadmin.dto.CustomerOnbordingDTO;
import com.clinicadmin.dto.Response;
import com.clinicadmin.dto.ResponseStructure;
import com.clinicadmin.entity.CustomerOnbording;
import com.clinicadmin.service.CustomerOnboardingService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/clinic-admin")
//@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class CustomerOnboardingController {

	@Autowired
	private CustomerOnboardingService customerOnboardingService;
	// ✅ Create / Onboard Customer
    @PostMapping("/customers/onboard")
    public ResponseEntity<Response> onboardCustomer(@Valid @RequestBody CustomerOnbordingDTO dto) {
        Response response = customerOnboardingService.onboardCustomer(dto);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
    
 // Excel Customer Import
    @PostMapping(
            value = "/customers/import-excel",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Response> importCustomersFromExcel(
            @RequestParam("file") MultipartFile file) {

        Response response =
                customerOnboardingService
                        .importCustomersFromExcel(file);

        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }
    // ✅ Get All Customers
    @GetMapping("/customers/getAllCustomers")
    public ResponseEntity<Response> getAllCustomers() {
        Response response = customerOnboardingService.getAllCustomers();
        return ResponseEntity.status(response.getStatus()).body(response);
    }
    
    // ✅ Get All Customers
    @GetMapping("/customers/mobilenumber/{mobilenumber}/name/{name}")
    public  Map<String,String> getCustomerByMobilenumberAndName(@PathVariable String mobilenumber,@PathVariable String name){
        return customerOnboardingService.getCustomerByMobilenumberAndName(mobilenumber, name);
    }
    
    

    // ✅ Get Customers by HospitalId
    @GetMapping("/customers/hospital/{hospitalId}/{branchId}")
    public ResponseEntity<Response> getCustomersByHospitalId(@PathVariable String hospitalId,@PathVariable String branchId) {
        Response response = customerOnboardingService.getCustomersByHospitalId(hospitalId,branchId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    // ✅ Get Customers by BranchId
    @GetMapping("/customers/branch/{branchId}")
    public ResponseEntity<Response> getCustomersByBranchId(@PathVariable String branchId) {
        Response response = customerOnboardingService.getCustomersByBranchId(branchId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    // ✅ Get Customers by HospitalId & BranchId
    @GetMapping("/customers/hospital/{hospitalId}/branch/{branchId}")
    public ResponseEntity<Response> getCustomersByHospitalIdAndBranchId(@PathVariable String hospitalId,
                                                                        @PathVariable String branchId) {
        Response response = customerOnboardingService.getCustomersByHospitalIdAndBranchId(hospitalId, branchId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    // ✅ Get Customer By ID   
    @GetMapping("/customers/id/{customerId}")
    public ResponseEntity<Response> getCustomerById(@PathVariable String customerId) {
        Response response = customerOnboardingService.getCustomerById(customerId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
    
    @GetMapping("/customers/mobileNumber/{mobileNumber}")
    public ResponseEntity<Response> getCustomerByMobileNumber(@PathVariable String mobileNumber) {
        Response response = customerOnboardingService.getCustomerByMobiileNumber(mobileNumber);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
    
    @GetMapping("/customer/mobilenumber/{mobilenumber}/{clinicId}")
    public CustomerOnbordingDTO getCustomerByMobileNumberAndClinicId(@PathVariable String mobilenumber,@PathVariable String clinicId) {
    	CustomerOnbordingDTO response = customerOnboardingService.getCustomerByMobileNumberAndClinicId(mobilenumber,clinicId);
        return response;
    }
    
    
    @GetMapping("/customer/patientId/{patientId}/{clinicId}")
    public ResponseEntity<Response> getCustomerByPatientId(@PathVariable String patientId,@PathVariable String clinicId) {
        Response response = customerOnboardingService.getCustomersByPatientId(patientId,clinicId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    // ✅ Update Customer
    @PutMapping("/customers/updatecustomer/{customerId}")
    public ResponseEntity<Response> updateCustomer(@PathVariable String customerId,
                                                   @RequestBody CustomerOnbordingDTO dto) {
        Response response = customerOnboardingService.updateCustomer(customerId, dto);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    // ✅ Delete Customer
    @DeleteMapping("/customers/deletecustomer/{customerId}")
    public ResponseEntity<Response> deleteCustomer(@PathVariable String customerId) {
        Response response = customerOnboardingService.deleteCustomer(customerId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    // ✅ Login
    @PostMapping("/customers/login")
    public ResponseEntity<Response> login(@RequestBody CustomerLoginDTO dto) {
        Response response = customerOnboardingService.login(dto);
        return ResponseEntity.status(response.getStatus()).body(response);
    }


//	// ✅ Reset Password with PathVariable
//	@PostMapping("/customers/reset-password/{username}/{oldPassword}/{newPassword}")
//	public ResponseEntity<Response> resetPassword(@PathVariable String username, @PathVariable String oldPassword,
//			@PathVariable String newPassword) {
//		Response response = customerOnboardingService.resetPassword(username, oldPassword, newPassword);
//		return ResponseEntity.status(response.getStatus()).body(response);
//	}
    
    
    @GetMapping("/gcmToken/{token}")
    public CustomerOnbordingDTO getCustomerByToken(
 			 @PathVariable String token ){
 	   return customerOnboardingService.getCustomerByToken(token);
  }
    
    @GetMapping("/deviceIdByCustomerId/{customerId}")
    public String customerDeviceId(
 			 @PathVariable String customerId ){
 	   return customerOnboardingService.customerDeviceId(customerId);
  } 
    
    @GetMapping("/customername/{id}")
    public String getCustomername(
 			 @PathVariable String id ){
 	   return customerOnboardingService.getCustomername(id);
  }

    @GetMapping("/patinetname/{id}")
    public String getPatientname(
            @PathVariable String id ){
        return customerOnboardingService.retrievePatientName(id);
    }


    @GetMapping("/bookings/byInput/{input}/{clinicId}")	
			public ResponseEntity<?> retrieveAppointnmentsByInput(@PathVariable String input,@PathVariable String clinicId){
    	List<BookingInfoByInput> response = customerOnboardingService.customersByInput(input,clinicId);
				if (response == null) {
					return new ResponseEntity<>(ResponseStructure.buildResponse(null,
							"No booking yet" + input, HttpStatus.OK, HttpStatus.OK.value()),
							HttpStatus.OK);}
				return new ResponseEntity<>(ResponseStructure.buildResponse(response,
						"Booking fetched sucessfully on clinicId" + input, HttpStatus.OK, HttpStatus.OK.value()),
						HttpStatus.OK);}
		
    // Example: GET /api/customers/search?clinicId=C001&branchId=B001&searchInput=rakesh
    @GetMapping("/customer/searchInput/{clinicId}/{branchId}/{searchInput}")
    public ResponseEntity<Response> getCustomersByHospitalId(
            @PathVariable String clinicId,
            @PathVariable String branchId,
            @PathVariable String searchInput) {

        Response response = customerOnboardingService.getCustomersByHospitalId(clinicId, branchId, searchInput);
        return ResponseEntity.status(response.getStatus()).body(response);
    }


    @GetMapping("/customers/clinicId/{clinicId}")
    public ResponseEntity<Response> getCustomersByClinicId(@PathVariable String clinicId) {
        return customerOnboardingService.getCustomersByClinicId(clinicId);

    }


    @GetMapping("/customer/searchInput/clinicId/{clinicId}/{searchInput}")
    public ResponseEntity<Response> getCustomersByHospitalIdAndSeachInput(
            @PathVariable String clinicId,
            @PathVariable String searchInput) {

        Response response = customerOnboardingService.getCustomersByHospitalIdAndClinicId(clinicId, searchInput);
        return ResponseEntity.status(response.getStatus()).body(response);
    }


}
