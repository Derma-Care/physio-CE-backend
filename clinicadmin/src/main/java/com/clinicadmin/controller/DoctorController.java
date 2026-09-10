package com.clinicadmin.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.clinicadmin.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.clinicadmin.entity.Doctors;
import com.clinicadmin.repository.DoctorsRepository;
//import com.clinicadmin.service.DoctorNoteService;
import com.clinicadmin.service.DoctorService;
import com.clinicadmin.validations.FormatChecks;
import com.clinicadmin.validations.RequiredChecks;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;

@RestController
@RequestMapping("/clinic-admin")
//@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class DoctorController {

	@Autowired
	private DoctorService doctorService;
	
	@Autowired
	private DoctorsRepository doctorRespository;
//	@Autowired
//	private DoctorNoteService doctorNoteService;

	@Autowired
	private Validator validator;

	// ------------------------------Doctor APIs
	// ------------------------------------------------------------------------
	@PostMapping("/addDoctor")
	public ResponseEntity<Response> addDoctor(@RequestBody DoctorsDTO dto) {
		Response response = new Response();

		// Step 1: Validate required fields
		Set<ConstraintViolation<DoctorsDTO>> requiredViolations = validator.validate(dto, RequiredChecks.class);

		if (!requiredViolations.isEmpty()) {
			Map<String, String> errors = new HashMap<>();
			StringBuilder errorMessages = new StringBuilder();

			for (ConstraintViolation<DoctorsDTO> violation : requiredViolations) {
				String field = violation.getPropertyPath().toString();
				String message = violation.getMessage();
				errors.put(field, message);
				errorMessages.append(message).append("; ");
			}

			response.setSuccess(false);
			response.setStatus(HttpStatus.BAD_REQUEST.value());
			response.setMessage(errorMessages.toString());
//	        response.setData(errors);

			return ResponseEntity.badRequest().body(response);
		}

		
		// Step 2: Validate format/other rules

		Set<ConstraintViolation<DoctorsDTO>> formatViolations = validator.validate(dto, FormatChecks.class);

		if (!formatViolations.isEmpty()) {
			Map<String, String> errors = new HashMap<>();
			StringBuilder errorMessages = new StringBuilder();

			for (ConstraintViolation<DoctorsDTO> violation : formatViolations) {
				String field = violation.getPropertyPath().toString();
				String message = violation.getMessage();
				errors.put(field, message);
				errorMessages.append(message).append("; ");
			}

			response.setSuccess(false);
			response.setStatus(HttpStatus.BAD_REQUEST.value());
			response.setMessage(errorMessages.toString());
//	        response.setData(errors);

			return ResponseEntity.badRequest().body(response);
		}

		dto.trimAllDoctorFields();
		response = doctorService.addDoctor(dto);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

//	 // ================= START VERIFICATION =================
//    @PutMapping("/start-verificationForDoctor/{doctorId}")
//    public ResponseEntity<Response> startVerification(@PathVariable String doctorId) {
//
//        Response response = doctorService.startVerificationProcess(doctorId);
//
//        return ResponseEntity.status(response.getStatus()).body(response);
//    }
//
//    // ================= VERIFY DOCTOR =================
//    @PutMapping("/verifyDoctor/{doctorId}")
//    public ResponseEntity<Response> verifyDoctor(@PathVariable String doctorId) {
//
//        Response response = doctorService.verifyDoctor(doctorId);
//
//        return ResponseEntity.status(response.getStatus()).body(response);
//    }
//
//    // ================= REJECT DOCTOR =================
//    @PutMapping("/rejectDoctor/{doctorId}")
//    public ResponseEntity<Response> rejectDoctor(
//            @PathVariable String doctorId,
//            @RequestParam String reason) {
//
//        Response response = doctorService.rejectDoctor(doctorId, reason);
//
//        return ResponseEntity.status(response.getStatus()).body(response);
//    }

	/**
	 * Get all doctors.
	 * 
	 * @return List of all doctors
	 */
	@GetMapping("/doctors")
	public ResponseEntity<Response> getAllDoctors() {
		Response response = doctorService.getAllDoctors();
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	/**
	 * Update doctor details by ID.
	 * 
	 * @param id  Doctor ID
	 * @param dto Updated doctor data
	 * @return Updated doctor response
	 */
	@PutMapping("/updateDoctor/{doctorId}")
	public ResponseEntity<Response> updateDoctorById(@PathVariable String doctorId, @RequestBody DoctorsDTO dto) {
		dto.trimAllDoctorFields();
		Response response = doctorService.upDateDoctorById(doctorId, dto);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	@DeleteMapping("/delete-doctor/{doctorId}")
	public ResponseEntity<Response> deleteDoctorById(@PathVariable String doctorId) {
		Response response = doctorService.deleteDoctorById(doctorId);
		return ResponseEntity.status(response.getStatus()).body(response);

	}

	// Delete doctor from a specific branch
	@DeleteMapping("/delete-doctor/{doctorId}/branch/{branchId}")
	public ResponseEntity<Response> deleteDoctorFromBranch(@PathVariable String doctorId,
			@PathVariable String branchId) {

		Response response = doctorService.deleteDoctorFromBranch(doctorId, branchId);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	@DeleteMapping("/delete-doctors-by-clinic/{clinicId}")
	public ResponseEntity<Response> deleteDoctorsByClinic(@PathVariable String clinicId) {
		Response response = doctorService.deleteDoctorsByClinic(clinicId);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	// ----------------------------Doctor
	// Login----------------------------------------------------------
	@PostMapping("/doctorLogin")
	public ResponseEntity<Response> doctorLogin(@Valid @RequestBody DoctorAndStaffLoginDto loginDTO) {
		Response response = doctorService.login(loginDTO);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	// -------------------------Change
	// Password----------------------------------------------------------
	@PutMapping("/update-password/{username}")
	public ResponseEntity<Response> updatePassword(@PathVariable String username,
			@RequestBody ChangeDoctorPasswordDTO updatePasswordDTO) {

		updatePasswordDTO.setUsername(username); // set the path variable into the DTO
		Response response = doctorService.changePassword(updatePasswordDTO);
		return new ResponseEntity<>(response, HttpStatus.valueOf(response.getStatus()));
	}
	// ----------Get doctor by ID. @PathVariable id Doctor ID @return Doctor
	// details----------------

	@GetMapping("/doctor/{id}")
	public ResponseEntity<Response> getDoctorById(@PathVariable String id) {
		Response response = doctorService.getDoctorById(id);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

//		--------------------Get Doctors by ClinicID and DoctorId---------------------
	@GetMapping("/clinic/{clinicId}/doctor/{doctorId}")
	public ResponseEntity<Response> getDoctorByClinicAndDoctorId(@PathVariable String clinicId,
			@PathVariable String doctorId) {

		Response response = doctorService.getDoctorsByClinicIdAndDoctorId(clinicId, doctorId);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	// ----------------Get Doctors by their Clinic
	// ids--------------------------------------------------
	@GetMapping("doctors/hospitalById/{hospitalId}")
	public ResponseEntity<Response> getDoctorsByHospitalById(@PathVariable String hospitalId) {
		Response response = doctorService.getDoctorsByClinicId(hospitalId);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	@GetMapping("/getDoctorsByHospitalIdAndBranchId/{hospitalId}/{branchId}")
	public ResponseEntity<Response> getDoctorsByHospitalIdAndBranchId(@PathVariable String hospitalId,
			@PathVariable String branchId) {

		Response response = doctorService.getDoctorsByHospitalIdAndBranchId(hospitalId, branchId);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

//	
//	@GetMapping("/getDoctorsByHospitalIdAndBranchId/{hospitalId}/{branchId}")
//	public ResponseEntity<Response> getDoctorsByHospitalAndBranch(
//	        @PathVariable String hospitalId,
//	        @PathVariable String branchId) {
//
//	    Response response = doctorService.getDoctorsByClinicIdAndBranchId(hospitalId, branchId);
//	    return ResponseEntity.status(response.getStatus()).body(response);
//	}

//		------------------Doctor Availability----------------------------------------------------------------------------------------
	@PostMapping("/doctorId/{doctorId}/availability")
	public ResponseEntity<Response> doctorAvailabilityStatus(@PathVariable String doctorId,
			@RequestBody DoctorAvailabilityStatusDTO status) {
		Response response = doctorService.availabilityStatus(doctorId, status);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

//		------------------Get Doctors by hopital id and subServiceId----------------------------------------------------------------------------------------
//	@GetMapping("/doctors/hospital/{hospitalId}/subServiceId/{subServiceId}")
//	public ResponseEntity<Response> getDoctorsBySubServiceId(@PathVariable String hospitalId,
//			@PathVariable String subServiceId) {
//		Response response = doctorService.getDoctorsBySubserviceId(hospitalId, subServiceId);
//		return ResponseEntity.status(response.getStatus()).body(response);
//	}

//	  @GetMapping("doctors/{hospitalId}/{branchId}/{subServiceId}")
//	    public ResponseEntity<Response> getDoctorsByHospitalBranchAndSubService(
//	            @PathVariable String hospitalId,
//	            @PathVariable String branchId,
//	            @PathVariable String subServiceId) {
//
//	        Response response = doctorService.getDoctorsByHospitalIdAndBranchIdSubserviceId(hospitalId, branchId, subServiceId);
//	        return ResponseEntity.status(response.getStatus()).body(response);
//	    }
	/*
	 * -----------------------------------------------------------------------------
	 * -------------------------------------------------
	 * ---------------------------------- Docots
	 * Slots------------------------------------------------------------------------
	 * ---
	 * -----------------------------------------------------------------------------
	 * -------------------------------------------------
	 */

	// ---------------------------- Add Doctor Slot ----------------------------
	@PostMapping("/addDoctorSlots/{hospitalId}/{doctorId}")
	public ResponseEntity<Response> addDoctorSlot(@PathVariable String hospitalId, @PathVariable String doctorId,
			@RequestBody DoctorSlotDTO slotDto) {

		Response response = doctorService.saveDoctorSlot(hospitalId, doctorId, slotDto);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	// --------------------------------update the
	// slot-------------------------------------------
	@PutMapping("/doctor/update-slot")
	public ResponseEntity<Response> updateDoctorSlot(@RequestBody UpdateSlotRequestDTO request) {
		Response response = doctorService.updateDoctorSlot(request.getDoctorId(), request.getDate(),
				request.getOldSlot(), request.getNewSlot());
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	// ----------------------------- Get doctor
	// slots----------------------------------------------------------------------------
	@GetMapping("/getDoctorslots/{hospitalId}/{doctorId}")
	public ResponseEntity<Response> getDoctorSlot(@PathVariable String hospitalId, @PathVariable String doctorId) {
		Response response = doctorService.getDoctorSlots(hospitalId, doctorId);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	// --------------------------delete slot by using time date
	// doctorId-----------------------------------------------------
	@DeleteMapping("/doctorId/{doctorId}/branchId/{branchId}/date/{date}/slot/{slot}")
	public ResponseEntity<Response> deleteDoctorSlot(@PathVariable String doctorId, @PathVariable String branchId,
			@PathVariable String date, @PathVariable String slot) {
		Response response = doctorService.deleteDoctorSlot(doctorId, branchId, date, slot);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	@DeleteMapping("/doctorId/{doctorId}/{date}/{slot}/slots")
	public Response deleteDoctorSlot(@PathVariable String doctorId, @PathVariable String date,
			@PathVariable String slot) {
		return doctorService.deleteDoctorSlot(doctorId, date, slot);
	}

	// ----------------------------------------delete doctor slot by
	// date-------------------------------------------------

	@DeleteMapping("/delete-by-date/{doctorId}/{date}")
	public ResponseEntity<Response> deleteDoctorSlotsByDate(@PathVariable String doctorId, @PathVariable String date) {
		Response response = doctorService.deleteDoctorSlotbyDate(doctorId, date);

		return new ResponseEntity<>(response, HttpStatus.valueOf(response.getStatus()));
	}

	@DeleteMapping("/delete-by-date/{doctorId}/{branchId}/{date}")
	public ResponseEntity<Response> deleteDoctorSlotsByDate(@PathVariable String doctorId,
			@PathVariable String branchId, @PathVariable String date) {

		Response response = doctorService.deleteDoctorSlotbyDate(doctorId, branchId, date);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	@PutMapping("/updateDoctorSlotWhileBooking/{doctorId}/{branchId}/{date}/{time}")
	public boolean updateDoctorSlotWhileBooking(@PathVariable String doctorId, @PathVariable String branchId,
			@PathVariable String date, @PathVariable String time) {
		return doctorService.updateSlot(doctorId, branchId, date, time);
	}

	@PutMapping("/makingFalseDoctorSlot/{doctorId}/{branchId}/{date}/{time}")
	public boolean makingFalseDoctorSlot(@PathVariable String doctorId, @PathVariable String branchId,
			@PathVariable String date, @PathVariable String time) {
		return doctorService.makingFalseDoctorSlot(doctorId, branchId, date, time);
	}

//		---------------------------------------------------------------------------------------------------------------------------
//		---------------------------------------------------------------------------------------------------------------------------

// -------------------------Get Hopitals and Doctors using SubserviceId------------------------------------------------
//	@GetMapping("/getHospitalAndDoctorUsingSubServiceId/{subServiceId}")
//	public ResponseEntity<Response> getHospitalAndDoctorUsingSubServiceId(@PathVariable String subServiceId) {
//		Response response = doctorService.getHospitalAndDoctorsUsingSubserviceId(subServiceId);
//		return ResponseEntity.status(response.getStatus()).body(response);
//	}
//--------------------------- Get ALl Doctors By SubServiceId ----------------------------------------------------------------
//	
//			@GetMapping("/getAllDoctorsBySubServiceId/{subServiceId}")
//			public ResponseEntity<Response> getAllDoctorsBySubServiceId(@PathVariable String subServiceId) {
//				Response response = doctorService.getAllDoctorsBySubserviceId(subServiceId);
//				return ResponseEntity.status(response.getStatus()).body(response);
//			}

	/*
	 * -----------------------------------------------------------------------------
	 * ----------------------------------------------
	 * --------------------------------------------- Notes
	 * -----------------------------------------------------------------------
	 * -----------------------------------------------------------------------------
	 * ----------------------------------------------
	 */

	// -------------------------getAllNotes-----------------------------------------------
//	@GetMapping("/notes/getAllNotes")
//	public ResponseEntity<Response> getAllNotes() {
//		Response response = doctorNoteService.getAllNotes();
//		return ResponseEntity.status(response.getStatus()).body(response);
//	}

	// NOTIFICATION OF DOCTOR

	@GetMapping("/notificationToClinic/{hospitalId}")
	public ResponseEntity<?> notificationTodoctor(@PathVariable String hospitalId) {
		return doctorService.notificationToClinic(hospitalId);
	}

	// -----------------------------GET CLINICS AND DOCTORS BUY RECOMMONDATION ==
	// TRUE---------------------------------
	@GetMapping("/recommendedClinicAndDoctors")
	public ResponseEntity<Response> getClinicAndDoctorUsingRecommondation() {
		Response response = doctorService.getRecommendedClinicsAndDoctors();
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	// -----------------------------Get Best Doctor
	// subserviceId---------------------------------
//		@GetMapping("/bestDoctor/{subServiceId}")
//		public ResponseEntity<Response> getBestDoctors(@PathVariable String subServiceId){
//			Response response=doctorService.getBestDoctorBySubService(subServiceId);
//			return ResponseEntity.status(response.getStatus()).body(response);
//		}
	// ----------------------------- Using key GET CLINICS AND DOCTORS BUY
	// RECOMMONDATION ----------------------------------------
	@GetMapping("/clinics/{keyPoints}")
	public ResponseEntity<Response> getRecommendedClinicsAndDoctors(@PathVariable String keyPoints) {
		// Convert comma-separated values to List
		List<String> keyPointList = Arrays.stream(keyPoints.split(",")).map(String::trim).collect(Collectors.toList());

		Response response = doctorService.getRecommendedClinicsAndDoctors(keyPointList);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

//	----------------- Get All doctors with respective their clinic---------------------
	@GetMapping("/clinics/getAllDoctorWithRespectiveClinics")
	public ResponseEntity<Response> getAllDoctorWithRespectiveClinics() {
		Response response = doctorService.getAllDoctorsWithRespectiveClinic();
		return ResponseEntity.status(response.getStatus()).body(response);

	}

	@GetMapping("/clinics/getAllDoctorWithRespectiveClinics/{consultationType}")
	public ResponseEntity<Response> getClinicsWithDoctors(@PathVariable int consultationType) {
		Response response = doctorService.getAllDoctorsWithRespectiveClinic(consultationType);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	@GetMapping("/clinics/getAllDoctorWithRespectiveClinics/{hospitalId}/{consultationType}")
	public ResponseEntity<Response> getClinicsWithDoctors(@PathVariable String hospitalId,
			@PathVariable int consultationType) {

		Response response = doctorService.getAllDoctorsWithRespectiveClinic(hospitalId, consultationType);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

// --------------------Login By Using roles-------------------
	@PostMapping("/loginUsingRoles")
	public ResponseEntity<Response> loginUsingRoles(@RequestBody DoctorAndStaffLoginDto dto) {
		Response response = doctorService.loginUsingRoles(dto);
		return ResponseEntity.status(response.getStatus()).body(response);

	}
	
//	--------------------If Resetpasword with roles----------------------------------
	
	   @PutMapping("/update-password")
	    public ResponseEntity<Response> changePasswordWithRole(
	            @RequestBody @Valid ClinicStaffUpdatedPassword updateDTO) {

	        Response response = doctorService.changePasswordWithRole(updateDTO);

	        return ResponseEntity.status(response.getStatus()).body(response);
	   }
//		---------------------------------------------Slots using branchId----------------------------------------------
	// ------------------ Generate slots dynamically ------------------
	@GetMapping("/generateDoctorSlots/{doctorId}/{branchId}/{date}/{intervalMinutes}/{openingTime}/{closingTime}")
	public Response generateSlots(@PathVariable String doctorId, @PathVariable String branchId,
			@PathVariable String date, @PathVariable int intervalMinutes, @PathVariable String openingTime,
			@PathVariable String closingTime) {
		return doctorService.generateDoctorSlots(doctorId, branchId, date, intervalMinutes, openingTime, closingTime);
	}

	@PostMapping("/addDoctorSlots/{hospitalId}/{branchId}/{doctorId}")
	public ResponseEntity<Response> addDoctorSlot(@PathVariable String hospitalId, @PathVariable String branchId,
			@PathVariable String doctorId, @RequestBody DoctorSlotDTO slotDto) {

		Response response = doctorService.saveDoctorSlot(hospitalId, branchId, doctorId, slotDto);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	@GetMapping("/getDoctorSlots/{hospitalId}/{branchId}/{doctorId}")
	public ResponseEntity<Response> getDoctorSlot(@PathVariable String hospitalId, @PathVariable String branchId,
			@PathVariable String doctorId) {

		Response response = doctorService.getDoctorSlots(hospitalId, branchId, doctorId);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

// ----------------give best one doctor using key words-----------------------------------------------------------------
	@GetMapping("/getBestDoctorByKeyWords/{keyPoints}")
	public ResponseEntity<Response> getRecommendedClinicsAndOnDoctors(@PathVariable String keyPoints) {
		// Convert comma-separated values into List<String>
		List<String> keyPointList = Arrays.stream(keyPoints.split(",")).map(String::trim).collect(Collectors.toList());

		Response response = doctorService.getRecommendedClinicsAndDoctors(keyPointList);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	@GetMapping("/getBestDoctorByKeyWordsAndConsultation/{keyPoints}/{consultationType}")
	public ResponseEntity<Response> getRecommendedClinicsAndDoctors(@PathVariable String keyPoints,
			@PathVariable int consultationType) {

		// Convert comma-separated keywords into a list
		List<String> keyPointList = Arrays.stream(keyPoints.split(",")).map(String::trim).collect(Collectors.toList());

		// Call service method with consultation type
		Response response = doctorService.getRecommendedClinicsAndDoctors(keyPointList, consultationType);

		// Return proper HTTP response
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	@GetMapping("/getBestDoctorByKeyWords/{hospitalId}/{keyPoints}/{consultationType}")
	public ResponseEntity<Response> getRecommendedClinicsAndDoctors(@PathVariable String hospitalId,
			@PathVariable String keyPoints, @PathVariable int consultationType) {

		// Convert comma-separated key points to List<String>
		List<String> keyPointList = Arrays.stream(keyPoints.split(",")).map(String::trim).collect(Collectors.toList());

		Response response = doctorService.getRecommendedClinicsAndDoctors(hospitalId, keyPointList, consultationType);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	@PostMapping("/block/slot")
	public boolean blockSlot(@RequestBody TempBlockingSlot tempBlockingSlot) {
		return doctorService.blockingSlot(tempBlockingSlot);

	}

	@GetMapping("/doctor/getDeviceId/{doctorId}")
	public String getDeviceId(@PathVariable String doctorId) {

		return doctorService.getDeviceId(doctorId);
	}
	
	
	 @PostMapping("/doctorDetails")
	    public ResponseEntity<Response> getDoctor(
             @RequestBody ClinicLoginRequestDTO clinicLoginRequestDTO) {

	        return doctorService.getDoctor(clinicLoginRequestDTO);
	    }

}