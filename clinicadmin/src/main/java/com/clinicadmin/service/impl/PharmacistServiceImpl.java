package com.clinicadmin.service.impl;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.clinicadmin.dto.Branch;
import com.clinicadmin.dto.DoctorPrescriptionDTO;
import com.clinicadmin.dto.MedicineDTO;
import com.clinicadmin.dto.MedicineTypeDTO;
import com.clinicadmin.dto.PharmacistDTO;
import com.clinicadmin.dto.Response;
import com.clinicadmin.entity.DoctorAndStaffLoginCredentials;
import com.clinicadmin.entity.Pharmacist;
import com.clinicadmin.feignclient.AdminServiceClient;
//import com.clinicadmin.feignclient.DoctorServiceFeign;
import com.clinicadmin.repository.DoctorLoginCredentialsRepository;
import com.clinicadmin.repository.PharmacistRepository;
import com.clinicadmin.service.PharmacistService;
import com.clinicadmin.utils.Base64CompressionUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import feign.FeignException;

@Service
public class PharmacistServiceImpl implements PharmacistService {

	@Autowired
	private PharmacistRepository pharmacistRepository;

	@Autowired
	PasswordEncoder passwordEncoder;

	@Autowired
	DoctorLoginCredentialsRepository credentialsRepository;
	@Autowired
	AdminServiceClient adminServiceClient;

	@Autowired
	ObjectMapper objectMapper;

//	@Autowired
//	private DoctorServiceFeign doctorServiceFeign;

	private final ObjectMapper mapper = new ObjectMapper();

	@Override
	public Response pharmacistOnboarding(PharmacistDTO dto) {
		Response response = new Response();

		if (pharmacistRepository.existsByContactNumber(dto.getContactNumber())) {
			response.setSuccess(false);
			response.setMessage("Pharmacist with this mobile number already exists");
			response.setStatus(HttpStatus.BAD_REQUEST.value());
			return response;
		}
		if (credentialsRepository.existsByUsername(dto.getContactNumber())) {
			response.setSuccess(false);
			response.setMessage("Login credentials already exist for this mobile number");
			response.setStatus(HttpStatus.BAD_REQUEST.value());
			return response;
		}

		ResponseEntity<Response> res = adminServiceClient.getBranchById(dto.getBranchId());
		Branch br = objectMapper.convertValue(res.getBody().getData(), Branch.class);

		Pharmacist pharmacist = mapDtoToEntity(dto);
		String pharmacistId = generatePharmacistId();
		pharmacist.setPharmacistId(pharmacistId);
		pharmacist.setBranchName(br.getBranchName());

		// username = contact number

		String username = dto.getContactNumber();
		String rawPassword = generateStructuredPassword();
		String encodedPassword = passwordEncoder.encode(rawPassword);

		Pharmacist saved = pharmacistRepository.save(pharmacist);

		DoctorAndStaffLoginCredentials credentials = DoctorAndStaffLoginCredentials.builder()
				.staffId(saved.getPharmacistId()).staffName(saved.getFullName()).hospitalId(saved.getHospitalId())
				.hospitalName(saved.getHospitalName()).branchId(saved.getBranchId()).branchName(saved.getBranchName())
				.emailId(saved.getEmailID()).mobilenumber(saved.getContactNumber()).username(username)
				.password(encodedPassword).role(dto.getRole()).permissions(saved.getPermissions()).build();
		credentialsRepository.save(credentials);

		PharmacistDTO savedDTO = mapEntityToDto(saved);
		savedDTO.setBranchName(saved.getBranchName());
		savedDTO.setUserName(username);
		savedDTO.setPassword(rawPassword);

		response.setSuccess(true);
		response.setData(savedDTO);
		response.setMessage("Pharmacist added successfully");
		response.setStatus(HttpStatus.CREATED.value());
		return response;
	}

	@Override
	public Response getAllPharmacistsByHospitalId(String hospitalId) {
		Response response = new Response();
		List<Pharmacist> list = pharmacistRepository.findByHospitalId(hospitalId);

		response.setSuccess(true);
		response.setData(list.stream().map(this::mapEntityToDto).toList());
		response.setMessage(list.isEmpty() ? "No Pharmacists found" : "Pharmacists fetched successfully");
		response.setStatus(HttpStatus.OK.value());
		return response;
	}

