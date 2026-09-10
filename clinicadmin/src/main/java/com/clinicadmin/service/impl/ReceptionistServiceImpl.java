package com.clinicadmin.service.impl;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.clinicadmin.dto.Branch;
import com.clinicadmin.dto.DashboardRequest;
import com.clinicadmin.dto.ReceptionistRequestDTO;
import com.clinicadmin.dto.Response;
import com.clinicadmin.dto.ResponseStructure;
import com.clinicadmin.entity.DoctorAndStaffLoginCredentials;
import com.clinicadmin.entity.ReceptionistEntity;
import com.clinicadmin.feignclient.AdminServiceClient;
import com.clinicadmin.feignclient.BookingFeign;
import com.clinicadmin.repository.DoctorLoginCredentialsRepository;
import com.clinicadmin.repository.ReceptionistRepository;
import com.clinicadmin.service.ReceptionistService;
import com.clinicadmin.utils.ReceptionistMapper;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ReceptionistServiceImpl implements ReceptionistService {
	private static final Logger log = LoggerFactory.getLogger(ReceptionistServiceImpl.class);

	@Autowired
	private ReceptionistRepository repository;

	@Autowired
	private DoctorLoginCredentialsRepository credentialsRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	AdminServiceClient adminServiceClient;

	@Autowired
	private BookingFeign bookingFeign;

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	private DoctorServiceImpl doctorServiceImpl;

	@Override
	public ResponseStructure<ReceptionistRequestDTO> createReceptionist(ReceptionistRequestDTO dto) {
		log.info("Create Receptionist request | contactNumber={}, branchId={}", dto.getContactNumber(),
				dto.getBranchId());

		if (repository.existsByContactNumber(dto.getContactNumber())) {
			log.warn("Receptionist already exists with contactNumber={}", dto.getContactNumber());

			return ResponseStructure.buildResponse(null, "Receptionist with this contact number already exists",
					HttpStatus.CONFLICT, HttpStatus.CONFLICT.value());
		}
		if (credentialsRepository.existsByUsername(dto.getContactNumber())) {
			log.warn("Login credentials already exist | username={}", dto.getContactNumber());

			return ResponseStructure.buildResponse(null, "Login credentials already exist for this mobile number",
					HttpStatus.CONFLICT, HttpStatus.CONFLICT.value());
		}
		log.info("Fetching branch details via Admin Service | branchId={}", dto.getBranchId());

		ResponseEntity<Response> res = adminServiceClient.getBranchById(dto.getBranchId());
		Branch br = objectMapper.convertValue(res.getBody().getData(), Branch.class);

		ReceptionistEntity entity = ReceptionistMapper.toEntity(dto);
		entity.setId(generateReceptionistId());
		entity.setBranchName(br.getBranchName());

		ReceptionistEntity saved = repository.save(entity);
		log.info("Receptionist saved | receptionistId={}", saved.getId());

		String username = dto.getContactNumber();
		String rawPassword = generateStructuredPassword();
		String encodedPassword = passwordEncoder.encode(rawPassword);

		DoctorAndStaffLoginCredentials credentials = DoctorAndStaffLoginCredentials.builder().staffId(saved.getId())
				.staffName(saved.getFullName()).hospitalId(saved.getClinicId()).hospitalName(saved.getHospitalName())
				.emailId(saved.getEmailId()).mobilenumber(saved.getContactNumber()).branchId(saved.getBranchId())
				.branchName(saved.getBranchName()).username(username).password(encodedPassword).role(dto.getRole())
				.permissions(saved.getPermissions()).build();

		credentialsRepository.save(credentials);

		log.info("Login credentials created | receptionistId={}", saved.getId());

		ReceptionistRequestDTO responseDTO = ReceptionistMapper.toDTO(saved);
		responseDTO.setBranchName(saved.getBranchName());
		responseDTO.setUserName(username);
		responseDTO.setPassword(rawPassword); // expose only on create

		return ResponseStructure.buildResponse(responseDTO, "Receptionist created successfully", HttpStatus.CREATED,
				HttpStatus.CREATED.value());
	}

	@Override
	public ResponseStructure<ReceptionistRequestDTO> getReceptionistById(String id) {
		log.info("Fetching Receptionist by id={}", id);

		Optional<ReceptionistEntity> optional = repository.findById(id);
		if (optional.isEmpty()) {
			log.warn("Receptionist not found | id={}", id);

			return ResponseStructure.buildResponse(null, "Receptionist not found", HttpStatus.NOT_FOUND,
					HttpStatus.NOT_FOUND.value());
		}
		log.info("Receptionist found | id={}", id);

		return ResponseStructure.buildResponse(ReceptionistMapper.toDTO(optional.get()),
				"Receptionist retrieved successfully", HttpStatus.OK, HttpStatus.OK.value());
	}

	@Override
	public ResponseStructure<List<ReceptionistRequestDTO>> getAllReceptionists() {
		log.info("Fetching all Receptionists");

		List<ReceptionistEntity> entities = repository.findAll();
		List<ReceptionistRequestDTO> dtos = entities.stream().map(ReceptionistMapper::toDTO)
				.collect(Collectors.toList());
		log.info("Receptionists fetch completed | count={}", dtos.size());

		return ResponseStructure.buildResponse(dtos,
				dtos.isEmpty() ? "No Receptionists found" : "Receptionists retrieved successfully", HttpStatus.OK,
				HttpStatus.OK.value());
	}

	@Override
	public ResponseStructure<ReceptionistRequestDTO> updateReceptionist(String id, ReceptionistRequestDTO dto) {
		log.info("Update Receptionist request | receptionistId={}", id);

		Optional<ReceptionistEntity> optional = repository.findById(id);
		if (optional.isEmpty()) {
			log.warn("Receptionist not found for update | receptionistId={}", id);

			return ResponseStructure.buildResponse(null, "Receptionist not found", HttpStatus.NOT_FOUND,
					HttpStatus.NOT_FOUND.value());
		}

		ReceptionistEntity existing = optional.get();

		// 🔹 Update normal fields
		if (dto.getFullName() != null)
			existing.setFullName(dto.getFullName());
		if (dto.getHospitalName() != null)
			existing.setHospitalName(dto.getHospitalName());
		if (dto.getRole() != null)
			existing.setRole(dto.getRole());
		if (dto.getBranchId() != null)
			existing.setBranchId(dto.getBranchId());
		if (dto.getDateOfBirth() != null)
			existing.setDateOfBirth(dto.getDateOfBirth());
		if (dto.getContactNumber() != null)
			existing.setContactNumber(dto.getContactNumber());
		if (dto.getQualification() != null)
			existing.setQualification(dto.getQualification());
		if (dto.getGovernmentId() != null)
			existing.setGovernmentId(dto.getGovernmentId());
		if (dto.getDateOfJoining() != null)
			existing.setDateOfJoining(dto.getDateOfJoining());
		if (dto.getDepartment() != null)
			existing.setDepartment(dto.getDepartment());
		if (dto.getAddress() != null)
			existing.setAddress(dto.getAddress());
		if (dto.getEmergencyContact() != null)
			existing.setEmergencyContact(dto.getEmergencyContact());
		if (dto.getPermissions() != null)
			existing.setPermissions(dto.getPermissions());
		if (dto.getBankAccountDetails() != null)
			existing.setBankAccountDetails(dto.getBankAccountDetails());
		if (dto.getEmailId() != null)
			existing.setEmailId(dto.getEmailId());
		if (dto.getPreviousEmploymentHistory() != null)
			existing.setPreviousEmploymentHistory(dto.getPreviousEmploymentHistory());
		if (dto.getShiftTimingsOrAvailability() != null)
			existing.setShiftTimingsOrAvailability(dto.getShiftTimingsOrAvailability());

		// 🔹 Update Base64 fields (PDF/Image)
		if (dto.getProfilePicture() != null)
			existing.setProfilePicture(encodeIfNotBase64(dto.getProfilePicture()));
		if (dto.getGraduationCertificate() != null)
			existing.setGraduationCertificate(encodeIfNotBase64(dto.getGraduationCertificate()));
		if (dto.getComputerSkillsProof() != null)
			existing.setComputerSkillsProof(encodeIfNotBase64(dto.getComputerSkillsProof()));
		existing.setUpdatedDate(LocalDate.now().toString());
		// 🔹 Save receptionist entity
		ReceptionistEntity updated = repository.save(existing);
		log.info("Receptionist updated successfully | receptionistId={}", updated.getId());

		// 🔹 Sync with DoctorLoginCredentials using receptionist.id
		Optional<DoctorAndStaffLoginCredentials> credsOpt = credentialsRepository.findByStaffId(updated.getId());
		if (credsOpt.isPresent()) {

			log.info("Syncing login credentials | receptionistId={}", updated.getId());

			DoctorAndStaffLoginCredentials creds = credsOpt.get();

			if (updated.getFullName() != null)
				creds.setStaffName(updated.getFullName());

			if (updated.getBranchId() != null)
				creds.setBranchId(updated.getBranchId());

			if (updated.getBranchName() != null)
				creds.setBranchName(updated.getBranchName());

			if (updated.getClinicId() != null)
				creds.setHospitalId(updated.getClinicId());

			if (updated.getHospitalName() != null)
				creds.setHospitalName(updated.getHospitalName());

			if (updated.getRole() != null)
				creds.setRole(updated.getRole());

			if (updated.getPermissions() != null)
				creds.setPermissions(updated.getPermissions());

			if (updated.getContactNumber() != null)
				creds.setMobilenumber(updated.getContactNumber());

			if (updated.getEmailId() != null)
				creds.setEmailId(updated.getEmailId());

			credentialsRepository.save(creds);

			doctorServiceImpl.updatePermissions(optional.get().getContactNumber(),dto.getPermissions());

			log.info("Login credentials synced successfully | receptionistId={}", updated.getId());
		}

		return ResponseStructure.buildResponse(ReceptionistMapper.toDTO(updated), "Receptionist updated successfully",
				HttpStatus.OK, HttpStatus.OK.value());
	}

	/**
	 * Utility method to encode string to Base64 only if not already encoded.
	 */
	private String encodeIfNotBase64(String input) {
		try {
			Base64.getDecoder().decode(input); // already Base64
			return input;
		} catch (IllegalArgumentException e) {
			return Base64.getEncoder().encodeToString(input.getBytes(StandardCharsets.UTF_8));
		}
	}

	@Override
	public ResponseStructure<String> deleteReceptionist(String id) {
		log.info("Delete Receptionist request | receptionistId={}", id);

		try {
			Optional<ReceptionistEntity> optional = repository.findById(id);
			if (optional.isEmpty()) {
				log.warn("Receptionist not found for delete | receptionistId={}", id);

				return ResponseStructure.buildResponse(null, "Receptionist not found", HttpStatus.NOT_FOUND,
						HttpStatus.NOT_FOUND.value());
			}

			// ✅ Delete receptionist record
			repository.deleteById(id);
			log.info("Receptionist deleted | receptionistId={}", id);

			// ✅ Delete corresponding login credentials (if any)
			Optional<DoctorAndStaffLoginCredentials> credentials = credentialsRepository.findByStaffId(id);
			if (credentials.isPresent()) {
				credentialsRepository.deleteById(credentials.get().getId());
				log.info("Login credentials deleted | receptionistId={}", id);

			}

			return ResponseStructure.buildResponse(id, "Receptionist and credentials deleted successfully",
					HttpStatus.OK, HttpStatus.OK.value());

		} catch (Exception e) {
			log.error("Error deleting receptionist | receptionistId={}", id, e);

			return ResponseStructure.buildResponse(null, "Error deleting receptionist: " + e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}

//    @Override
//    public OnBoardResponse login(String userName, String password) {
//        Optional<ReceptionistEntity> optional = repository.findByUserName(userName);
//
//        if (optional.isEmpty() || !optional.get().getPassword().equals(password)) {
//            return new OnBoardResponse(
//                    "Invalid username or password",
//                    HttpStatus.UNAUTHORIZED,
//                    HttpStatus.UNAUTHORIZED.value(),
//                    null,
//                    null
//            );
//        }
//
//        ReceptionistEntity user = optional.get();
//
//        // ✅ Wrap permissions inside the role
//        Map<String, Map<String, List<String>>> wrappedPermissions = Map.of(
//            user.getRole(), user.getPermissions()
//        );
//
//        return new OnBoardResponse(
//                "Login successful",
//                HttpStatus.OK,
//                HttpStatus.OK.value(),
//                user.getRole(),
//                wrappedPermissions
//        );
//    }

//    @Override
//    public ResponseStructure<String> resetPassword(String contactNumber, ReceptionistRestPassword request) {
//        ReceptionistEntity entity = repository.findByContactNumber(contactNumber)
//                .orElseThrow(() -> new RuntimeException("Receptionist not found with contact number: " + contactNumber));
//
//        ResponseStructure<String> response = new ResponseStructure<>();
//
//        if (!entity.getPassword().equals(request.getCurrentpassword())) {
//            response.setData(null);
//            response.setMessage("Current password is incorrect");
//            response.setHttpStatus(HttpStatus.BAD_REQUEST);
//            return response;
//        }
//
//        if (!request.getNewPassword().equals(request.getConformPassword())) {
//            response.setData(null);
//            response.setMessage("New password and Confirm password do not match");
//            response.setHttpStatus(HttpStatus.BAD_REQUEST);
//            return response;
//        }
//
//        entity.setPassword(request.getNewPassword());
//        repository.save(entity);
//
//        response.setData("Password updated successfully");
//        response.setMessage("Success");
//        response.setHttpStatus(HttpStatus.OK);
//        return response;
//    }

	// ----------------- Helper methods -------------------
	private String generateReceptionistId() {
		return "REC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
	}

	private String generateStructuredPassword() {
		String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%";
		SecureRandom random = new SecureRandom();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 10; i++) {
			sb.append(chars.charAt(random.nextInt(chars.length())));
		}
		return sb.toString();
	}

	@Override
	public ResponseStructure<List<ReceptionistRequestDTO>> getReceptionistsByClinic(String clinicId) {
		log.info("Fetching Receptionists by clinicId={}", clinicId);

		List<ReceptionistEntity> entities = repository.findByClinicId(clinicId);
		List<ReceptionistRequestDTO> dtos = entities.stream().map(ReceptionistMapper::toDTO)
				.collect(Collectors.toList());
		log.info("Receptionists fetched | clinicId={}, count={}", clinicId, dtos.size());

		return ResponseStructure.buildResponse(dtos, dtos.isEmpty() ? "No receptionists found for clinic " + clinicId
				: "Receptionists retrieved successfully", HttpStatus.OK, HttpStatus.OK.value());
	}

	@Override
	public ResponseStructure<ReceptionistRequestDTO> getReceptionistByClinicAndId(String clinicId,
			String receptionistId) {
		log.info("Fetching Receptionist | clinicId={}, receptionistId={}", clinicId, receptionistId);

		ReceptionistEntity entity = repository.findByClinicIdAndId(clinicId, receptionistId)
				.orElseThrow(() -> new RuntimeException("Receptionist not found with clinicId: " + clinicId
						+ " and receptionistId: " + receptionistId));

		log.debug("Receptionist entity fetched successfully | receptionistId={}", receptionistId);

		ReceptionistRequestDTO dto = ReceptionistMapper.toDTO(entity);

		log.info("Receptionist fetched successfully | clinicId={}, receptionistId={}", clinicId, receptionistId);

		return ResponseStructure.<ReceptionistRequestDTO>builder().statusCode(200)
				.message("Receptionist data fetched successfully").data(dto).build();
	}

	@Override
	public ResponseStructure<List<ReceptionistRequestDTO>> getReceptionistsByClinicAndBranch(String clinicId,
			String branchId) {
		log.info("Fetching Receptionists | clinicId={}, branchId={}", clinicId, branchId);

		// Fetch receptionist entities from repository by clinicId and branchId
		List<ReceptionistEntity> entities = repository.findByClinicIdAndBranchId(clinicId, branchId);

		// Map entities to DTOs
		List<ReceptionistRequestDTO> dtos = entities.stream().map(ReceptionistMapper::toDTO)
				.collect(Collectors.toList());
		log.info("Receptionists fetched | clinicId={}, branchId={}, count={}", clinicId, branchId, dtos.size());

		// Build response
		String message = dtos.isEmpty() ? "No receptionists found for clinic " + clinicId + " and branch " + branchId
				: "Receptionists retrieved successfully";

		return ResponseStructure.buildResponse(dtos, message, HttpStatus.OK, HttpStatus.OK.value());
	}

	@Override
	public ResponseEntity<Response> getReceptionistDashboard(
	        String clinicId,
	        String branchId) {

	    // Today's bookings
	    ResponseEntity<ResponseStructure<List<Map<String, Object>>>> response =
	            bookingFeign.getTodayBookings(clinicId, branchId);

	    List<Map<String, Object>> bookings = Collections.emptyList();

	    if (response != null
	            && response.getBody() != null
	            && response.getBody().getData() != null) {
	        bookings = response.getBody().getData();
	    }

	    long pending = 0;
	    long confirmed = 0;
	    long sitting = 0;
	    long followUp = 0;

	    for (Map<String, Object> booking : bookings) {

	        String bookingStatus = booking.get("status") != null
	                ? booking.get("status").toString().trim()
	                : "";

	        String visitType = booking.get("visitType") != null
	                ? booking.get("visitType").toString().trim()
	                : "";

	        if ("pending".equalsIgnoreCase(bookingStatus)) {
	            pending++;
	        }

	        if ("confirmed".equalsIgnoreCase(bookingStatus)) {
	            confirmed++;
	        }

	        if ("sitting".equalsIgnoreCase(visitType)) {
	            sitting++;
	        }

	        if ("follow-up".equalsIgnoreCase(visitType)
	                || "followup".equalsIgnoreCase(visitType)) {
	            followUp++;
	        }
	    }

	    Map<String, Object> dashboard = new LinkedHashMap<>();

	    dashboard.put("clinicId", clinicId);
	    dashboard.put("branchId", branchId);
	

	    // Default status since receptionist is no longer fetched
	    dashboard.put("status", false);

	    dashboard.put("pending", pending);
	    dashboard.put("confirmed", confirmed);
	    dashboard.put("sitting", sitting);
	    dashboard.put("followUp", followUp);

	    Response res = new Response();
	    res.setSuccess(true);
	    res.setData(dashboard);
	    res.setMessage("Dashboard data fetched successfully");
	    res.setStatus(HttpStatus.OK.value());

	    return ResponseEntity.ok(res);
	}
	@Override
	public Response updateReceptionistDashboard(String clinicId, String branchId, String role,
			DashboardRequest request) {

		ReceptionistEntity receptionist = repository
				.findByClinicIdAndBranchIdAndRoleIgnoreCase(clinicId, branchId, role)
				.orElseThrow(() -> new RuntimeException("Receptionist not found"));

		receptionist.setDashboardStatus(request.getStatus());

		repository.save(receptionist);

		Map<String, Object> dashboard = new LinkedHashMap<>();

		dashboard.put("clinicId", clinicId);
		dashboard.put("branchId", branchId);
		dashboard.put("role", role);
		dashboard.put("status", receptionist.getDashboardStatus());

		Response res = new Response();
		res.setSuccess(true);
		res.setData(dashboard);
		res.setMessage("Dashboard updated successfully");
		res.setStatus(HttpStatus.OK.value());

		return res;
	}
}