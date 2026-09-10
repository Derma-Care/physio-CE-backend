package com.clinicadmin.service.impl;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.clinicadmin.dto.AdministratorDTO;
import com.clinicadmin.dto.Branch;
import com.clinicadmin.dto.Response;
import com.clinicadmin.entity.Administrator;
import com.clinicadmin.entity.DoctorAndStaffLoginCredentials;
import com.clinicadmin.feignclient.AdminServiceClient;
import com.clinicadmin.repository.AdministratorRepository;
import com.clinicadmin.repository.DoctorLoginCredentialsRepository;
import com.clinicadmin.service.AdministratorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AdministratorServiceImpl implements AdministratorService {

	private static final Logger log = LoggerFactory.getLogger(AdministratorServiceImpl.class);

	@Autowired
	private AdministratorRepository administratorRepository;

	@Autowired
	private DoctorLoginCredentialsRepository credentialsRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private AdminServiceClient adminServiceClient;

	@Autowired
	private ObjectMapper objectMapper;

	// ------------------- Onboarding ----------------------

	@Override
	public Response administratorOnboarding(AdministratorDTO dto) {
		log.info("Administrator onboarding started | clinicId={}, branchId={}, contact={}", dto.getClinicId(),
				dto.getBranchId(), dto.getContactNumber());
		Response response = new Response();

		// Check if admin already exists by contact number or email
		if (administratorRepository.existsByContactNumber(dto.getContactNumber())) {
			log.warn("Administrator already exists with contact number={}", dto.getContactNumber());
			response.setSuccess(false);
			response.setMessage("Administrator with this contact number already exists");
			response.setStatus(HttpStatus.BAD_REQUEST.value());
			return response;
		}

		if (credentialsRepository.existsByUsername(dto.getContactNumber())) {
			log.warn("Login credentials already exist for username={}", dto.getContactNumber());
			response.setSuccess(false);
			response.setMessage("Login credentials already exist for this contact number");
			response.setStatus(HttpStatus.BAD_REQUEST.value());
			return response;
		}
		log.info("Fetching branch details via Admin Service | branchId={}", dto.getBranchId());
		// Get branch name via Feign
		ResponseEntity<Response> res = adminServiceClient.getBranchById(dto.getBranchId());
		Branch branch = objectMapper.convertValue(res.getBody().getData(), Branch.class);

		// Map DTO → Entity
		Administrator admin = mapDtoToEntity(dto);
		admin.setBranchName(branch.getBranchName());
		admin.setAdminId(generateAdminId());

		// Generate login credentials
		String username = dto.getContactNumber();
		String rawPassword = generateStructuredPassword();
		String encodedPassword = passwordEncoder.encode(rawPassword);
		log.debug("Generated credentials | username={}", username);

		// Save admin in DB
		Administrator savedAdmin = administratorRepository.save(admin);

		// Create login credentials
		DoctorAndStaffLoginCredentials credentials = DoctorAndStaffLoginCredentials.builder()
				.staffId(savedAdmin.getId()).staffName(savedAdmin.getFullName()).hospitalId(savedAdmin.getClinicId())
				.hospitalName(savedAdmin.getHospitalName()).branchId(savedAdmin.getBranchId())
				.branchName(savedAdmin.getBranchName()).emailId(savedAdmin.getEmailId())
				.mobilenumber(savedAdmin.getContactNumber()).username(username).password(encodedPassword)
				.role(dto.getRole()).permissions(savedAdmin.getPermissions()).build();
		credentialsRepository.save(credentials);
		log.info("Login credentials created | adminId={}", savedAdmin.getAdminId());

		AdministratorDTO savedDTO = mapEntityToDto(savedAdmin);

		savedDTO.setUserName(username);
		savedDTO.setPassword(rawPassword);

		response.setSuccess(true);
		response.setData(savedDTO);
		response.setMessage("Administrator onboarded successfully");
		response.setStatus(HttpStatus.CREATED.value());
		log.info("Administrator onboarding completed | adminId={}", savedAdmin.getAdminId());
		return response;
	}

	// ------------------- Fetch All ----------------------

	@Override
	public Response getAllAdministratorsByClinic(String clinicId) {
		log.info("Fetching administrators | clinicId={}", clinicId);
		Response response = new Response();

		if (clinicId == null || clinicId.isBlank()) {
			log.warn("Invalid clinicId provided");
			response.setSuccess(false);
			response.setMessage("Clinic ID must not be empty");
			response.setStatus(HttpStatus.BAD_REQUEST.value());
			return response;
		}

		List<Administrator> admins = administratorRepository.findByClinicId(clinicId);
		if (admins.isEmpty()) {
			log.info("No administrators found | clinicId={}", clinicId);
			response.setSuccess(true);
			response.setData(List.of());
			response.setMessage("No administrators found");
			response.setStatus(HttpStatus.OK.value());
			return response;
		}

		List<AdministratorDTO> dtoList = admins.stream().map(this::mapEntityToDto).toList();
		response.setSuccess(true);
		response.setData(dtoList);
		response.setMessage("Administrators fetched successfully");
		response.setStatus(HttpStatus.OK.value());
		log.info("Administrators fetched | count={}", admins.size());
		return response;
	}

	@Override
	public Response getAllAdministratorsByClinic(String clinicId, String branchId) {

		log.info("Fetch administrators request received. clinicId: {}, branchId: {}", clinicId, branchId);

		Response response = new Response();

		// 1️⃣ Validate input
		if (clinicId == null || clinicId.isBlank()) {

			log.warn("Fetch administrators failed: clinicId is null or blank");

			response.setSuccess(false);
			response.setMessage("Clinic ID must not be empty");
			response.setStatus(HttpStatus.BAD_REQUEST.value());
			return response;
		}

		List<Administrator> admins;

		// Fetch data based on provided parameters
		if (branchId != null && !branchId.isBlank()) {

			log.debug("Fetching administrators by clinicId and branchId");
			admins = administratorRepository.findByClinicIdAndBranchId(clinicId, branchId);

		} else {

			log.debug("Fetching administrators by clinicId only");
			admins = administratorRepository.findByClinicId(clinicId);
		}

		// Handle empty results
		if (admins.isEmpty()) {

			log.info("No administrators found for clinicId: {}, branchId: {}", clinicId, branchId);

			response.setSuccess(true);
			response.setData(List.of());
			response.setMessage("No administrators found");
			response.setStatus(HttpStatus.OK.value());
			return response;
		}

		log.info("Total administrators found: {}", admins.size());

		// Convert entities to DTOs
		log.debug("Mapping Administrator entities to AdministratorDTOs");
		List<AdministratorDTO> dtoList = admins.stream().map(this::mapEntityToDto).toList();

		// 5️⃣ Build response
		log.info("Administrators fetched successfully for clinicId: {}, branchId: {}", clinicId, branchId);

		response.setSuccess(true);
		response.setData(dtoList);
		response.setMessage("Administrators fetched successfully");
		response.setStatus(HttpStatus.OK.value());
		return response;
	}

	// ------------------- Fetch By ID ----------------------
	@Override
	public Response getAdministratorById(String clinicId, String adminId) {

		log.info("Fetch administrator request received. clinicId: {}, adminId: {}", clinicId, adminId);

		Response response = new Response();

		if (clinicId == null || clinicId.isBlank()) {

			log.warn("Fetch administrator failed: clinicId is null or blank");

			response.setSuccess(false);
			response.setMessage("Clinic ID must not be empty");
			response.setStatus(HttpStatus.BAD_REQUEST.value());
			return response;
		}

		if (adminId == null || adminId.isBlank()) {

			log.warn("Fetch administrator failed: adminId is null or blank");

			response.setSuccess(false);
			response.setMessage("Administrator ID must not be empty");
			response.setStatus(HttpStatus.BAD_REQUEST.value());
			return response;
		}

		// Repository call using both clinicId and adminId
		log.debug("Searching administrator with clinicId: {}, adminId: {}", clinicId, adminId);
		Optional<Administrator> adminOpt = administratorRepository.findByClinicIdAndAdminId(clinicId, adminId);

		if (adminOpt.isPresent()) {

			log.info("Administrator found with adminId: {}", adminId);

			response.setSuccess(true);
			response.setData(mapEntityToDto(adminOpt.get()));
			response.setMessage("Administrator found successfully");
			response.setStatus(HttpStatus.OK.value());

		} else {

			log.warn("Administrator not found. clinicId: {}, adminId: {}", clinicId, adminId);

			response.setSuccess(false);
			response.setMessage("Administrator not found for the given clinic");
			response.setStatus(HttpStatus.NOT_FOUND.value());
		}

		return response;
	}

	@Override
	public Response getAdministratorUsingClinicIdAndBranchIdAndAdminId(String clinicId, String branchId,
			String adminId) {

		log.info("Fetch administrator request received. clinicId: {}, branchId: {}, adminId: {}", clinicId, branchId,
				adminId);

		Response response = new Response();

		if (clinicId == null || clinicId.isBlank()) {

			log.warn("Fetch administrator failed: clinicId is null or blank");

			response.setSuccess(false);
			response.setMessage("Clinic ID must not be empty");
			response.setStatus(HttpStatus.BAD_REQUEST.value());
			return response;
		}

		if (adminId == null || adminId.isBlank()) {

			log.warn("Fetch administrator failed: adminId is null or blank");

			response.setSuccess(false);
			response.setMessage("Administrator ID must not be empty");
			response.setStatus(HttpStatus.BAD_REQUEST.value());
			return response;
		}

		// Repository call using clinicId, branchId and adminId
		log.debug("Searching administrator with clinicId: {}, branchId: {}, adminId: {}", clinicId, branchId, adminId);
		Optional<Administrator> adminOpt = administratorRepository.findByClinicIdAndBranchIdAndAdminId(clinicId,
				branchId, adminId);

		if (adminOpt.isPresent()) {

			log.info("Administrator found with adminId: {}", adminId);

			response.setSuccess(true);
			response.setData(mapEntityToDto(adminOpt.get()));
			response.setMessage("Administrator found successfully");
			response.setStatus(HttpStatus.OK.value());

		} else {

			log.warn("Administrator not found. clinicId: {}, branchId: {}, adminId: {}", clinicId, branchId, adminId);

			response.setSuccess(false);
			response.setMessage("Administrator not found for the given clinic");
			response.setStatus(HttpStatus.NOT_FOUND.value());
		}

		return response;
	}

	// ------------------- Update ----------------------

	@Override
	public Response updateAdministrator(String clinicId, String adminId, AdministratorDTO dto) {

		log.info("Update administrator request received. clinicId: {}, adminId: {}", clinicId, adminId);

		Response response = new Response();

		// Validate required inputs
		if (clinicId == null) {

			log.warn("Update administrator failed: clinicId is null");

			response.setSuccess(false);
			response.setMessage("Clinic ID must not be null");
			response.setStatus(HttpStatus.BAD_REQUEST.value());
			return response;
		}

		if (adminId == null) {

			log.warn("Update administrator failed: adminId is null");

			response.setSuccess(false);
			response.setMessage("Administrator ID must not be null");
			response.setStatus(HttpStatus.BAD_REQUEST.value());
			return response;
		}

		if (dto == null) {

			log.warn("Update administrator failed: AdministratorDTO is null");

			response.setSuccess(false);
			response.setMessage("Administrator data must not be null");
			response.setStatus(HttpStatus.BAD_REQUEST.value());
			return response;
		}

		// Fetch admin by clinicId and adminId
		log.debug("Searching administrator with clinicId: {}, adminId: {}", clinicId, adminId);
		Optional<Administrator> existingOpt = administratorRepository.findByClinicIdAndAdminId(clinicId, adminId);

		if (existingOpt.isEmpty()) {

			log.warn("Update administrator failed: Administrator not found. clinicId: {}, adminId: {}", clinicId,
					adminId);

			response.setSuccess(false);
			response.setMessage("Administrator not found for the given clinic and ID");
			response.setStatus(HttpStatus.NOT_FOUND.value());
			return response;
		}

		Administrator admin = existingOpt.get();

		log.info("Administrator found. Updating details for adminId: {}", adminId);

		// Update non-null fields
		if (dto.getFullName() != null) {
			log.debug("Updating fullName");
			admin.setFullName(dto.getFullName());
		}

		if (dto.getEmailId() != null) {
			log.debug("Updating emailId");
			admin.setEmailId(dto.getEmailId());
		}

		if (dto.getDepartment() != null) {
			log.debug("Updating department");
			admin.setDepartment(dto.getDepartment());
		}

		if (dto.getContactNumber() != null) {
			log.debug("Updating contactNumber");
			admin.setContactNumber(dto.getContactNumber());
		}

		if (dto.getGender() != null) {
			log.debug("Updating gender");
			admin.setGender(dto.getGender());
		}

		if (dto.getDateOfBirth() != null) {
			log.debug("Updating dateOfBirth");
			admin.setDateOfBirth(dto.getDateOfBirth());
		}

		if (dto.getAddress() != null) {
			log.debug("Updating address");
			admin.setAddress(dto.getAddress());
		}

		if (dto.getBankAccountDetails() != null) {
			log.debug("Updating bankAccountDetails");
			admin.setBankAccountDetails(dto.getBankAccountDetails());
		}

		if (dto.getProfilePicture() != null) {
			log.debug("Updating profilePicture");
			admin.setProfilePicture(dto.getProfilePicture());
		}

		if (dto.getPermissions() != null) {
			log.debug("Updating permissions");
			admin.setPermissions(dto.getPermissions());
		}

		// Save updated admin
		log.info("Saving updated administrator for adminId: {}", adminId);
		Administrator updatedAdmin = administratorRepository.save(admin);

		// Sync credentials (if present)
		log.debug("Syncing login credentials for adminId: {}", adminId);
		credentialsRepository.findByStaffId(adminId).ifPresent(creds -> {
			log.info("Updating credentials for adminId: {}", adminId);

			if (updatedAdmin.getFullName() != null)
				creds.setStaffName(updatedAdmin.getFullName());

			if (updatedAdmin.getBranchId() != null)
				creds.setBranchId(updatedAdmin.getBranchId());

			if (updatedAdmin.getBranchName() != null)
				creds.setBranchName(updatedAdmin.getBranchName());

			if (updatedAdmin.getClinicId() != null)
				creds.setHospitalId(updatedAdmin.getClinicId());

			if (updatedAdmin.getHospitalName() != null)
				creds.setHospitalName(updatedAdmin.getHospitalName());

			if (updatedAdmin.getRole() != null)
				creds.setRole(updatedAdmin.getRole());

			if (updatedAdmin.getPermissions() != null)
				creds.setPermissions(updatedAdmin.getPermissions());

			if (updatedAdmin.getContactNumber() != null)
				creds.setMobilenumber(updatedAdmin.getContactNumber());

			if (updatedAdmin.getEmailId() != null)
				creds.setEmailId(updatedAdmin.getEmailId());
			credentialsRepository.save(creds);
		});

		// Prepare response
		log.info("Administrator updated successfully for adminId: {}", adminId);

		response.setSuccess(true);
		response.setData(mapEntityToDto(updatedAdmin));
		response.setMessage("Administrator updated successfully");
		response.setStatus(HttpStatus.OK.value());
		return response;
	}

	@Override
	public Response updateAdministratorByClinicIdAndBranchIdAndAdminId(String clinicId, String branchId, String adminId,
			AdministratorDTO dto) {

		log.info("Update administrator request received. clinicId: {}, branchId: {}, adminId: {}", clinicId, branchId,
				adminId);

		Response response = new Response();

		// Validate inputs
		if (clinicId == null || clinicId.isBlank()) {

			log.warn("Update administrator failed: clinicId is null or blank");

			response.setSuccess(false);
			response.setMessage("Clinic ID must not be null or empty");
			response.setStatus(HttpStatus.BAD_REQUEST.value());
			return response;
		}

		if (branchId == null || branchId.isBlank()) {

			log.warn("Update administrator failed: branchId is null or blank");

			response.setSuccess(false);
			response.setMessage("Branch ID must not be null or empty");
			response.setStatus(HttpStatus.BAD_REQUEST.value());
			return response;
		}

		if (adminId == null || adminId.isBlank()) {

			log.warn("Update administrator failed: adminId is null or blank");

			response.setSuccess(false);
			response.setMessage("Administrator ID must not be null or empty");
			response.setStatus(HttpStatus.BAD_REQUEST.value());
			return response;
		}

		if (dto == null) {

			log.warn("Update administrator failed: AdministratorDTO is null");

			response.setSuccess(false);
			response.setMessage("Administrator data must not be null");
			response.setStatus(HttpStatus.BAD_REQUEST.value());
			return response;
		}

		// Fetch admin by clinicId, branchId, and adminId
		log.debug("Searching administrator with clinicId: {}, branchId: {}, adminId: {}", clinicId, branchId, adminId);
		Optional<Administrator> existingOpt = administratorRepository.findByClinicIdAndBranchIdAndAdminId(clinicId,
				branchId, adminId);

		if (existingOpt.isEmpty()) {

			log.warn("Update administrator failed: Administrator not found. clinicId: {}, branchId: {}, adminId: {}",
					clinicId, branchId, adminId);

			response.setSuccess(false);
			response.setMessage("Administrator not found for the given Clinic ID, Branch ID, and Administrator ID");
			response.setStatus(HttpStatus.NOT_FOUND.value());
			return response;
		}

		Administrator admin = existingOpt.get();

		log.info("Administrator found. Updating details for adminId: {}", adminId);

		// Update non-null fields only
		if (dto.getFullName() != null) {
			log.debug("Updating fullName");
			admin.setFullName(dto.getFullName());
		}

		if (dto.getEmailId() != null) {
			log.debug("Updating emailId");
			admin.setEmailId(dto.getEmailId());
		}

		if (dto.getDepartment() != null) {
			log.debug("Updating department");
			admin.setDepartment(dto.getDepartment());
		}

		if (dto.getContactNumber() != null) {
			log.debug("Updating contactNumber");
			admin.setContactNumber(dto.getContactNumber());
		}

		if (dto.getGender() != null) {
			log.debug("Updating gender");
			admin.setGender(dto.getGender());
		}

		if (dto.getDateOfBirth() != null) {
			log.debug("Updating dateOfBirth");
			admin.setDateOfBirth(dto.getDateOfBirth());
		}

		if (dto.getAddress() != null) {
			log.debug("Updating address");
			admin.setAddress(dto.getAddress());
		}

		if (dto.getBankAccountDetails() != null) {
			log.debug("Updating bankAccountDetails");
			admin.setBankAccountDetails(dto.getBankAccountDetails());
		}

		if (dto.getProfilePicture() != null) {
			log.debug("Updating profilePicture");
			admin.setProfilePicture(dto.getProfilePicture());
		}

		if (dto.getPermissions() != null) {
			log.debug("Updating permissions");
			admin.setPermissions(dto.getPermissions());
		}

		// Save updated record
		log.info("Saving updated administrator for adminId: {}", adminId);
		Administrator updatedAdmin = administratorRepository.save(admin);

		// Sync credentials if available
		log.debug("Syncing login credentials for adminId: {}", adminId);
		credentialsRepository.findByStaffId(adminId).ifPresent(creds -> {
			log.info("Updating credentials for adminId: {}", adminId);
			credentialsRepository.save(creds);
		});

		// Return success response
		log.info("Administrator updated successfully for clinicId: {}, branchId: {}, adminId: {}", clinicId, branchId,
				adminId);

		response.setSuccess(true);
		response.setData(mapEntityToDto(updatedAdmin));
		response.setMessage("Administrator updated successfully for given clinic and branch");
		response.setStatus(HttpStatus.OK.value());
		return response;
	}

	// ------------------- Delete ----------------------

	@Override
	public Response deleteAdministrator(String clinicId, String adminId) {

		log.info("Delete administrator request received. clinicId: {}, adminId: {}", clinicId, adminId);

		Response response = new Response();

		if (clinicId == null) {

			log.warn("Delete administrator failed: clinicId is null");

			response.setSuccess(false);
			response.setMessage("Clinic ID must not be null");
			response.setStatus(HttpStatus.BAD_REQUEST.value());
			return response;
		}

		if (adminId == null) {

			log.warn("Delete administrator failed: adminId is null");

			response.setSuccess(false);
			response.setMessage("Administrator ID must not be null");
			response.setStatus(HttpStatus.BAD_REQUEST.value());
			return response;
		}

		// Find admin by clinicId and adminId
		log.debug("Searching administrator for deletion. clinicId: {}, adminId: {}", clinicId, adminId);
		Optional<Administrator> existingOpt = administratorRepository.findByClinicIdAndAdminId(clinicId, adminId);

		if (existingOpt.isEmpty()) {

			log.warn("Delete administrator failed: Administrator not found. clinicId: {}, adminId: {}", clinicId,
					adminId);

			response.setSuccess(false);
			response.setMessage("Administrator not found for the given clinic and ID");
			response.setStatus(HttpStatus.NOT_FOUND.value());
			return response;
		}

		Administrator existingAdmin = existingOpt.get();

		log.info("Administrator found. Proceeding with deletion for adminId: {}", adminId);

		// Delete admin
		log.info("Deleting administrator record for adminId: {}", adminId);
		administratorRepository.deleteById(existingAdmin.getId());

		// Delete associated credentials (if any)
		log.debug("Checking and deleting credentials for adminId: {}", adminId);
		credentialsRepository.findByStaffId(adminId).ifPresent(creds -> {
			log.info("Deleting credentials for adminId: {}", adminId);
			credentialsRepository.deleteById(creds.getId());
		});

		log.info("Administrator and credentials deleted successfully for adminId: {}", adminId);

		response.setSuccess(true);
		response.setMessage("Administrator and credentials deleted successfully");
		response.setStatus(HttpStatus.OK.value());
		return response;
	}

	@Override
	public Response deleteAdministratorByClinicIdAndBranchIdAndAdminId(String clinicId, String branchId,
			String adminId) {

		log.info("Delete administrator request received. clinicId: {}, branchId: {}, adminId: {}", clinicId, branchId,
				adminId);

		Response response = new Response();

		// Validate inputs
		if (clinicId == null || clinicId.isBlank()) {

			log.warn("Delete administrator failed: clinicId is null or blank");

			response.setSuccess(false);
			response.setMessage("Clinic ID must not be null or empty");
			response.setStatus(HttpStatus.BAD_REQUEST.value());
			return response;
		}

		if (branchId == null || branchId.isBlank()) {

			log.warn("Delete administrator failed: branchId is null or blank");

			response.setSuccess(false);
			response.setMessage("Branch ID must not be null or empty");
			response.setStatus(HttpStatus.BAD_REQUEST.value());
			return response;
		}

		if (adminId == null || adminId.isBlank()) {

			log.warn("Delete administrator failed: adminId is null or blank");

			response.setSuccess(false);
			response.setMessage("Administrator ID must not be null or empty");
			response.setStatus(HttpStatus.BAD_REQUEST.value());
			return response;
		}

		// Find admin by clinicId, branchId, and adminId
		log.debug("Searching administrator for deletion. clinicId: {}, branchId: {}, adminId: {}", clinicId, branchId,
				adminId);
		Optional<Administrator> existingOpt = administratorRepository.findByClinicIdAndBranchIdAndAdminId(clinicId,
				branchId, adminId);

		if (existingOpt.isEmpty()) {

			log.warn("Delete administrator failed: Administrator not found. clinicId: {}, branchId: {}, adminId: {}",
					clinicId, branchId, adminId);

			response.setSuccess(false);
			response.setMessage("Administrator not found for the given Clinic ID, Branch ID, and Admin ID");
			response.setStatus(HttpStatus.NOT_FOUND.value());
			return response;
		}

		Administrator existingAdmin = existingOpt.get();

		log.info("Administrator found. Proceeding with deletion for adminId: {}", adminId);

		// Delete admin
		log.info("Deleting administrator record for adminId: {}", adminId);
		administratorRepository.deleteById(existingAdmin.getId());

		// Delete credentials
		log.debug("Checking and deleting credentials for adminId: {}", adminId);
		credentialsRepository.findByStaffId(adminId).ifPresent(creds -> {
			log.info("Deleting credentials for adminId: {}", adminId);
			credentialsRepository.deleteById(creds.getId());
		});

		// Return success response
		log.info("Administrator and credentials deleted successfully. clinicId: {}, branchId: {}, adminId: {}",
				clinicId, branchId, adminId);

		response.setSuccess(true);
		response.setMessage("Administrator and credentials deleted successfully for the given clinic and branch");
		response.setStatus(HttpStatus.OK.value());
		return response;
	}

	// ------------------- Mapper Methods ----------------------

	private Administrator mapDtoToEntity(AdministratorDTO dto) {
		Administrator admin = new Administrator();
		admin.setClinicId(dto.getClinicId());
		admin.setBranchId(dto.getBranchId());
		admin.setBranchName(dto.getBranchName());
		admin.setHospitalName(dto.getHospitalName());
		admin.setFullName(dto.getFullName());
		admin.setGender(dto.getGender());
		admin.setDateOfBirth(dto.getDateOfBirth());
		admin.setContactNumber(dto.getContactNumber());
		admin.setEmailId(dto.getEmailId());
		admin.setGovernmentId(dto.getGovernmentId());
		admin.setQualificationOrCertifications(dto.getQualificationOrCertifications());
		admin.setDateOfJoining(dto.getDateOfJoining());
		admin.setDepartment(dto.getDepartment());
		admin.setYearOfExperience(dto.getYearOfExperience());
		admin.setRole(dto.getRole());
		admin.setAddress(dto.getAddress());
		admin.setEmergencyContact(dto.getEmergencyContact());
		admin.setProfilePicture(dto.getProfilePicture());
		admin.setBankAccountDetails(dto.getBankAccountDetails());
		admin.setPermissions(dto.getPermissions());
		admin.setCreatedBy(dto.getCreatedBy());
		admin.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Kolkata")).toString());
		return admin;
	}

	private AdministratorDTO mapEntityToDto(Administrator admin) {
		AdministratorDTO dto = new AdministratorDTO();
		dto.setId(admin.getId());
		dto.setAdminId(admin.getAdminId());
		dto.setClinicId(admin.getClinicId());
		dto.setBranchId(admin.getBranchId());
		dto.setBranchName(admin.getBranchName());
		dto.setHospitalName(admin.getHospitalName());
		dto.setFullName(admin.getFullName());
		dto.setGender(admin.getGender());
		dto.setDateOfBirth(admin.getDateOfBirth());
		dto.setContactNumber(admin.getContactNumber());
		dto.setEmailId(admin.getEmailId());
		dto.setGovernmentId(admin.getGovernmentId());
		dto.setQualificationOrCertifications(admin.getQualificationOrCertifications());
		dto.setDateOfJoining(admin.getDateOfJoining());
		dto.setDepartment(admin.getDepartment());
		dto.setYearOfExperience(admin.getYearOfExperience());
		dto.setRole(admin.getRole());
		dto.setAddress(admin.getAddress());
		dto.setEmergencyContact(admin.getEmergencyContact());
		dto.setProfilePicture(admin.getProfilePicture());
		dto.setBankAccountDetails(admin.getBankAccountDetails());
		dto.setPermissions(admin.getPermissions());
		dto.setCreatedAt(admin.getCreatedAt());
		dto.setCreatedBy(admin.getCreatedBy());
		dto.setUpdatedDate(admin.getUpdatedDate());
		return dto;
	}

	// ------------------- Helpers ----------------------

	private String generateAdminId() {
		return "AD_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
	}

	private String generateStructuredPassword() {
		String[] words = { "Admin" };
		String specialChars = "@#$%&*!?";
		String digits = "0123456789";
		SecureRandom random = new SecureRandom();

		String word = words[random.nextInt(words.length)];
		String capitalizedWord = word.substring(0, 1).toUpperCase() + word.substring(1);
		char specialChar = specialChars.charAt(random.nextInt(specialChars.length()));

		StringBuilder numberPart = new StringBuilder();
		for (int i = 0; i < 3; i++) {
			numberPart.append(digits.charAt(random.nextInt(digits.length())));
		}

		return capitalizedWord + specialChar + numberPart;
	}

}