	@Override
	public Response getPharmacistById(String pharmacistId) {
		Response response = new Response();
		return pharmacistRepository.findByPharmacistId(pharmacistId).map(pharmacist -> {
			response.setSuccess(true);
			response.setData(mapEntityToDto(pharmacist));
			response.setMessage("Pharmacist found");
			response.setStatus(HttpStatus.OK.value());
			return response;
		}).orElseGet(() -> {
			response.setSuccess(false);
			response.setMessage("Pharmacist not found");
			response.setStatus(HttpStatus.OK.value());
			return response;
		});
	}

	@Override
	public Response updatePharmacist(String pharmacistId, PharmacistDTO dto) {
		Response response = new Response();

		return pharmacistRepository.findByPharmacistId(pharmacistId).map(existing -> {

			if (dto.getHospitalId() != null)
				existing.setHospitalId(dto.getHospitalId());
			if (dto.getFullName() != null)
				existing.setFullName(dto.getFullName());
			if (dto.getGender() != null)
				existing.setGender(dto.getGender());
			if (dto.getQualification() != null)
				existing.setQualification(dto.getQualification());
			if (dto.getDateOfBirth() != null)
				existing.setDateOfBirth(dto.getDateOfBirth());
			if (dto.getContactNumber() != null)
				existing.setContactNumber(dto.getContactNumber());
			if (dto.getGovernmentId() != null)
				existing.setGovernmentId(dto.getGovernmentId());
			if (dto.getShiftTimingsOrAvailability() != null)
				existing.setShiftTimingsOrAvailability(dto.getShiftTimingsOrAvailability());
			if (dto.getPharmacyLicense() != null)
				existing.setPharmacyLicense(dto.getPharmacyLicense());
			if (dto.getStatePharmacyCouncilRegistration() != null)
				existing.setStatePharmacyCouncilRegistration(
						Base64CompressionUtil.compressBase64(dto.getStatePharmacyCouncilRegistration()));
			if (dto.getYearsOfExperience() != null)
				existing.setYearsOfExperience(dto.getYearsOfExperience());
			if (dto.getDateOfJoining() != null)
				existing.setDateOfJoining(dto.getDateOfJoining());
			if (dto.getDepartment() != null)
				existing.setDepartment(dto.getDepartment());
			if (dto.getBankAccountDetails() != null)
				existing.setBankAccountDetails(dto.getBankAccountDetails());
			if (dto.getAddress() != null)
				existing.setAddress(dto.getAddress());
			if (dto.getEmailID() != null)
				existing.setEmailID(dto.getEmailID());
			if (dto.getPreviousEmploymentHistory() != null)
				existing.setPreviousEmploymentHistory(dto.getPreviousEmploymentHistory());
			if (dto.getExperienceCertificates() != null)
				existing.setExperienceCertificates(
						Base64CompressionUtil.compressBase64(dto.getExperienceCertificates()));
			if (dto.getEmergencyContactNumber() != null)
				existing.setEmergencyContactNumber(dto.getEmergencyContactNumber());
			if (dto.getDpharmaOrBPharmaCertificate() != null)
				existing.setDpharmaOrBPharmaCertificate(
						Base64CompressionUtil.compressBase64(dto.getDpharmaOrBPharmaCertificate()));
			if (dto.getProfilePicture() != null)
				existing.setProfilePicture(Base64CompressionUtil.compressBase64(dto.getProfilePicture()));
			if (dto.getPermissions() != null)
				existing.setPermissions(dto.getPermissions());
			existing.setUpdatedDate(LocalDate.now().toString());
			// 🔹 Save pharmacist entity
			Pharmacist updated = pharmacistRepository.save(existing);

			// 🔹 Sync permissions to DoctorLoginCredentials
			Optional<DoctorAndStaffLoginCredentials> credsOpt = credentialsRepository
					.findByStaffId(updated.getPharmacistId());
			if (credsOpt.isPresent()) {
				DoctorAndStaffLoginCredentials creds = credsOpt.get();

				if (updated.getFullName() != null)
				    creds.setStaffName(updated.getFullName());

				if (updated.getBranchId() != null)
				    creds.setBranchId(updated.getBranchId());

				if (updated.getBranchName() != null)
				    creds.setBranchName(updated.getBranchName());

				if (updated.getHospitalId() != null)
				    creds.setHospitalId(updated.getHospitalId());

				if (updated.getHospitalName() != null)
				    creds.setHospitalName(updated.getHospitalName());

				if (updated.getRole() != null)
				    creds.setRole(updated.getRole());

				if (updated.getPermissions() != null)
				    creds.setPermissions(updated.getPermissions());

				if (updated.getContactNumber() != null)
				    creds.setMobilenumber(updated.getContactNumber());

				if (updated.getEmailID() != null)
				    creds.setEmailId(updated.getEmailID());
				credentialsRepository.save(creds);
			}

			response.setSuccess(true);
			response.setData(mapEntityToDto(updated));
			response.setMessage("Pharmacist updated successfully");
			response.setStatus(HttpStatus.OK.value());
			return response;

		}).orElseGet(() -> {
			response.setSuccess(false);
			response.setMessage("Pharmacist not found");
			response.setStatus(HttpStatus.OK.value());
			return response;
		});
	}

