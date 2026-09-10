package com.clinicadmin.service.impl;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.clinicadmin.dto.Branch;
import com.clinicadmin.dto.Response;
import com.clinicadmin.dto.ResponseStructure;
import com.clinicadmin.dto.WardBoyDTO;
import com.clinicadmin.entity.DoctorAndStaffLoginCredentials;
import com.clinicadmin.entity.WardBoy;
import com.clinicadmin.feignclient.AdminServiceClient;
import com.clinicadmin.repository.DoctorLoginCredentialsRepository;
import com.clinicadmin.repository.WardBoyRepository;
import com.clinicadmin.service.WardBoyService;
import com.clinicadmin.utils.WardBoyMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class WardBoyServiceImpl implements WardBoyService {
	private static final Logger log = LoggerFactory.getLogger(WardBoyServiceImpl.class);

	@Autowired
	private WardBoyRepository wardBoyRepository;
	@Autowired
	private DoctorLoginCredentialsRepository credentialsRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	AdminServiceClient adminServiceClient;

	@Autowired
	ObjectMapper objectMapper;

	private static final String CHAR_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
	private static final SecureRandom random = new SecureRandom();

	private String generateWardBoyId() {
		return "WB_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
	}

	@Override
	public ResponseStructure<WardBoyDTO> addWardBoy(WardBoyDTO dto) {
		log.info("Add WardBoy started | clinicId={}, branchId={}, contact={}", dto.getClinicId(), dto.getBranchId(),
				dto.getContactNumber());

		if (wardBoyRepository.findByContactNumber(dto.getContactNumber()).isPresent()) {
			log.warn("WardBoy already exists | contactNumber={}", dto.getContactNumber());
			return ResponseStructure.buildResponse(null,
					"WardBoy already exists with contact number : " + dto.getContactNumber(), HttpStatus.CONFLICT,
					HttpStatus.CONFLICT.value());
		}
		if (credentialsRepository.existsByUsername(dto.getContactNumber())) {
			log.warn("Login credentials already exist | username={}", dto.getContactNumber());
			return ResponseStructure.buildResponse(null, "Login credentials already exist for this mobile number",
					HttpStatus.CONFLICT, HttpStatus.CONFLICT.value());
		}
		log.info("Fetching branch details via Admin Service | branchId={}", dto.getBranchId());
		ResponseEntity<Response> res = adminServiceClient.getBranchById(dto.getBranchId());
		Branch br = objectMapper.convertValue(res.getBody().getData(), Branch.class);

		WardBoy wardBoy = WardBoyMapper.toEntity(dto);
		wardBoy.setBranchName(br.getBranchName());
		wardBoy.setWardBoyId(generateWardBoyId());

		WardBoy saved = wardBoyRepository.save(wardBoy);
		log.info("WardBoy saved successfully | wardBoyId={}", saved.getWardBoyId());

		String username = dto.getContactNumber();
		String rawPassword = generateStructuredPassword();
		String encodedPassword = passwordEncoder.encode(rawPassword);

		log.debug("Credentials generated | username={}", username);

		DoctorAndStaffLoginCredentials credentials = DoctorAndStaffLoginCredentials.builder()
				.staffId(saved.getWardBoyId()).staffName(saved.getFullName()).hospitalId(saved.getClinicId())
				.hospitalName(saved.getHospitalName()).branchId(saved.getBranchId()).branchName(saved.getBranchName())
				.username(username).password(encodedPassword).role(saved.getRole()).permissions(saved.getPermissions())
				.mobilenumber(saved.getContactNumber()).emailId(saved.getEmailId()).build();
		credentialsRepository.save(credentials);

		log.info("Login credentials created | wardBoyId={}", saved.getWardBoyId());

		WardBoyDTO responseDto = WardBoyMapper.toDTO(saved);
		responseDto.setBranchName(saved.getBranchName());
		responseDto.setUserName(username);
		responseDto.setPassword(rawPassword);

		log.info("Add WardBoy completed | wardBoyId={}", saved.getWardBoyId());

		return ResponseStructure.buildResponse(responseDto, "WardBoy added successfully", HttpStatus.CREATED,
				HttpStatus.CREATED.value());
	}

	@Override
	public ResponseStructure<WardBoyDTO> getWardBoyById(String id) {
		log.info("Fetching WardBoy by ID | id={}", id);

		WardBoy wardBoy = wardBoyRepository.findById(id).orElseThrow(() -> {
			log.warn("WardBoy not found | id={}", id);
			return new RuntimeException("WardBoy not found with ID: " + id);
		});
		WardBoyDTO dto = WardBoyMapper.toDTO(wardBoy);
		log.info("WardBoy fetched successfully | id={}", id);

		return ResponseStructure.buildResponse(dto, "WardBoy fetched successfully", HttpStatus.OK,
				HttpStatus.OK.value());
	}

	@Override
	public ResponseStructure<List<WardBoyDTO>> getAllWardBoys() {
		log.info("Fetching all WardBoys");

		List<WardBoyDTO> wardBoys = wardBoyRepository.findAll().stream().map(WardBoyMapper::toDTO)
				.collect(Collectors.toList());
		log.info("Total WardBoys fetched | count={}", wardBoys.size());

		return ResponseStructure.buildResponse(wardBoys, "All WardBoys fetched successfully", HttpStatus.OK,
				HttpStatus.OK.value());
	}

	@Override
	public ResponseStructure<WardBoyDTO> updateWardBoy(String id, WardBoyDTO dto) {
		log.info("Updating WardBoy | id={}", id);

		WardBoy existing = wardBoyRepository.findById(id).orElseThrow(() -> {
			log.warn("WardBoy not found for update | id={}", id);
			return new RuntimeException("WardBoy not found with ID: " + id);
		});
		log.debug("WardBoy fetched successfully for update | id={}", id);
		if (dto.getContactNumber() != null && !existing.getContactNumber().equals(dto.getContactNumber())) {
			log.debug("Updating contact number | old={}, new={}", existing.getContactNumber(), dto.getContactNumber());
			if (wardBoyRepository.findByContactNumber(dto.getContactNumber()).isPresent()) {
				log.warn("Duplicate contact number detected | contact={}", dto.getContactNumber());

				throw new RuntimeException("WardBoy already exists with contact number: " + dto.getContactNumber());
			}
			existing.setContactNumber(dto.getContactNumber());
		}
		if (dto.getBranchId() != null) {
			log.debug("Updating branchId | value={}", dto.getBranchId());
			existing.setBranchId(dto.getBranchId());
		}
		if (dto.getRole() != null) {
			log.debug("Updating role | value={}", dto.getRole());
			existing.setRole(dto.getRole());
		}
		if (dto.getClinicId() != null) {
			log.debug("Updating clinicId | value={}", dto.getClinicId());
			existing.setClinicId(dto.getClinicId());
		}

		if (dto.getHospitalName() != null) {
			log.debug("Updating hospitalName");
			existing.setHospitalName(dto.getHospitalName());
		}
		if (dto.getPermissions() != null) {
			log.debug("Updating permissions");
			existing.setPermissions(dto.getPermissions());
		}
		if (dto.getFullName() != null) {
			log.debug("Updating fullName");
			existing.setFullName(dto.getFullName());
		}
//		if (dto.getClinicId() != null)
//			existing.setClinicId(dto.getClinicId());
		if (dto.getDateOfBirth() != null) {
			log.debug("Updating dateOfBirth");
			existing.setDateOfBirth(dto.getDateOfBirth());
		}

		if (dto.getGovernmentId() != null) {
			log.debug("Updating governmentId");
			existing.setGovernmentId(dto.getGovernmentId());
		}
		if (dto.getDateOfJoining() != null) {
			log.debug("Updating dateOfJoining");
			existing.setDateOfJoining(dto.getDateOfJoining());
		}
		if (dto.getDepartment() != null) {
			log.debug("Updating department");
			existing.setDepartment(dto.getDepartment());
		}
		if (dto.getBankAccountDetails() != null) {
			log.debug("Updating bankAccountDetails");
			existing.setBankAccountDetails(dto.getBankAccountDetails());
		}
		if (dto.getAddress() != null) {
			log.debug("Updating address");
			existing.setAddress(dto.getAddress());
		}
		if (dto.getGender() != null) {
			log.debug("Updating gender");
			existing.setGender(dto.getGender());
		}
		if (dto.getWorkExprience() != null) {
			log.debug("Updating workExperience");
			existing.setWorkExprience(dto.getWorkExprience());
		}

		if (dto.getShiftTimingsOrAvailability() != null) {
			log.debug("Updating shiftTimingsOrAvailability");
			existing.setShiftTimingsOrAvailability(dto.getShiftTimingsOrAvailability());
		}
		if (dto.getEmergencyContact() != null) {
			log.debug("Updating emergencyContact");
			existing.setEmergencyContact(dto.getEmergencyContact());
		}
//		if (dto.getPermissions() != null)
//			existing.setPermissions(dto.getPermissions());

		if (dto.getMedicalFitnessCertificate() != null) {
			log.debug("Updating medicalFitnessCertificate");
			existing.setMedicalFitnessCertificate(WardBoyMapper.toEntity(dto).getMedicalFitnessCertificate());
		}
		if (dto.getBasicHealthFirstAidTrainingCertificate() != null) {
			log.debug("Updating firstAidTrainingCertificate");
			existing.setBasicHealthFirstAidTrainingCertificate(
					WardBoyMapper.toEntity(dto).getBasicHealthFirstAidTrainingCertificate());
		}
		if (dto.getPoliceVerification() != null) {
			log.debug("Updating policeVerification");
			existing.setPoliceVerification(WardBoyMapper.toEntity(dto).getPoliceVerification());
		}

		if (dto.getEmailId() != null) {
			log.debug("Updating emailId");
			existing.setEmailId(dto.getEmailId());
		}
		if (dto.getPreviousEmploymentHistory() != null) {
			log.debug("Updating previousEmploymentHistory");
			existing.setPreviousEmploymentHistory(dto.getPreviousEmploymentHistory());
		}
//		if (dto.getBasicHealthFirstAidTrainingCertificate() != null)
//			existing.setBasicHealthFirstAidTrainingCertificate(
//					WardBoyMapper.toEntity(dto).getBasicHealthFirstAidTrainingCertificate());
//		
		if (dto.getPoliceVerificationCertificate() != null) {
			log.debug("Updating policeVerificationCertificate");
			existing.setPoliceVerificationCertificate(WardBoyMapper.toEntity(dto).getPoliceVerificationCertificate());
		}
		if (dto.getProfilePicture() != null) {
			log.debug("Updating profilePicture");
			existing.setProfilePicture(WardBoyMapper.toEntity(dto).getProfilePicture());
		}
		log.info("Saving updated WardBoy | id={}", id);
		WardBoy saved = wardBoyRepository.save(existing);
		Optional<DoctorAndStaffLoginCredentials> credsOpt = credentialsRepository.findByStaffId(saved.getWardBoyId());

		if (credsOpt.isPresent()) {

			log.info("Syncing login credentials | wardBoyId={}", saved.getWardBoyId());

			DoctorAndStaffLoginCredentials creds = credsOpt.get();

			if (saved.getFullName() != null)
				creds.setStaffName(saved.getFullName());

			if (saved.getClinicId() != null)
				creds.setHospitalId(saved.getClinicId());

			if (saved.getHospitalName() != null)
				creds.setHospitalName(saved.getHospitalName());

			if (saved.getBranchId() != null)
				creds.setBranchId(saved.getBranchId());

			if (saved.getBranchName() != null)
				creds.setBranchName(saved.getBranchName());

			if (saved.getRole() != null)
				creds.setRole(saved.getRole());

			if (saved.getPermissions() != null)
				creds.setPermissions(saved.getPermissions());

			if (saved.getContactNumber() != null) {
				creds.setMobilenumber(saved.getContactNumber());
				creds.setUsername(saved.getContactNumber()); // Username is mobile number
			}

			if (saved.getEmailId() != null)
				creds.setEmailId(saved.getEmailId());

			credentialsRepository.save(creds);

			log.info("Login credentials synced successfully | wardBoyId={}", saved.getWardBoyId());
		}
		log.info("WardBoy updated successfully | id={}", id);
		WardBoyDTO responseDto = WardBoyMapper.toDTO(saved);

		return ResponseStructure.buildResponse(responseDto, "WardBoy updated successfully", HttpStatus.OK,
				HttpStatus.OK.value());
	}

	@Override
	public ResponseStructure<Void> deleteWardBoy(String id) {
		log.info("Delete WardBoy request received | id={}", id);

		try {
			// ✅ Step 1: Check if WardBoy exists
			if (!wardBoyRepository.existsById(id)) {
				log.warn("WardBoy not found for deletion | id={}", id);

				return ResponseStructure.buildResponse(null, "WardBoy not found with ID: " + id, HttpStatus.NOT_FOUND,
						HttpStatus.NOT_FOUND.value());
			}
			log.info("WardBoy exists, proceeding with deletion | id={}", id);
			// ✅ Step 2: Delete WardBoy record
			wardBoyRepository.deleteById(id);
			log.info("WardBoy deleted successfully | id={}", id);
			// ✅ Step 3: Delete corresponding login credentials (if exist)
			Optional<DoctorAndStaffLoginCredentials> credentials = credentialsRepository.findByStaffId(id);
			if (credentials.isPresent()) {
				log.debug("Deleting WardBoy login credentials | id={}", id);
				credentialsRepository.deleteById(credentials.get().getId());
				log.info("WardBoy login credentials deleted | id={}", id);
			} else {
				log.debug("No login credentials found for WardBoy | id={}", id);
			}

			log.info("Delete WardBoy completed successfully | id={}", id);

			// ✅ Step 4: Return success response
			return ResponseStructure.buildResponse(null, "WardBoy and credentials deleted successfully", HttpStatus.OK,
					HttpStatus.OK.value());

		} catch (Exception e) {
			log.error("Error occurred while deleting WardBoy | id={}", id, e);

			return ResponseStructure.buildResponse(null, "Error deleting WardBoy: " + e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}

	@Override
	public ResponseStructure<List<WardBoyDTO>> getWardBoysByClinicId(String clinicId) {
		log.info("Fetching WardBoys by clinicId={}", clinicId);

		List<WardBoyDTO> wardBoys = wardBoyRepository.findAllByClinicId(clinicId).stream().map(WardBoyMapper::toDTO)
				.collect(Collectors.toList());
		log.info("WardBoys fetched successfully | clinicId={}, count={}", clinicId, wardBoys.size());
		return ResponseStructure.buildResponse(wardBoys, "WardBoys fetched successfully for clinicId: " + clinicId,
				HttpStatus.OK, HttpStatus.OK.value());
	}

	@Override
	public ResponseStructure<WardBoyDTO> getWardBoyByIdAndClinicId(String wardBoyId, String clinicId) {
		log.info("Fetching WardBoy | wardBoyId={}, clinicId={}", wardBoyId, clinicId);

		WardBoy wardBoy = wardBoyRepository.findByWardBoyIdAndClinicId(wardBoyId, clinicId).orElseThrow(() -> {
			log.warn("WardBoy not found | wardBoyId={}, clinicId={}", wardBoyId, clinicId);
			return new RuntimeException("WardBoy not found with ID: " + wardBoyId + " for clinicId: " + clinicId);
		});
		log.debug("WardBoy entity fetched successfully | wardBoyId={}", wardBoyId);

		WardBoyDTO dto = WardBoyMapper.toDTO(wardBoy);

		log.info("WardBoy fetched successfully | wardBoyId={}, clinicId={}", wardBoyId, clinicId);

		return ResponseStructure.buildResponse(dto, "WardBoy fetched successfully", HttpStatus.OK,
				HttpStatus.OK.value());
	}

	// -------------- Generate password--------------------------
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
	public ResponseStructure<List<WardBoyDTO>> getWardBoysByClinicIdAndBranchId(String clinicId, String branchId) {
		log.info("Fetching WardBoys | clinicId={}, branchId={}", clinicId, branchId);

		// Fetch all ward boys for the given clinicId and branchId
		List<WardBoy> wordboy = wardBoyRepository.findByClinicIdAndBranchId(clinicId, branchId);
		log.debug("WardBoy entities fetched from DB | clinicId={}, branchId={}, count={}", clinicId, branchId,
				wordboy.size());
		List<WardBoyDTO> wardBoys = wordboy // Repository method
				.stream().map(WardBoyMapper::toDTO) // Convert Entity → DTO
				.collect(Collectors.toList());

		log.debug("WardBoy entities converted to DTOs | count={}", wardBoys.size());

		// Return response
		if (wardBoys.isEmpty()) {
			log.info("No WardBoys found | clinicId={}, branchId={}", clinicId, branchId);

			return ResponseStructure.buildResponse(null,
					"No WardBoys found for clinicId: " + clinicId + " and branchId: " + branchId, HttpStatus.OK,
					HttpStatus.OK.value());
		}
		log.info("WardBoys fetched successfully | clinicId={}, branchId={}, count={}", clinicId, branchId,
				wardBoys.size());

		return ResponseStructure.buildResponse(wardBoys,
				"WardBoys fetched successfully for clinicId: " + clinicId + " and branchId: " + branchId, HttpStatus.OK,
				HttpStatus.OK.value());
	}

}