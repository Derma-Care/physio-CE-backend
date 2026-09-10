package com.clinicadmin.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clinicadmin.dto.ChangeDoctorPasswordDTO;
import com.clinicadmin.dto.Response;
import com.clinicadmin.dto.ResponseStructure;
import com.clinicadmin.dto.TherapistDTO;
import com.clinicadmin.dto.TherapistPresenceRequest;
import com.clinicadmin.service.DoctorService;
import com.clinicadmin.service.TherapistService;

@RestController
@RequestMapping("/clinic-admin")
public class TherapistController {

	@Autowired
	private TherapistService service;

	@Autowired
	private DoctorService doctorService;

	// ================= CREATE =================
	@PostMapping("/addTherapist")
	public ResponseEntity<Response> createTherapist(@RequestBody TherapistDTO dto) {

		Response response = service.therapistOnboarding(dto);

		return ResponseEntity.status(response.getStatus()).body(response);
	}

	@GetMapping("/therapist/deviceId/{deviceId}")
	public String retrivetherapistDeviceId(@PathVariable String deviceId) {

		return doctorService.getByTherapistDeviceId(deviceId);
	}

//    // ================= LOGIN =================
//    @PostMapping("/login")
//    public ResponseEntity<ResponseStructure<TherapistLoginResponseDTO>> login(
//            @RequestBody TherapistLoginDTO dto) {
//
//        ResponseStructure<TherapistLoginResponseDTO> response = service.login(dto);
//
//        return ResponseEntity.status(response.getHttpStatus())
//                .body(response);
//    }

	// ================= GET BY therapistId =================
	@GetMapping("/getByTherapistId/{therapistId}")
	public ResponseEntity<ResponseStructure<TherapistDTO>> getByTherapistId(@PathVariable String therapistId) {

		ResponseStructure<TherapistDTO> response = service.getBytherapistId(therapistId);

		return ResponseEntity.status(response.getHttpStatus()).body(response);
	}

	// ================= GET BY CLINIC + BRANCH =================
	@GetMapping("/getByTherapistClinicIdAndBranchId/{clinicId}/{branchId}")
	public ResponseEntity<ResponseStructure<List<TherapistDTO>>> getByClinicIdAndBranchId(@PathVariable String clinicId,
			@PathVariable String branchId) {

		ResponseStructure<List<TherapistDTO>> response = service.getByClinicIdAndBranchId(clinicId, branchId);

		return ResponseEntity.status(response.getHttpStatus()).body(response);
	}

	// ================= GET Threapistdata by clinicId and Branch Id with required
	// field=================
	@GetMapping("/getTherapistWithRequiredFileds/{clinicId}/{branchId}")
	public ResponseEntity<Response> getTherapistWithRequiredFileds(@PathVariable String clinicId,
			@PathVariable String branchId) {

		Response response = service.getTherapistData(clinicId, branchId);

		return ResponseEntity.status(response.getStatus()).body(response);
	}

	// ================= GET BY CLINIC + BRANCH + THERAPIST =================
	@GetMapping("/getByClinicIdBranchIdAndTherapistId/{clinicId}/{branchId}/{therapistId}")
	public ResponseEntity<ResponseStructure<List<TherapistDTO>>> getByClinicIdBranchIdAndTherapistId(
			@PathVariable String clinicId, @PathVariable String branchId, @PathVariable String therapistId) {

		ResponseStructure<List<TherapistDTO>> response = service.getByClinicIdBranchIdAndTherapistId(clinicId, branchId,
				therapistId);

		return ResponseEntity.status(response.getHttpStatus()).body(response);
	}

	// ================= UPDATE =================
	@PutMapping("/updateByTherapistId/{therapistId}")
	public ResponseEntity<ResponseStructure<TherapistDTO>> updateByTherapistId(@PathVariable String therapistId,
			@RequestBody TherapistDTO dto) {

		ResponseStructure<TherapistDTO> response = service.updateBytherapistId(therapistId, dto);

		return ResponseEntity.status(response.getHttpStatus()).body(response);
	}

	// ================= DELETE =================
	@DeleteMapping("/deleteByTherapistId/{therapistId}")
	public ResponseEntity<ResponseStructure<String>> deleteByTherapistId(@PathVariable String therapistId) {

		ResponseStructure<String> response = service.deleteBytherapistId(therapistId);

		return ResponseEntity.status(response.getHttpStatus()).body(response);
	}

	// ================= GET ONLY PAID SESSIONS =================
	@GetMapping("/getPaidSessionsByClinicIdBranchIdBookingIdAndTherapistRecordId/{clinicId}/{branchId}/{bookingId}/{therapistRecordId}")
	public ResponseEntity<Response> getPaidSessions(@PathVariable String clinicId, @PathVariable String branchId,
			@PathVariable String bookingId, @PathVariable String therapistRecordId) {

		Response response = service.getPaidSessions(clinicId, branchId, bookingId, therapistRecordId);

		return ResponseEntity.status(response.getStatus()).body(response);
	}

	// ================= GET THERAPIST PERFORMANCE SUMMARY =================
	@GetMapping("/getTherapistPerformanceSummary/{clinicId}/{branchId}/{therapistId}/{year}")
	public ResponseEntity<Response> getTherapistPerformanceSummary(@PathVariable String clinicId,
			@PathVariable String branchId, @PathVariable String therapistId, @PathVariable int year) {

		Response response = service.getTherapistPerformanceSummary(clinicId, branchId, therapistId, year);

		return ResponseEntity.status(response.getStatus()).body(response);
	}

	// ================= UPDATE THERAPIST PRESENCE =================
	@PutMapping("/updateTherapistPresence/{therapistId}")
	public ResponseEntity<Response> updateTherapistPresence(@PathVariable String therapistId,
			@RequestBody TherapistPresenceRequest request) {

		Response response = service.updateTherapistPresence(therapistId, request);

		return ResponseEntity.status(response.getStatus()).body(response);
	}

	// ================= GET THERAPIST FEEDBACK =================
	@GetMapping("/getTherapistFeedback/{clinicId}/{branchId}/{therapistId}")
	public ResponseEntity<Response> getTherapistFeedback(@PathVariable String clinicId, @PathVariable String branchId,
			@PathVariable String therapistId) {

		Response response = service.getTherapistFeedback(clinicId, branchId, therapistId);

		return ResponseEntity.status(response.getStatus()).body(response);
	}

	@GetMapping("/getTherapistsWithServices/{clinicId}/{branchId}")
	public ResponseEntity<Response> getTherapistsWithServices(@PathVariable String clinicId,
			@PathVariable String branchId) {

		Response response = service.getTherapistsWithServices(clinicId, branchId);

		return ResponseEntity.status(response.getStatus()).body(response);
	}

	@PutMapping("/therapist/update-password/{username}")
	public ResponseEntity<Response> updatePassword(@PathVariable String username,
			@RequestBody ChangeDoctorPasswordDTO updatePasswordDTO) {

		updatePasswordDTO.setUsername(username);

		Response response = service.changePassword(updatePasswordDTO);

		return ResponseEntity.status(response.getStatus()).body(response);
	}
}