	@Override
	public Response deletePharmacist(String pharmacistId) {
		Response response = new Response();

		try {
			// ✅ Step 1: Check if Pharmacist exists
			Optional<Pharmacist> existing = pharmacistRepository.findByPharmacistId(pharmacistId);
			if (existing.isEmpty()) {
				response.setSuccess(false);
				response.setMessage("Pharmacist not found");
				response.setStatus(HttpStatus.NOT_FOUND.value());
				return response;
			}

			// ✅ Step 2: Delete pharmacist record
			pharmacistRepository.deleteByPharmacistId(pharmacistId);

			// ✅ Step 3: Delete corresponding login credentials (if any)
			Optional<DoctorAndStaffLoginCredentials> credentials = credentialsRepository.findByStaffId(pharmacistId);
			if (credentials.isPresent()) {
				credentialsRepository.deleteById(credentials.get().getId());
			}

			// ✅ Step 4: Build response
			response.setSuccess(true);
			response.setMessage("Pharmacist and credentials deleted successfully");
			response.setStatus(HttpStatus.OK.value());

		} catch (Exception e) {
			response.setSuccess(false);
			response.setMessage("Error deleting Pharmacist: " + e.getMessage());
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return response;
	}

	@Override
	public Response getPharmacistsByClinicIdAndBranchId(String hospitalId, String branchId) {
		Response response = new Response();

		List<Pharmacist> pharmacists = pharmacistRepository.findByHospitalIdAndBranchId(hospitalId, branchId);

		if (pharmacists.isEmpty()) {
			response.setSuccess(false);
			response.setMessage("No pharmacists found for hospital " + hospitalId + " and branch " + branchId);
			response.setStatus(HttpStatus.OK.value());
			response.setData(Collections.emptyList());
		} else {
			response.setSuccess(true);
			response.setMessage("Pharmacists retrieved successfully");
			response.setStatus(HttpStatus.OK.value());
			// ✅ Map entities to DTOs to get string ID
			response.setData(pharmacists.stream().map(this::mapEntityToDto).toList());
		}

		return response;
	}

	// ---------------- LOGIN ----------------
//	@Override
//	public Response pharmacistLogin(PharmacistLoginDTO loginDTO) {
//		Response response = new Response();
//		Optional<Pharmacist> pharmacist = pharmacistRepository.findByUserName(loginDTO.getUserName());
//
//		if (pharmacist.isPresent()) {
//			if (pharmacist.get().getPassword().equals(loginDTO.getPassword())) {
//				response.setSuccess(true);
//				response.setMessage("Login Successful");
//				response.setStatus(HttpStatus.OK.value());
//			} else {
//				response.setSuccess(false);
//				response.setMessage("Invalid password");
//				response.setStatus(HttpStatus.UNAUTHORIZED.value());
//			}
//		} else {
//			response.setSuccess(false);
//			response.setMessage("Invalid Username");
//			response.setStatus(HttpStatus.NOT_FOUND.value());
//		}
//		return response;
//	}

	// ---------------- RESET PASSWORD ----------------
//	@Override
//	public Response resetLoginPassword(ResetPharmacistLoginPasswordDTO dto) {
//		Response response = new Response();
//		Optional<Pharmacist> pharmacistOpt = pharmacistRepository.findByUserName(dto.getUserName());
//
//		if (pharmacistOpt.isPresent()) {
//			Pharmacist pharmacist = pharmacistOpt.get();
//			if (pharmacist.getPassword().equals(dto.getCurrentPassword())) {
//				if (dto.getNewPassword().equals(dto.getConfirmPassword())) {
//					pharmacist.setPassword(dto.getNewPassword());
//					pharmacistRepository.save(pharmacist);
//					response.setSuccess(true);
//					response.setMessage("Password updated successfully");
//					response.setStatus(HttpStatus.OK.value());
//				} else {
//					response.setSuccess(false);
//					response.setMessage("New password and confirm password do not match");
//					response.setStatus(HttpStatus.BAD_REQUEST.value());
//				}
//			} else {
//				response.setSuccess(false);
//				response.setMessage("Invalid current password");
//				response.setStatus(HttpStatus.UNAUTHORIZED.value());
//			}
//		} else {
//			response.setSuccess(false);
//			response.setMessage("Invalid Username");
//			response.setStatus(HttpStatus.NOT_FOUND.value());
//		}
//		return response;
//	}

	// ---------------- Helper Methods ----------------
	private Pharmacist mapDtoToEntity(PharmacistDTO dto) {
		Pharmacist pharmacist = new Pharmacist();
		// Convert MongoDB ObjectId or any complex id to simple string
		dto.setId(pharmacist.getId() != null ? pharmacist.getId().toString() : null);
		pharmacist.setHospitalId(dto.getHospitalId());
		pharmacist.setHospitalName(dto.getHospitalName());
		pharmacist.setBranchId(dto.getBranchId());
		pharmacist.setRole(dto.getRole());
		pharmacist.setPermissions(dto.getPermissions());
		pharmacist.setFullName(dto.getFullName());
		pharmacist.setGender(dto.getGender());
		pharmacist.setQualification(dto.getQualification());
		pharmacist.setDateOfBirth(dto.getDateOfBirth());
		pharmacist.setContactNumber(dto.getContactNumber());
		pharmacist.setGovernmentId(dto.getGovernmentId());
		pharmacist.setPharmacyLicense(dto.getPharmacyLicense());
		pharmacist.setYearsOfExperience(dto.getYearsOfExperience());
		pharmacist.setDateOfJoining(dto.getDateOfJoining());
		pharmacist.setDepartment(dto.getDepartment());
		pharmacist.setBankAccountDetails(dto.getBankAccountDetails());
		pharmacist.setAddress(dto.getAddress());
		pharmacist.setEmailID(dto.getEmailID());
		pharmacist.setShiftTimingsOrAvailability(dto.getShiftTimingsOrAvailability());
		pharmacist.setEmergencyContactNumber(dto.getEmergencyContactNumber());
		pharmacist.setPreviousEmploymentHistory(dto.getPreviousEmploymentHistory());
		pharmacist.setProfilePicture(Base64CompressionUtil.compressBase64(dto.getProfilePicture()));
		pharmacist.setStatePharmacyCouncilRegistration(
				Base64CompressionUtil.compressBase64(dto.getStatePharmacyCouncilRegistration()));
		pharmacist.setExperienceCertificates(Base64CompressionUtil.compressBase64(dto.getExperienceCertificates()));
		pharmacist.setDpharmaOrBPharmaCertificate(
				Base64CompressionUtil.compressBase64(dto.getDpharmaOrBPharmaCertificate()));
		pharmacist.setCreatedBy(dto.getCreatedBy());
		pharmacist.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Kolkata")).toString());

		return pharmacist;
	}

	private PharmacistDTO mapEntityToDto(Pharmacist pharmacist) {
		PharmacistDTO dto = new PharmacistDTO();
		dto.setId(pharmacist.getId().toString());
		dto.setPharmacistId(pharmacist.getPharmacistId());
		dto.setHospitalId(pharmacist.getHospitalId());
		dto.setHospitalName(pharmacist.getHospitalName());
		dto.setBranchId(pharmacist.getBranchId());
		dto.setRole(pharmacist.getRole());
		dto.setFullName(pharmacist.getFullName());
		dto.setGender(pharmacist.getGender());
		dto.setQualification(pharmacist.getQualification());
		dto.setDateOfBirth(pharmacist.getDateOfBirth());
		dto.setContactNumber(pharmacist.getContactNumber());
		dto.setGovernmentId(pharmacist.getGovernmentId());
		dto.setPharmacyLicense(pharmacist.getPharmacyLicense());
		dto.setYearsOfExperience(pharmacist.getYearsOfExperience());
		dto.setDateOfJoining(pharmacist.getDateOfJoining());
		dto.setDepartment(pharmacist.getDepartment());
		dto.setBankAccountDetails(pharmacist.getBankAccountDetails());
		dto.setAddress(pharmacist.getAddress());
		dto.setEmailID(pharmacist.getEmailID());
		dto.setShiftTimingsOrAvailability(pharmacist.getShiftTimingsOrAvailability());
		dto.setPreviousEmploymentHistory(pharmacist.getPreviousEmploymentHistory());
		dto.setEmergencyContactNumber(pharmacist.getEmergencyContactNumber());
		dto.setProfilePicture(Base64CompressionUtil.decompressBase64(pharmacist.getProfilePicture()));
		dto.setDpharmaOrBPharmaCertificate(
				Base64CompressionUtil.decompressBase64(pharmacist.getDpharmaOrBPharmaCertificate()));
		dto.setExperienceCertificates(Base64CompressionUtil.decompressBase64(pharmacist.getExperienceCertificates()));
		dto.setStatePharmacyCouncilRegistration(
				Base64CompressionUtil.decompressBase64(pharmacist.getStatePharmacyCouncilRegistration()));
		dto.setPermissions(pharmacist.getPermissions());
		dto.setCreatedBy(pharmacist.getCreatedBy());
		dto.setCreatedAt(pharmacist.getCreatedAt());
		dto.setUpdatedDate(pharmacist.getUpdatedDate());

		return dto;
	}

	private String generatePharmacistId() {
		return "PH_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
	}

	private String generateStructuredPassword() {
		String[] words = { "Pharma" };
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

// ---------------- PRESCRIPTION APIs ----------------
//	@Override
//	public ResponseEntity<Response> createPrescription(DoctorPrescriptionDTO dto) {
//	    try {
//	        return doctorServiceFeign.createPrescription(dto);
//	    } catch (FeignException ex) {
//	        try {
//	            Response doctorResponse = mapper.readValue(ex.contentUTF8(), Response.class);
//	            return ResponseEntity.status(ex.status()).body(doctorResponse);
//	        } catch (Exception e) {
//	            Response fallback = new Response();
//	            fallback.setSuccess(false);
//	            fallback.setMessage("Doctor Service error: " + ex.getMessage());
//	            fallback.setStatus(ex.status());
//	            return ResponseEntity.status(ex.status()).body(fallback);
//	        }
//	    }
//	}

//	@Override
//	public ResponseEntity<Response> getAllPrescriptions() {
//	    try {
//	        return doctorServiceFeign.getAllPrescriptions();
//	    } catch (FeignException ex) {
//	        try {
//	            Response doctorResponse = mapper.readValue(ex.contentUTF8(), Response.class);
//	            return ResponseEntity.status(ex.status()).body(doctorResponse);
//	        } catch (Exception e) {
//	            Response fallback = new Response();
//	            fallback.setSuccess(false);
//	            fallback.setMessage("Doctor Service error: " + ex.getMessage());
//	            fallback.setStatus(ex.status());
//	            return ResponseEntity.status(ex.status()).body(fallback);
//	        }
//	    }
//	}

//	@Override
//	public ResponseEntity<Response> getPrescriptionById(String id) {
//	    try {
//	        return doctorServiceFeign.getPrescriptionById(id);
//	    } catch (FeignException ex) {
//	        try {
//	            Response doctorResponse = mapper.readValue(ex.contentUTF8(), Response.class);
//	            return ResponseEntity.status(ex.status()).body(doctorResponse);
//	        } catch (Exception e) {
//	            Response fallback = new Response();
//	            fallback.setSuccess(false);
//	            fallback.setMessage("Doctor Service error: " + ex.getMessage());
//	            fallback.setStatus(ex.status());
//	            return ResponseEntity.status(ex.status()).body(fallback);
//	        }
//	    }
//	}

//	@Override
//	public ResponseEntity<Response> getMedicineById(String medicineId) {
//	    try {
//	        return doctorServiceFeign.getMedicineById(medicineId);
//	    } catch (FeignException ex) {
//	        try {
//	            Response doctorResponse = mapper.readValue(ex.contentUTF8(), Response.class);
//	            return ResponseEntity.status(ex.status()).body(doctorResponse);
//	        } catch (Exception e) {
//	            Response fallback = new Response();
//	            fallback.setSuccess(false);
//	            fallback.setMessage("Doctor Service error: " + ex.getMessage());
//	            fallback.setStatus(ex.status());
//	            return ResponseEntity.status(ex.status()).body(fallback);
//	        }
//	    }
//	}

//	@Override
//	public ResponseEntity<Response> searchMedicines(String keyword) {
//	    try {
//	        return doctorServiceFeign.searchMedicines(keyword);
//	    } catch (FeignException ex) {
//	        try {
//	            Response doctorResponse = mapper.readValue(ex.contentUTF8(), Response.class);
//	            return ResponseEntity.status(ex.status()).body(doctorResponse);
//	        } catch (Exception e) {
//	            Response fallback = new Response();
//	            fallback.setSuccess(false);
//	            fallback.setMessage("Doctor Service error: " + ex.getMessage());
//	            fallback.setStatus(ex.status());
//	            return ResponseEntity.status(ex.status()).body(fallback);
//	        }
//	    }
//	}

//	@Override
//	public ResponseEntity<Response> deletePrescription(String id) {
//	    try {
//	        return doctorServiceFeign.deletePrescription(id);
//	    } catch (FeignException ex) {
//	        try {
//	            Response doctorResponse = mapper.readValue(ex.contentUTF8(), Response.class);
//	            return ResponseEntity.status(ex.status()).body(doctorResponse);
//	        } catch (Exception e) {
//	            Response fallback = new Response();
//	            fallback.setSuccess(false);
//	            fallback.setMessage("Doctor Service error: " + ex.getMessage());
//	            fallback.setStatus(ex.status());
//	            return ResponseEntity.status(ex.status()).body(fallback);
//	        }
//	    }
//	}

//	@Override
//	public ResponseEntity<Response> deleteMedicine(String medicineId) {
//	    try {
//	        return doctorServiceFeign.deleteMedicine(medicineId);
//	    } catch (FeignException ex) {
//	        try {
//	            Response doctorResponse = mapper.readValue(ex.contentUTF8(), Response.class);
//	            return ResponseEntity.status(ex.status()).body(doctorResponse);
//	        } catch (Exception e) {
//	            Response fallback = new Response();
//	            fallback.setSuccess(false);
//	            fallback.setMessage("Doctor Service error: " + ex.getMessage());
//	            fallback.setStatus(ex.status());
//	            return ResponseEntity.status(ex.status()).body(fallback);
//	        }
//	    }
//	}

//	@Override
//	public ResponseEntity<Response> getPrescriptionsByClinicId(String clinicId) {
//	    try {
//	        return doctorServiceFeign.getPrescriptionsByClinicId(clinicId);
//	    } catch (FeignException ex) {
//	        try {
//	            Response doctorResponse = mapper.readValue(ex.contentUTF8(), Response.class);
//	            return ResponseEntity.status(ex.status()).body(doctorResponse);
//	        } catch (Exception e) {
//	            Response fallback = new Response();
//	            fallback.setSuccess(false);
//	            fallback.setMessage("Doctor Service error: " + ex.getMessage());
//	            fallback.setStatus(ex.status());
//	            return ResponseEntity.status(ex.status()).body(fallback);
//	        }
//	    }
//	    
//	    
//
//	}

//	@Override
//	public ResponseEntity<Response> updateMedicine(String medicineId, MedicineDTO dto) {
//	    try {
//	        return doctorServiceFeign.updateMedicine(medicineId, dto);
//	    } catch (FeignException ex) {
//	        try {
//	            Response doctorResponse = mapper.readValue(ex.contentUTF8(), Response.class);
//	            return ResponseEntity.status(ex.status()).body(doctorResponse);
//	        } catch (Exception e) {
//	            Response fallback = new Response();
//	            fallback.setSuccess(false);
//	            fallback.setMessage("Doctor Service error: " + ex.getMessage());
//	            fallback.setStatus(ex.status());
//	            return ResponseEntity.status(ex.status()).body(fallback);
//	        }
//	    }
//	}
//	

	// ---------------- MEDICINE TYPE APIs (NEW) ----------------

//	@Override
//	public ResponseEntity<Response> searchOrAddMedicineType(MedicineTypeDTO dto) {
//	    try {
//	        return doctorServiceFeign.searchOrAddMedicineType(dto);
//	    } catch (FeignException ex) {
//	        try {
//	            Response doctorResponse = mapper.readValue(ex.contentUTF8(), Response.class);
//	            return ResponseEntity.status(ex.status()).body(doctorResponse);
//	        } catch (Exception e) {
//	            Response fallback = new Response();
//	            fallback.setSuccess(false);
//	            fallback.setMessage("Doctor Service error: " + ex.getMessage());
//	            fallback.setStatus(ex.status());
//	            return ResponseEntity.status(ex.status()).body(fallback);
//	        }
//	    }
//	}

//	@Override
//	public ResponseEntity<Response> getMedicineTypes(String clinicId) {
//	    try {
//	        return doctorServiceFeign.getMedicineTypes(clinicId);
//	    } catch (FeignException ex) {
//	        try {
//	            Response doctorResponse = mapper.readValue(ex.contentUTF8(), Response.class);
//	            return ResponseEntity.status(ex.status()).body(doctorResponse);
//	        } catch (Exception e) {
//	            Response fallback = new Response();
//	            fallback.setSuccess(false);
//	            fallback.setMessage("Doctor Service error: " + ex.getMessage());
//	            fallback.setStatus(ex.status());
//	            return ResponseEntity.status(ex.status()).body(fallback);
//	        }
//	    }
//	}

//		@Override
//		public ResponseEntity<Response> getMedicineType(String Id) {
//		    try {
//		        return doctorServiceFeign.getMedicineType(Id);
//		    } catch (FeignException ex) {
//		        try {
//		            Response doctorResponse = mapper.readValue(ex.contentUTF8(), Response.class);
//		            return ResponseEntity.status(ex.status()).body(doctorResponse);
//		        } catch (Exception e) {
//		            Response fallback = new Response();
//		            fallback.setSuccess(false);
//		            fallback.setMessage("Doctor Service error: " + ex.getMessage());
//		            fallback.setStatus(ex.status());
//		            return ResponseEntity.status(ex.status()).body(fallback);
//		        }
//		    }
//	}

//		@Override
//		public ResponseEntity<Response> getAllMedicineTypes() {
//		    try {
//		        return doctorServiceFeign.getAllMedicineTypes();
//		    } catch (FeignException ex) {
//		        try {
//		            Response doctorResponse = mapper.readValue(ex.contentUTF8(), Response.class);
//		            return ResponseEntity.status(ex.status()).body(doctorResponse);
//		        } catch (Exception e) {
//		            Response fallback = new Response();
//		            fallback.setSuccess(false);
//		            fallback.setMessage("Doctor Service error: " + ex.getMessage());
//		            fallback.setStatus(ex.status());
//		            return ResponseEntity.status(ex.status()).body(fallback);
//		        }
//		    }
//	}

}