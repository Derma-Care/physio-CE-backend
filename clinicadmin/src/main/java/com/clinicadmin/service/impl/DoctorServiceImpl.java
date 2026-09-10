package com.clinicadmin.service.impl;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import com.clinicadmin.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.clinicadmin.entity.DoctorAndStaffLoginCredentials;
import com.clinicadmin.entity.DoctorCounter;
import com.clinicadmin.entity.DoctorSlot;
import com.clinicadmin.entity.Doctors;
import com.clinicadmin.feignclient.AdminServiceClient;
import com.clinicadmin.feignclient.BookingFeign;
import com.clinicadmin.feignclient.NotificationFeign;
import com.clinicadmin.repository.DoctorLoginCredentialsRepository;
import com.clinicadmin.repository.DoctorSlotRepository;
import com.clinicadmin.repository.DoctorsRepository;
import com.clinicadmin.service.DoctorService;
import com.clinicadmin.service.EmailService;
import com.clinicadmin.service.S3Service;
import com.clinicadmin.utils.DoctorMapper;
import com.clinicadmin.utils.DoctorSlotMapper;
import com.clinicadmin.utils.ExtractFeignMessage;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
public class DoctorServiceImpl implements DoctorService {

	@Autowired
	private DoctorsRepository doctorsRepository;

	@Autowired
	private DoctorLoginCredentialsRepository credentialsRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private DoctorSlotRepository slotRepository;

	@Autowired
	AdminServiceClient adminServiceClient;

	@Autowired
	private NotificationFeign notificationFeign;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private MongoOperations mongoOperations;

	@Autowired
	private BookingFeign bookingFeign;

	@Autowired
	private EmailService emailService;

	@Autowired
	private S3Service s3Service;

	private final List<TempBlockingSlot> slots = new CopyOnWriteArrayList<>();

	private static final DateTimeFormatter SLOT_TIME_FORMATTER = new DateTimeFormatterBuilder()
			.parseCaseInsensitive().appendPattern("h:mm a").toFormatter(Locale.ENGLISH);

	private static final ZoneId CLINIC_ZONE_ID = ZoneId.of("Asia/Kolkata");

	public DoctorServiceImpl(DoctorsRepository doctorsRepository,
			DoctorLoginCredentialsRepository credentialsRepository, PasswordEncoder passwordEncoder,
			DoctorSlotRepository slotRepository) {
		this.doctorsRepository = doctorsRepository;
		this.credentialsRepository = credentialsRepository;
		this.passwordEncoder = passwordEncoder;
		this.slotRepository = slotRepository;
	}

	// =====================================================================================
	// ADD DOCTOR
	// =====================================================================================
	@Override
	public Response addDoctor(DoctorsDTO dto) {
		log.info("Add Doctor request received. mobile={}, hospitalId={}, branchId={}", dto.getDoctorMobileNumber(),
				dto.getHospitalId(), dto.getBranchId());
		Response response = new Response();
		try {
			dto.trimAllDoctorFields();
			log.debug("Doctor DTO fields trimmed successfully");

			// -------------------- Duplicate checks (create) --------------------
			Response duplicateCheckResponse = validateDuplicateFieldsOnCreate(dto);
			if (duplicateCheckResponse != null) {
				return duplicateCheckResponse;
			}

			if (credentialsRepository.existsByUsername(dto.getDoctorMobileNumber())) {
				log.warn("Login credentials already exist for this mobile number: {}", dto.getDoctorMobileNumber());
				return failure("Login credentials already exist for this mobile number", HttpStatus.BAD_REQUEST);
			}

			// -------------------- Validate clinic --------------------
			log.debug("Validating clinicId: {}", dto.getHospitalId());
			ResponseEntity<Response> clinicRes;
			try {
				clinicRes = adminServiceClient.getClinicById(dto.getHospitalId());
			} catch (FeignException fe) {
				log.error("Clinic not found via admin service. clinicId={}", dto.getHospitalId());
				return failure("Clinic not found with ID: " + dto.getHospitalId(), HttpStatus.NOT_FOUND);
			}

			if (clinicRes.getBody() == null || !clinicRes.getBody().isSuccess()) {
				log.warn("Clinic validation failed. clinicId={}", dto.getHospitalId());
				return failure("Clinic not found with ID: " + dto.getHospitalId(), HttpStatus.NOT_FOUND);
			}

			ClinicDTO clinicDTO = objectMapper.convertValue(clinicRes.getBody().getData(), ClinicDTO.class);
			log.info("Clinic validated successfully. clinicId={}, clinicName={}", dto.getHospitalId(),
					dto.getHospitalName());

			// -------------------- Validate branch --------------------
			if (dto.getBranchId() == null || dto.getBranchId().isBlank()) {
				log.warn("Branch Id is missing for clinicId={}", dto.getHospitalId());
				return failure("Branch ID is required", HttpStatus.BAD_REQUEST);
			}
			log.debug("Validating branchId={}, clinicId={}", dto.getBranchId(), dto.getHospitalId());
			ResponseEntity<Response> branchRes;
			try {
				branchRes = adminServiceClient.getBranchByClinicAndBranchId(dto.getHospitalId(), dto.getBranchId());
			} catch (FeignException fe) {
				log.error("Branch not found via Admin Service. clinicId={}, branchId={}", dto.getHospitalId(),
						dto.getBranchId());
				return failure("Branch not found for clinicId: " + dto.getHospitalId() + " and branchId: "
						+ dto.getBranchId(), HttpStatus.NOT_FOUND);
			}

			if (branchRes.getBody() == null || !branchRes.getBody().isSuccess()) {
				log.warn("Branch validation failed. clinicId={}, branchId={}", dto.getHospitalId(), dto.getBranchId());
				return failure("Branch not found for clinicId: " + dto.getHospitalId() + " and branchId: "
						+ dto.getBranchId(), HttpStatus.NOT_FOUND);
			}

			Branch branchDTO = objectMapper.convertValue(branchRes.getBody().getData(), Branch.class);
			log.debug("Branch resolved. branchName={}, branchId={}, clinicId={}", branchDTO.getBranchName(),
					branchDTO.getBranchId(), branchDTO.getClinicId());

			// -------------------- Generate doctorId --------------------
			log.debug("Generating doctorId for clinicId={}, branchId={}", dto.getHospitalId(), dto.getBranchId());
			String clinicSeq = String.format("%04d", Integer.parseInt(dto.getHospitalId()));
			String branchSeq = branchDTO.getBranchId().substring(clinicSeq.length());

			String counterKey = "doctor_" + dto.getHospitalId() + "_" + branchDTO.getBranchId();
			Query query = Query.query(Criteria.where("_id").is(counterKey));
			Update update = new Update().inc("seq", 1);
			FindAndModifyOptions options = FindAndModifyOptions.options().upsert(true).returnNew(true);

			DoctorCounter counter = mongoOperations.findAndModify(query, update, options, DoctorCounter.class);
			long nextDoctorSeq = (counter != null) ? counter.getSeq() : 1L;
			String doctorSeq = String.format("%02d", nextDoctorSeq);

			String doctorId = clinicSeq + branchSeq + doctorSeq;
			log.info("Generated doctorId={}", doctorId);
			dto.setDoctorId(doctorId);

			// -------------------- Map DTO -> Entity --------------------
			Doctors doctor = DoctorMapper.mapDoctorDTOtoDoctorEntity(dto);
			doctor.setDoctorId(doctorId);
			doctor.setHospitalName(clinicDTO.getName());
			// Strict branch assignment: only use the branch provided in payload
			doctor.setBranchId(dto.getBranchId());

			// -------------------- Save doctor --------------------
			Doctors savedDoctor = doctorsRepository.save(doctor);
			log.info("Doctor saved successfully. doctorId={}", savedDoctor.getDoctorId());

			// -------------------- Create login credentials --------------------
			String username = savedDoctor.getDoctorMobileNumber();
			String rawPassword = generateStructuredPassword();
			String encodedPassword = passwordEncoder.encode(rawPassword);

			DoctorAndStaffLoginCredentials credentials = DoctorAndStaffLoginCredentials.builder()
					.staffId(savedDoctor.getDoctorId()).staffName(savedDoctor.getDoctorName())
					.hospitalId(savedDoctor.getHospitalId()).mobilenumber(savedDoctor.getDoctorMobileNumber())
					.hospitalName(savedDoctor.getHospitalName()).branchId(savedDoctor.getBranchId()).username(username)
					.password(encodedPassword).role(dto.getRole()).emailId(savedDoctor.getDoctorEmail())
					.permissions(savedDoctor.getPermissions()).build();

			credentialsRepository.save(credentials);
			log.info("Login credentials created successfully for doctorId={}", savedDoctor.getDoctorId());

			// -------------------- Send email to doctor --------------------
			try {
				Map<String, String> mailData = new HashMap<>();
				mailData.put("clinicName", clinicDTO.getName());
				mailData.put("subject", "Doctor Onboarding Successful");
				mailData.put("message", "Your account has been created successfully.\n"
						+ "Please use the below credentials to login.\n\n" + "Doctor ID: " + savedDoctor.getDoctorId());
				mailData.put("username", username);
				mailData.put("password", rawPassword);
				mailData.put("role", dto.getRole());

				emailService.sendEmail(savedDoctor.getDoctorEmail(), mailData);
				log.info("Doctor onboarding email sent to {}", savedDoctor.getDoctorEmail());
			} catch (Exception e) {
				log.error("Failed to send doctor onboarding email: {}", e.getMessage());
			}

			DoctorsDTO toDTO = DoctorMapper.mapDoctorEntityToDoctorDTO(savedDoctor, s3Service);
			Map<String, Object> data = new HashMap<>();
			data.put("doctor", toDTO);
			data.put("username", username);
			data.put("temporaryPassword", rawPassword);
			data.put("generatedDoctorId", doctorId);

			response.setSuccess(true);
			response.setData(data);
			response.setMessage("Doctor added successfully with login credentials");
			response.setStatus(HttpStatus.CREATED.value());

		} catch (Exception e) {
			log.error("Exception occurred while adding doctor. mobile={}, hospitalId={}, exception={}",
					dto.getDoctorMobileNumber(), dto.getHospitalId(), e.getMessage(), e);
			response.setSuccess(false);
			response.setMessage("Error occurred while adding doctor: " + e.getMessage());
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		log.info("Add Doctor request completed. status={}", response.getStatus());
		return response;
	}

	@Override
	public Response getAllDoctors() {
		log.info("Get All Doctors request received");
		Response response = new Response();
		try {
			log.debug("Fetching doctors data from database");
			List<Doctors> doctors = doctorsRepository.findAll();

			if (doctors.isEmpty()) {
				log.warn("No doctors found in the system");
				response.setSuccess(true);
				response.setData(Collections.emptyList());
				response.setMessage("No doctor data available");
				response.setStatus(HttpStatus.OK.value());
			} else {
				log.info("Number of doctors found: {}", doctors.size());
				List<DoctorsDTO> toDTO = doctors.stream()
						.map(doc -> DoctorMapper.mapDoctorEntityToDoctorDTO(doc, s3Service))
						.collect(Collectors.toList());
				response.setSuccess(true);
				response.setData(toDTO);
				response.setMessage("Doctor data retrieved successfully");
				response.setStatus(HttpStatus.OK.value());
			}

		} catch (Exception e) {
			log.error("Exception occurred while fetching all doctors: {}", e.getMessage(), e);
			response.setSuccess(false);
			response.setData(null);
			response.setMessage("Error while fetching doctors: " + e.getMessage());
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		log.info("Get all doctors request completed. status={}", response.getStatus());
		return response;
	}

	@Override
	public Response getDoctorsByClinicId(String hospitalId) {
		log.info("Get doctors by clinicId request received. hospitalId={}", hospitalId);
		Response response = new Response();
		try {
			log.debug("Fetching doctors data from database. hospitalId={}", hospitalId);
			List<Doctors> doctorList = doctorsRepository.findByHospitalId(hospitalId);
			if (!doctorList.isEmpty()) {
				log.info("Doctors found. hospitalId={}, count={}", hospitalId, doctorList.size());
				List<DoctorsDTO> dtos = doctorList.stream()
						.map(doc -> DoctorMapper.mapDoctorEntityToDoctorDTO(doc, s3Service))
						.collect(Collectors.toList());
				response.setSuccess(true);
				response.setData(dtos);
				response.setMessage("Doctors fetched successfully");
				response.setStatus(200);
			} else {
				log.warn("No doctors found in hospitalId={}", hospitalId);
				response.setSuccess(true);
				response.setData(Collections.emptyList());
				response.setMessage("No doctors found for hospitalId: " + hospitalId);
				response.setStatus(200);
			}
		} catch (Exception e) {
			log.error("Exception occurred while fetching doctors using hospitalId={}, exception={}", hospitalId,
					e.getMessage(), e);
			response.setSuccess(false);
			response.setMessage("An error occurred while fetching doctors for hospitalId: " + hospitalId);
			response.setStatus(500);
		}
		log.info("Get doctors by clinicId request completed. hospitalId={}, status={}", hospitalId,
				response.getStatus());
		return response;
	}

	@Override
	public Response getDoctorById(String id) {
		log.info("Get Doctor by id request received: {}", id);
		Response response = new Response();
		try {
			log.debug("Fetching doctor data from database. doctorId={}", id);
			Optional<Doctors> doctorOptional = doctorsRepository.findByDoctorId(id);

			if (doctorOptional.isPresent()) {
				Doctors dataFromDB = doctorOptional.get();
				DoctorsDTO toDTO = DoctorMapper.mapDoctorEntityToDoctorDTO(dataFromDB, s3Service);
				log.info("Doctor found. doctorId={}, doctorName={}", toDTO.getDoctorId(), toDTO.getDoctorName());
				response.setSuccess(true);
				response.setData(toDTO);
				response.setMessage("Doctor retrieved successfully");
				response.setStatus(HttpStatus.OK.value());
			} else {
				log.warn("Doctor not found with this doctorId={}", id);
				response.setSuccess(false);
				response.setData(null);
				response.setMessage("Doctor not found with ID: " + id);
				response.setStatus(HttpStatus.NOT_FOUND.value());
			}
		} catch (Exception e) {
			log.error("Exception occurred while fetching doctor by ID: {}", e.getMessage(), e);
			response.setSuccess(false);
			response.setData(null);
			response.setMessage("Error fetching doctor by ID: " + e.getMessage());
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return response;
	}

	// =====================================================================================
	// UPDATE DOCTOR
	// =====================================================================================
	@Override
	public Response upDateDoctorById(String doctorId, DoctorsDTO dto) {
		log.info("Update doctor request received for doctorId={}", doctorId);

		Response response = new Response();
		try {
			log.debug("Fetching doctor from database for doctorId={}", doctorId);
			Optional<Doctors> doctorOptional = doctorsRepository.findByDoctorId(doctorId);

			if (doctorOptional.isEmpty()) {
				log.warn("Doctor not found with doctorId={}", doctorId);
				return failure("Doctor not found with ID: " + doctorId, HttpStatus.NOT_FOUND);
			}

			Doctors doctor = doctorOptional.get();
			log.debug("Doctor found. Validating updated fields for doctorId={}", doctorId);

			// -------------------- Duplicate checks (update, excluding self) --------------------
			Response duplicateCheckResponse = validateDuplicateFieldsOnUpdate(dto, doctor, doctorId);
			if (duplicateCheckResponse != null) {
				return duplicateCheckResponse;
			}

			/* ---------- FIELD UPDATES ---------- */
			if (dto.getDoctorPicture() != null && !dto.getDoctorPicture().isBlank())
				doctor.setDoctorPicture(dto.getDoctorPicture()); // S3 key stored as-is
			if (dto.getHospitalId() != null)
				doctor.setHospitalId(dto.getHospitalId());
			if (dto.getDoctorEmail() != null)
				doctor.setDoctorEmail(dto.getDoctorEmail());
			if (dto.getDoctorLicence() != null)
				doctor.setDoctorLicence(dto.getDoctorLicence());
			if (dto.getDoctorMobileNumber() != null)
				doctor.setDoctorMobileNumber(dto.getDoctorMobileNumber());
			if (dto.getProviderType() != null)
				doctor.setProviderType(dto.getProviderType());
			if (dto.getDoctorName() != null)
				doctor.setDoctorName(dto.getDoctorName());
			if (dto.getSpecialization() != null)
				doctor.setSpecialization(dto.getSpecialization());
			if (dto.getGender() != null)
				doctor.setGender(dto.getGender());
			if (dto.getExperience() != null)
				doctor.setExperience(dto.getExperience());
			if (dto.getQualification() != null)
				doctor.setQualification(dto.getQualification());
			if (dto.getAvailableDays() != null)
				doctor.setAvailableDays(dto.getAvailableDays());
			if (dto.getAvailableTimes() != null)
				doctor.setAvailableTimes(dto.getAvailableTimes());
			if (dto.getProfileDescription() != null)
				doctor.setProfileDescription(dto.getProfileDescription());
			if (dto.getFocusAreas() != null)
				doctor.setFocusAreas(dto.getFocusAreas());
			if (dto.getDoctorAverageRating() != 0.0)
				doctor.setDoctorAverageRating(dto.getDoctorAverageRating());
			if (dto.getLanguages() != null)
				doctor.setLanguages(dto.getLanguages());
			if (dto.getHighlights() != null)
				doctor.setHighlights(dto.getHighlights());
			if (dto.getDateofJoining() != null)
				doctor.setDateofJoining(dto.getDateofJoining());
			if (dto.getAadharID() != null)
				doctor.setAadharID(dto.getAadharID());
			if (dto.getDoctorSignature() != null && !dto.getDoctorSignature().isBlank())
				doctor.setDoctorSignature(dto.getDoctorSignature()); // S3 key stored as-is
			if (dto.getDoctorFees() != null)
				doctor.setDoctorFees(DoctorMapper.mapDoctorFeeDTOtoEntity(dto.getDoctorFees()));

			if (dto.getBankAccountDetails() != null) {
				BankAccountDetails bankDetails = doctor.getBankAccountDetails();

				if (bankDetails != null) {
					if (dto.getBankAccountDetails().getAccountHolderName() != null) {
						bankDetails.setAccountHolderName(dto.getBankAccountDetails().getAccountHolderName());
					}
					if (dto.getBankAccountDetails().getAccountNumber() != null) {
						bankDetails.setAccountNumber(dto.getBankAccountDetails().getAccountNumber());
					}
					if (dto.getBankAccountDetails().getBankName() != null) {
						bankDetails.setBankName(dto.getBankAccountDetails().getBankName());
					}
					if (dto.getBankAccountDetails().getBranchName() != null) {
						bankDetails.setBranchName(dto.getBankAccountDetails().getBranchName());
					}
					if (dto.getBankAccountDetails().getIfscCode() != null) {
						bankDetails.setIfscCode(dto.getBankAccountDetails().getIfscCode());
					}
					if (dto.getBankAccountDetails().getPanCardNumber() != null) {
						bankDetails.setPanCardNumber(dto.getBankAccountDetails().getPanCardNumber());
					}
					doctor.setBankAccountDetails(bankDetails);
				} else {
					doctor.setBankAccountDetails(dto.getBankAccountDetails());
				}
			}

			if (dto.getDoctorAvailabilityStatus() != null) {
				doctor.setDoctorAvailabilityStatus(dto.getDoctorAvailabilityStatus());
			}

			if (dto.isRecommendation() != doctor.isRecommendation()) {
				doctor.setRecommendation(dto.isRecommendation());
			}

			if (dto.isAssociatedWithIADVC() != doctor.isAssociatedWithIADVC()) {
				doctor.setAssociatedWithIADVC(dto.isAssociatedWithIADVC());
			}

			if (dto.getAssociationsOrMemberships() != null && !dto.getAssociationsOrMemberships().isEmpty()) {
				doctor.setAssociationsOrMemberships(dto.getAssociationsOrMemberships());
			}

			if (dto.getBranches() != null && !dto.getBranches().isEmpty()) {
				doctor.setBranches(dto.getBranches());
			}

			log.info("Saving updated doctor data for doctorId={}", doctorId);
			Doctors updatedDoctor = doctorsRepository.save(doctor);

			// -------------------- Sync login credentials --------------------
			Optional<DoctorAndStaffLoginCredentials> credentialsOpt = credentialsRepository.findByStaffId(doctorId);

			if (credentialsOpt.isPresent()) {
				DoctorAndStaffLoginCredentials creds = credentialsOpt.get();

				if (dto.getDoctorName() != null)
					creds.setStaffName(dto.getDoctorName());
				if (dto.getBranchId() != null)
					creds.setBranchId(dto.getBranchId());
				if (dto.getHospitalId() != null)
					creds.setHospitalId(dto.getHospitalId());
				if (dto.getHospitalName() != null)
					creds.setHospitalName(dto.getHospitalName());
				if (dto.getRole() != null)
					creds.setRole(dto.getRole());
				if (dto.getPermissions() != null)
					creds.setPermissions(dto.getPermissions());
				if (dto.getDoctorMobileNumber() != null)
					creds.setMobilenumber(dto.getDoctorMobileNumber());
				if (dto.getDoctorEmail() != null)
					creds.setEmailId(dto.getDoctorEmail());

				credentialsRepository.save(creds);
				log.info("Login credentials updated for doctorId={}", doctorId);
			} else {
				log.warn("Login credentials not found for doctorId={}", doctorId);
			}

			DoctorsDTO toDTO = DoctorMapper.mapDoctorEntityToDoctorDTO(updatedDoctor, s3Service);

			response.setSuccess(true);
			response.setData(toDTO);
			response.setMessage("Doctor updated successfully");
			response.setStatus(HttpStatus.OK.value());

			log.info("Doctor updated successfully for doctorId={}", doctorId);

		} catch (Exception e) {
			log.error("Exception occurred while updating doctorId={}", doctorId, e);
			response.setSuccess(false);
			response.setData(null);
			response.setMessage("Error updating doctor: " + e.getMessage());
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return response;
	}

	// =====================================================================================
	// DUPLICATE VALIDATION HELPERS
	// =====================================================================================

	/**
	 * Validates uniqueness of mobile number, email, licence, and Aadhar ID when
	 * creating a new doctor. Returns a populated failure {@link Response} if a
	 * duplicate is found, or {@code null} if all fields are unique.
	 */
	private Response validateDuplicateFieldsOnCreate(DoctorsDTO dto) {

		if (doctorsRepository.existsByDoctorMobileNumber(dto.getDoctorMobileNumber())) {
			log.warn("Duplicate doctor mobile number detected, mobileNumber={}", dto.getDoctorMobileNumber());
			return failure("Doctor with this mobile number already exists", HttpStatus.BAD_REQUEST);
		}

		if (isNotBlank(dto.getDoctorEmail()) && doctorsRepository.existsByDoctorEmail(dto.getDoctorEmail())) {
			log.warn("Duplicate doctor email detected, email={}", dto.getDoctorEmail());
			return failure("Doctor with this email already exists", HttpStatus.BAD_REQUEST);
		}

		if (isNotBlank(dto.getDoctorLicence()) && doctorsRepository.existsByDoctorLicence(dto.getDoctorLicence())) {
			log.warn("Duplicate doctor licence detected, licence={}", dto.getDoctorLicence());
			return failure("Doctor with this licence number already exists", HttpStatus.BAD_REQUEST);
		}

		if (isNotBlank(dto.getAadharID()) && doctorsRepository.existsByAadharID(dto.getAadharID())) {
			log.warn("Duplicate Aadhar ID detected, aadharID={}", dto.getAadharID());
			return failure("Doctor with this Aadhar ID already exists", HttpStatus.BAD_REQUEST);
		}

		return null;
	}

	/**
	 * Validates uniqueness of mobile number, email, licence, and Aadhar ID when
	 * updating an existing doctor, excluding the doctor's own current record from
	 * the comparison. Returns a populated failure {@link Response} if a duplicate
	 * is found, or {@code null} if all changed fields are unique.
	 */
	private Response validateDuplicateFieldsOnUpdate(DoctorsDTO dto, Doctors existingDoctor, String doctorId) {

		if (isNotBlank(dto.getDoctorMobileNumber())
				&& !dto.getDoctorMobileNumber().equals(existingDoctor.getDoctorMobileNumber())
				&& doctorsRepository.existsByDoctorMobileNumberAndDoctorIdNot(dto.getDoctorMobileNumber(), doctorId)) {
			log.warn("Duplicate mobile number on update, doctorId={}, mobile={}", doctorId,
					dto.getDoctorMobileNumber());
			return failure("Another doctor with this mobile number already exists", HttpStatus.BAD_REQUEST);
		}

		if (isNotBlank(dto.getDoctorEmail()) && !dto.getDoctorEmail().equals(existingDoctor.getDoctorEmail())
				&& doctorsRepository.existsByDoctorEmailAndDoctorIdNot(dto.getDoctorEmail(), doctorId)) {
			log.warn("Duplicate email on update, doctorId={}, email={}", doctorId, dto.getDoctorEmail());
			return failure("Another doctor with this email already exists", HttpStatus.BAD_REQUEST);
		}

		if (isNotBlank(dto.getDoctorLicence()) && !dto.getDoctorLicence().equals(existingDoctor.getDoctorLicence())
				&& doctorsRepository.existsByDoctorLicenceAndDoctorIdNot(dto.getDoctorLicence(), doctorId)) {
			log.warn("Duplicate licence on update, doctorId={}, licence={}", doctorId, dto.getDoctorLicence());
			return failure("Another doctor with this licence number already exists", HttpStatus.BAD_REQUEST);
		}

		if (isNotBlank(dto.getAadharID()) && !dto.getAadharID().equals(existingDoctor.getAadharID())
				&& doctorsRepository.existsByAadharIDAndDoctorIdNot(dto.getAadharID(), doctorId)) {
			log.warn("Duplicate Aadhar ID on update, doctorId={}, aadharID={}", doctorId, dto.getAadharID());
			return failure("Another doctor with this Aadhar ID already exists", HttpStatus.BAD_REQUEST);
		}

		return null;
	}

	private boolean isNotBlank(String value) {
		return value != null && !value.isBlank();
	}

	private Response failure(String message, HttpStatus status) {
		Response response = new Response();
		response.setSuccess(false);
		response.setMessage(message);
		response.setStatus(status.value());
		return response;
	}

	@Override
	public Response getDoctorsByClinicIdAndDoctorId(String clinicId, String doctorId) {

		log.info("Get Doctor request received. clinicId={}, doctorId={}", clinicId, doctorId);

		Response response = new Response();

		try {
			if (clinicId == null || clinicId.trim().isEmpty()) {
				log.warn("ClinicId is missing or empty");
				response.setSuccess(false);
				response.setData(Collections.emptyList());
				response.setMessage("Clinic ID (hospitalId) is required.");
				response.setStatus(HttpStatus.BAD_REQUEST.value());
				return response;
			}

			if (doctorId == null || doctorId.trim().isEmpty()) {
				log.warn("DoctorId is missing or empty for clinicId={}", clinicId);
				response.setSuccess(false);
				response.setData(Collections.emptyList());
				response.setMessage("Doctor ID is required.");
				response.setStatus(HttpStatus.BAD_REQUEST.value());
				return response;
			}

			log.debug("Fetching doctor from DB for clinicId={}, doctorId={}", clinicId, doctorId);
			Optional<Doctors> doctorOptional = doctorsRepository.findByHospitalIdAndDoctorId(clinicId, doctorId);

			if (doctorOptional.isPresent()) {
				log.info("Doctor found. clinicId={}, doctorId={}", clinicId, doctorId);
				Doctors dbData = doctorOptional.get();
				DoctorsDTO toDTO = DoctorMapper.mapDoctorEntityToDoctorDTO(dbData, s3Service);

				response.setSuccess(true);
				response.setData(toDTO);
				response.setMessage("Doctor retrieved successfully");
				response.setStatus(HttpStatus.OK.value());
			} else {
				log.warn("Doctor not found. clinicId={}, doctorId={}", clinicId, doctorId);
				response.setSuccess(false);
				response.setData(Collections.emptyList());
				response.setMessage("Doctor not found with ID: " + doctorId + " in Clinic: " + clinicId);
				response.setStatus(HttpStatus.NOT_FOUND.value());
			}

		} catch (Exception e) {
			log.error("Exception while fetching doctor. clinicId={}, doctorId={}", clinicId, doctorId, e);
			response.setSuccess(false);
			response.setData(Collections.emptyList());
			response.setMessage("Error fetching doctor: " + e.getMessage());
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		log.info("Get Doctor request completed. clinicId={}, doctorId={}, status={}", clinicId, doctorId,
				response.getStatus());

		return response;
	}

	@Override
	public Response deleteDoctorById(String doctorId) {

		log.info("Delete doctor request received for doctorId={}", doctorId);
		Response response = new Response();

		try {
			log.debug("Fetching doctor from DB for doctorId={}", doctorId);
			Optional<Doctors> optionalDoctor = doctorsRepository.findByDoctorId(doctorId);

			if (optionalDoctor.isPresent()) {
				log.info("Doctor found. Deleting doctor record for doctorId={}", doctorId);
				doctorsRepository.deleteById(optionalDoctor.get().getId());

				log.debug("Checking login credentials for doctorId={}", doctorId);
				Optional<DoctorAndStaffLoginCredentials> optionalCredentials = credentialsRepository
						.findByStaffId(doctorId);

				optionalCredentials.ifPresent(credentials -> {
					log.info("Deleting login credentials for doctorId={}", doctorId);
					credentialsRepository.delete(credentials);
				});

				response.setSuccess(true);
				response.setStatus(HttpStatus.OK.value());
				response.setMessage("Doctor and credentials deleted successfully.");

				log.info("Doctor and credentials deleted successfully for doctorId={}", doctorId);
			} else {
				log.warn("Doctor not found for deletion, doctorId={}", doctorId);
				response.setSuccess(false);
				response.setStatus(HttpStatus.NOT_FOUND.value());
				response.setMessage("Doctor not found with ID: " + doctorId);
			}

		} catch (Exception e) {
			log.error("Exception while deleting doctorId={}", doctorId, e);
			response.setSuccess(false);
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			response.setMessage("Error deleting doctor: " + e.getMessage());
		}

		return response;
	}

	@Override
	public Response deleteDoctorFromBranch(String doctorId, String branchId) {

		log.info("Delete doctor from branch request received. doctorId={}, branchId={}", doctorId, branchId);
		Response response = new Response();

		Optional<Doctors> optionalDoctor = doctorsRepository.findByDoctorId(doctorId);

		if (optionalDoctor.isEmpty()) {
			log.warn("Doctor not found for doctorId={}", doctorId);
			response.setSuccess(false);
			response.setStatus(HttpStatus.NOT_FOUND.value());
			response.setMessage("Doctor not found with ID: " + doctorId);
			return response;
		}

		Doctors doctor = optionalDoctor.get();

		if (doctor.getBranches() == null || doctor.getBranches().isEmpty()) {
			log.warn("Doctor has no branches assigned. doctorId={}", doctorId);
			response.setSuccess(false);
			response.setStatus(HttpStatus.BAD_REQUEST.value());
			response.setMessage("Doctor has no branches assigned");
			return response;
		}

		log.debug("Attempting to remove branchId={} from doctorId={}", branchId, doctorId);
		boolean removed = doctor.getBranches().removeIf(b -> b.getBranchId().equals(branchId));

		if (!removed) {
			log.warn("Doctor not assigned to branchId={} for doctorId={}", branchId, doctorId);
			response.setSuccess(false);
			response.setStatus(HttpStatus.NOT_FOUND.value());
			response.setMessage("Doctor not assigned to branch: " + branchId);
			return response;
		}

		if (doctor.getBranches().isEmpty()) {
			log.info("No branches left. Deleting doctor entirely for doctorId={}", doctorId);
			doctorsRepository.deleteById(doctor.getId());

			Optional<DoctorAndStaffLoginCredentials> optionalCredentials = credentialsRepository
					.findByStaffId(doctorId);

			optionalCredentials.ifPresent(credentials -> {
				log.info("Deleting credentials for doctorId={}", doctorId);
				credentialsRepository.delete(credentials);
			});

			response.setSuccess(true);
			response.setStatus(HttpStatus.OK.value());
			response.setMessage("Doctor deleted entirely as no branches left");

		} else {
			log.info("Updating doctor after branch removal. doctorId={}", doctorId);
			doctorsRepository.save(doctor);

			response.setSuccess(true);
			response.setStatus(HttpStatus.OK.value());
			response.setMessage("Doctor removed from branch successfully");
		}

		return response;
	}

	@Override
	public Response deleteDoctorsByClinic(String hospitalId) {

		log.info("Delete doctors by clinic request received. hospitalId={}", hospitalId);
		Response response = new Response();

		try {
			List<Doctors> doctors = doctorsRepository.findByHospitalId(hospitalId);

			if (!doctors.isEmpty()) {
				log.info("Found {} doctors for hospitalId={}", doctors.size(), hospitalId);

				for (Doctors doctor : doctors) {
					log.debug("Deleting credentials for doctorId={}", doctor.getDoctorId());
					Optional<DoctorAndStaffLoginCredentials> optionalCredentials = credentialsRepository
							.findByStaffId(doctor.getDoctorId());
					optionalCredentials.ifPresent(credentialsRepository::delete);

					log.debug("Deleting doctor record for doctorId={}", doctor.getDoctorId());
					doctorsRepository.deleteById(doctor.getId());
				}

				response.setSuccess(true);
				response.setStatus(HttpStatus.OK.value());
				response.setMessage(
						"All doctors and their credentials linked to clinic ID " + hospitalId + " have been deleted.");

				log.info("All doctors deleted successfully for hospitalId={}", hospitalId);

			} else {
				log.warn("No doctors found for hospitalId={}", hospitalId);
				response.setSuccess(false);
				response.setStatus(HttpStatus.NOT_FOUND.value());
				response.setMessage("No doctors found for clinic ID: " + hospitalId);
			}

		} catch (Exception e) {
			log.error("Exception while deleting doctors for hospitalId={}", hospitalId, e);
			response.setSuccess(false);
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			response.setMessage("Error deleting doctors linked to clinic ID " + hospitalId + ": " + e.getMessage());
		}

		return response;
	}

	// -------------------------------DOCTOR LOGIN-------------------------------------------------------------
	@Override
	public Response login(DoctorAndStaffLoginDto loginDTO) {

		log.info("Doctor login request received for username={}", loginDTO.getUserName());

		Response response = new Response();

		try {
			log.debug("Fetching login credentials for username={}", loginDTO.getUserName());
			Optional<DoctorAndStaffLoginCredentials> credentialsOpt = credentialsRepository
					.findByUsername(loginDTO.getUserName());

			if (credentialsOpt.isEmpty()) {
				log.warn("Login failed: username not found, username={}", loginDTO.getUserName());
				response.setSuccess(false);
				response.setMessage("Invalid credentials");
				response.setStatus(HttpStatus.UNAUTHORIZED.value());
				return response;
			}

			DoctorAndStaffLoginCredentials credentials = credentialsOpt.get();

			log.debug("Validating password for username={}", loginDTO.getUserName());
			boolean matches = passwordEncoder.matches(loginDTO.getPassword(), credentials.getPassword());

			if (!matches) {
				log.warn("Login failed: invalid password for username={}", loginDTO.getUserName());
				response.setSuccess(false);
				response.setMessage("Invalid credentials");
				response.setStatus(HttpStatus.UNAUTHORIZED.value());
				return response;
			}

			log.debug("Updating deviceId for doctorId={}", credentials.getStaffId());
			Optional<Doctors> doctorOpt = doctorsRepository.findByDoctorId(credentials.getStaffId());

			doctorOpt.ifPresent(doctor -> {
				doctor.setDeviceId(loginDTO.getDeviceId());
				doctorsRepository.save(doctor);
				log.info("DeviceId updated successfully for doctorId={}", credentials.getStaffId());
			});

			DoctorAndStaffLoginDto dto = new DoctorAndStaffLoginDto();
			dto.setUserName(credentials.getUsername());
			dto.setDeviceId(loginDTO.getDeviceId());
			dto.setStaffId(credentials.getStaffId());
			dto.setHospitalId(credentials.getHospitalId());

			response.setData(dto);
			response.setMessage("Login successful");
			response.setSuccess(true);
			response.setStatus(HttpStatus.OK.value());

			log.info("Login successful for username={}, doctorId={}", credentials.getUsername(),
					credentials.getStaffId());

		} catch (Exception e) {
			log.error("Exception during login for username={}", loginDTO.getUserName(), e);
			response.setSuccess(false);
			response.setMessage("Login failed due to server error");
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return response;
	}

	// -------------------------------DOCTOR CAN CHANGE PASSWORD-------------------------------------------------------------
	@Override
	public Response changePassword(ChangeDoctorPasswordDTO updateDTO) {

		log.info("Change password request received for username={}", updateDTO.getUserName());

		Response responseDTO = new Response();

		if (!updateDTO.getNewPassword().equals(updateDTO.getConfirmPassword())) {
			log.warn("Change password failed: new and confirm password mismatch for username={}",
					updateDTO.getUserName());
			responseDTO.setSuccess(false);
			responseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
			responseDTO.setMessage("New password and confirm password do not match");
			responseDTO.setData(null);
			return responseDTO;
		}

		log.debug("Fetching credentials for username={}", updateDTO.getUserName());
		Optional<DoctorAndStaffLoginCredentials> optionalCredentials = credentialsRepository
				.findByUsername(updateDTO.getUserName());

		if (optionalCredentials.isPresent()) {

			DoctorAndStaffLoginCredentials credentials = optionalCredentials.get();
			log.debug("Credentials found for username={}, staffId={}", credentials.getUsername(),
					credentials.getStaffId());

			log.debug("Validating current password for username={}", updateDTO.getUserName());
			if (passwordEncoder.matches(updateDTO.getCurrentPassword(), credentials.getPassword())) {

				log.info("Current password verified. Updating password for username={}", updateDTO.getUserName());

				credentials.setPassword(passwordEncoder.encode(updateDTO.getNewPassword()));
				credentialsRepository.save(credentials);

				responseDTO.setSuccess(true);
				responseDTO.setStatus(HttpStatus.OK.value());
				responseDTO.setMessage("Password updated successfully");
				responseDTO.setData(null);

				log.info("Password updated successfully for username={}", updateDTO.getUserName());

			} else {
				log.warn("Change password failed: incorrect current password for username={}", updateDTO.getUserName());
				responseDTO.setSuccess(false);
				responseDTO.setStatus(HttpStatus.UNAUTHORIZED.value());
				responseDTO.setMessage("Old password is incorrect");
				responseDTO.setData(null);
			}

		} else {
			log.warn("Change password failed: doctor not found for username={}", updateDTO.getUserName());
			responseDTO.setSuccess(false);
			responseDTO.setStatus(HttpStatus.NOT_FOUND.value());
			responseDTO.setMessage("Doctor not found");
			responseDTO.setData(null);
		}

		return responseDTO;
	}

	// --------------------- Get Doctors by hospitalId and branchId ---------------------------------
	@Override
	public Response getDoctorsByClinicIdAndBranchId(String hospitalId, String branchId) {

		log.info("Get doctors request received for hospitalId={}, branchId={}", hospitalId, branchId);

		Response response = new Response();

		try {
			log.debug("Fetching doctors from DB for hospitalId={} and branchId={}", hospitalId, branchId);
			List<Doctors> doctorList = doctorsRepository.findByHospitalIdAndBranchId(hospitalId, branchId);

			if (!doctorList.isEmpty()) {
				log.info("Found {} doctors for hospitalId={} and branchId={}", doctorList.size(), hospitalId, branchId);

				List<DoctorsDTO> dtos = doctorList.stream()
						.map(doc -> DoctorMapper.mapDoctorEntityToDoctorDTO(doc, s3Service))
						.collect(Collectors.toList());

				response.setSuccess(true);
				response.setData(dtos);
				response.setMessage("Doctors fetched successfully");
				response.setStatus(HttpStatus.OK.value());

			} else {
				log.warn("No doctors found for hospitalId={} and branchId={}", hospitalId, branchId);
				response.setSuccess(true);
				response.setData(Collections.emptyList());
				response.setMessage("No doctors found for hospitalId: " + hospitalId + " and branchId: " + branchId);
				response.setStatus(HttpStatus.OK.value());
			}

		} catch (Exception e) {
			log.error("Exception while fetching doctors for hospitalId={} and branchId={}", hospitalId, branchId, e);
			response.setSuccess(false);
			response.setMessage("An error occurred while fetching doctors for hospitalId: " + hospitalId
					+ " and branchId: " + branchId);
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return response;
	}

	// ----------------- Helper Methods ------------------------

	private String generateStructuredPassword() {
		String[] words = { "doctor" };
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

	// ------------------------------- Doctor Availability Status --------------------------------------------------------------------------------
	@Override
	public Response availabilityStatus(String doctorId, DoctorAvailabilityStatusDTO status) {

		log.info("Update availability status request received for doctorId={}", doctorId);

		Response response = new Response();

		try {
			log.debug("Fetching doctor from DB for doctorId={}", doctorId);
			Optional<Doctors> doctor = doctorsRepository.findByDoctorId(doctorId);

			if (doctor.isPresent()) {
				Doctors getDoctor = doctor.get();
				boolean availability = status.isDoctorAvailabilityStatus();

				log.debug("Updating availability status to {} for doctorId={}", availability, doctorId);

				getDoctor.setDoctorAvailabilityStatus(availability);
				doctorsRepository.save(getDoctor);

				response.setSuccess(true);
				String message = availability ? "Doctor is now available" : "Doctor is now unavailable";
				response.setMessage(message);
				response.setStatus(HttpStatus.OK.value());

				log.info("Availability status updated successfully for doctorId={}, status={}", doctorId, availability);

			} else {
				log.warn("Doctor not found while updating availability status, doctorId={}", doctorId);
				response.setSuccess(false);
				response.setMessage("Doctor Not found with this id: " + doctorId);
				response.setStatus(HttpStatus.NOT_FOUND.value());
			}

		} catch (Exception e) {
			log.error("Exception while updating availability status for doctorId={}", doctorId, e);
			response.setSuccess(false);
			response.setMessage("Error while updating doctor availability status");
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return response;
	}

	// ------------------------------------- Adding Slots ---------------------------------------------------------------------------------------
	@Override
	public Response saveDoctorSlot(String hospitalId, String doctorId, DoctorSlotDTO dto) {
		log.info("Save doctor slot request received hospitalId={}, doctorId={}", hospitalId, doctorId);
		Response response = new Response();

		try {
			if (dto == null || dto.getAvailableSlots() == null || dto.getAvailableSlots().isEmpty()) {
				log.warn("Invalid slot details provided doctorId={}", doctorId);
				throw new IllegalArgumentException("Invalid slot details provided");
			}
			log.debug("Checking doctor existence doctorId={}", doctorId);
			Optional<Doctors> getDoctor = doctorsRepository.findByDoctorId(doctorId);
			if (getDoctor.isEmpty()) {
				log.warn("Doctor not found, doctorId={}", doctorId);
				response.setSuccess(false);
				response.setMessage("Doctor not found with ID: " + doctorId);
				response.setStatus(HttpStatus.NOT_FOUND.value());
				return response;
			}

			log.debug("Checking existing slot for doctorId={} on date={}", doctorId, dto.getDate());
			DoctorSlot existingSlot = slotRepository.findByDoctorIdAndDate(doctorId, dto.getDate());
			DoctorSlot savedSlot;
			if (existingSlot != null) {
				log.info("Existing slot found, doctorId={}, date={}", doctorId, dto.getDate());
				List<DoctorAvailableSlotDTO> currentSlots = existingSlot.getAvailableSlots();

				List<DoctorAvailableSlotDTO> newUniqueSlots = dto.getAvailableSlots().stream()
						.filter(incoming -> currentSlots.stream()
								.noneMatch(existing -> existing.getSlot().equals(incoming.getSlot())))
						.toList();
				log.debug("New unique slots count={}, doctorId={}", newUniqueSlots.size(), doctorId);
				currentSlots.addAll(newUniqueSlots);
				existingSlot.setAvailableSlots(currentSlots);

				savedSlot = slotRepository.save(existingSlot);
				log.info("Slots updated successfully, doctorId={}, totalSlots={}", doctorId, currentSlots.size());
			} else {
				log.info("No existing slot found. Creating new slot, doctorId={}, date={}", doctorId, dto.getDate());
				DoctorSlot newSlot = DoctorSlotMapper.doctorSlotDTOtoEntity(dto);
				newSlot.setDoctorId(doctorId);
				newSlot.setHospitalId(hospitalId);
				savedSlot = slotRepository.save(newSlot);
				log.info("New slot created successfully doctorId={}, slotCount={}", doctorId,
						dto.getAvailableSlots().size());
			}

			response.setSuccess(true);
			response.setData(savedSlot);
			response.setMessage("Slot(s) saved successfully");
			response.setStatus(HttpStatus.CREATED.value());
			log.info("Save doctor slot completed successfully, doctorId={}", doctorId);

		} catch (IllegalArgumentException e) {
			log.error("Validation error while saving slots doctorId={}, message={}", doctorId, e.getMessage());
			response.setSuccess(false);
			response.setMessage("Validation Error: " + e.getMessage());
			response.setStatus(HttpStatus.BAD_REQUEST.value());

		} catch (Exception e) {
			log.error("Exception occurred while saving slots, doctorId={}", doctorId, e);
			response.setSuccess(false);
			response.setMessage("An error occurred while saving slots: " + e.getMessage());
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		log.info("Save doctor slot request completed, status={}", response.getStatus());
		return response;
	}

	// ------------------------- Get Slots by Doctors -------------------------------------------
	@Override
	public Response getDoctorSlots(String hospitalId, String doctorId) {
		log.info("Get doctor slot request received, hospitalId={}, doctorId={}", hospitalId, doctorId);
		Response response = new Response();
		try {
			log.debug("Fetching slots from database, hospitalId={}, doctorId={}", hospitalId, doctorId);
			List<DoctorSlot> slotsFound = slotRepository.findByHospitalIdAndDoctorId(hospitalId, doctorId);

			if (slotsFound == null || slotsFound.isEmpty()) {
				log.warn("No slots found, hospitalId={}, doctorId={}", hospitalId, doctorId);
				response.setSuccess(true);
				response.setData(null);
				response.setMessage("Slots Not Found");
				response.setStatus(HttpStatus.OK.value());
				return response;
			}
			log.info("Slots fetched successfully, count={}, doctorId={}", slotsFound.size(), doctorId);
			response.setSuccess(true);
			response.setData(slotsFound);
			response.setMessage("Slots fetched successfully");
			response.setStatus(HttpStatus.OK.value());

		} catch (Exception e) {
			log.error("Error while fetching slots | hospitalId={} | doctorId={}", hospitalId, doctorId, e);
			response.setSuccess(false);
			response.setData(null);
			response.setMessage("Internal server error occurred");
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return response;
	}

	// --------------------------- Delete slot by time and date using doctorId -----------------------------------------
	@Override
	public Response deleteDoctorSlot(String doctorId, String branchId, String date, String slotToDelete) {
		log.info("Delete doctor slot request received, doctorId={}, branchId={}, date={}, slot={}", doctorId, branchId,
				date, slotToDelete);
		Response response = new Response();
		try {
			log.debug("Fetching doctor slot for deletion, doctorId={}, branchId={}, date={}", doctorId, branchId, date);
			DoctorSlot doctorSlot = slotRepository.findByDoctorIdAndBranchIdAndDate(doctorId, branchId, date);

			if (doctorSlot == null) {
				log.warn("No slot found for given details, doctorId={}, branchId={}, date={}", doctorId, branchId,
						date);
				response.setSuccess(false);
				response.setData(null);
				response.setMessage("No slot found for the doctor in this branch on the given date");
				response.setStatus(HttpStatus.NOT_FOUND.value());
				return response;
			}
			log.debug("Checking slot availability, slot={}, doctorId={}", slotToDelete, doctorId);
			boolean slotExists = doctorSlot.getAvailableSlots().stream()
					.anyMatch(s -> slotToDelete.equals(s.getSlot()) && !s.isSlotbooked());

			if (!slotExists) {
				log.warn("Slot not found or already booked, slot={}, doctorId={}", slotToDelete, doctorId);
				response.setSuccess(false);
				response.setData(null);
				response.setMessage("Slot not found or already booked");
				response.setStatus(HttpStatus.BAD_REQUEST.value());
				return response;
			}

			List<DoctorAvailableSlotDTO> updatedSlots = doctorSlot.getAvailableSlots().stream()
					.filter(s -> !(slotToDelete.equals(s.getSlot()) && !s.isSlotbooked())).collect(Collectors.toList());
			log.debug("Slot removed successfully, remainingSlots={}, doctorId={}", updatedSlots.size(), doctorId);
			doctorSlot.setAvailableSlots(updatedSlots);
			slotRepository.save(doctorSlot);

			DoctorSlotDTO dto = new DoctorSlotDTO();
			dto.setDoctorId(doctorSlot.getDoctorId());
			dto.setHospitalId(doctorSlot.getHospitalId());
			dto.setBranchId(doctorSlot.getBranchId());
			dto.setBranchName(doctorSlot.getBranchName());
			dto.setDate(doctorSlot.getDate());
			dto.setAvailableSlots(updatedSlots);

			response.setSuccess(true);
			response.setData(dto);
			response.setMessage("Slot deleted successfully for the given branch");
			response.setStatus(HttpStatus.OK.value());
			log.info("Slot deleted successfully, doctorId={}, branchId={}, date={}, slot={}", doctorId, branchId, date,
					slotToDelete);
		} catch (Exception e) {
			log.error("Exception occurred while deleting slots, doctorId={}, branchId={}, date={}", doctorId, branchId,
					date, e);
			response.setSuccess(false);
			response.setData(null);
			response.setMessage("Internal server error occurred: " + e.getMessage());
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return response;
	}

	@Override
	public Response deleteDoctorSlot(String doctorId, String date, String slotToDelete) {
		log.info("Delete doctor slot request received, doctorId={}, date={}, slot={}", doctorId, date, slotToDelete);
		Response response = new Response();
		try {
			log.debug("Fetching doctor slot | doctorId={} | date={}", doctorId, date);
			DoctorSlot doctorSlot = slotRepository.findByDoctorIdAndDate(doctorId, date);

			if (doctorSlot == null) {
				log.warn("No slot found | doctorId={} | date={}", doctorId, date);
				response.setSuccess(false);
				response.setData(null);
				response.setMessage("No slot found for the doctor on the given date");
				response.setStatus(HttpStatus.NOT_FOUND.value());
				return response;
			}
			log.debug("Validating slot availability | slot={} | doctorId={}", slotToDelete, doctorId);
			boolean slotExists = doctorSlot.getAvailableSlots().stream()
					.anyMatch(s -> slotToDelete.equals(s.getSlot()) && !s.isSlotbooked());

			if (!slotExists) {
				log.warn("Slot not found or already booked | slot={} | doctorId={}", slotToDelete, doctorId);
				response.setSuccess(false);
				response.setData(null);
				response.setMessage("Slot not found or already booked");
				response.setStatus(HttpStatus.BAD_REQUEST.value());
				return response;
			}

			List<DoctorAvailableSlotDTO> updatedSlots = doctorSlot.getAvailableSlots().stream()
					.filter(s -> !(slotToDelete.equals(s.getSlot()) && !s.isSlotbooked())).collect(Collectors.toList());
			log.debug("Slot removed | remainingSlots={} | doctorId={}", updatedSlots.size(), doctorId);

			doctorSlot.setAvailableSlots(updatedSlots);
			slotRepository.save(doctorSlot);

			DoctorSlotDTO dto = new DoctorSlotDTO();
			dto.setDoctorId(doctorSlot.getDoctorId());
			dto.setHospitalId(doctorSlot.getHospitalId());
			dto.setBranchId(doctorSlot.getBranchId());
			dto.setBranchName(doctorSlot.getBranchName());
			dto.setDate(doctorSlot.getDate());
			dto.setAvailableSlots(updatedSlots);

			response.setSuccess(true);
			response.setData(dto);
			response.setMessage("Slot deleted successfully");
			response.setStatus(HttpStatus.OK.value());
			log.info("Slot deleted successfully | doctorId={} | date={} | slot={}", doctorId, date, slotToDelete);
		} catch (Exception e) {
			log.error("Error while deleting slot | doctorId={} | date={}", doctorId, date, e);
			response.setSuccess(false);
			response.setData(null);
			response.setMessage("Internal server error occurred: " + e.getMessage());
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return response;
	}

	// ----------------------------- Update Slot ---------------------------------------------------------------------------
	@Override
	public Response updateDoctorSlot(String doctorId, String date, String oldSlot, String newSlot) {
		log.info("Update doctor slot request received, doctorId={}, date={}, oldSlot={}, newSlot={}", doctorId, date,
				oldSlot, newSlot);
		try {
			log.debug("Fetching doctor slot details from database");
			DoctorSlot doctorSlot = slotRepository.findByDoctorIdAndDate(doctorId, date);
			if (doctorSlot == null) {
				log.warn("No slot found for doctorId={} on date={}", doctorId, date);
				return failure("No slot found for the doctor on the given date", HttpStatus.NOT_FOUND);
			}
			List<DoctorAvailableSlotDTO> slotsList = doctorSlot.getAvailableSlots();
			log.debug("Total available slots found: {}", slotsList.size());
			boolean slotUpdated = false;

			for (DoctorAvailableSlotDTO slot : slotsList) {
				log.debug("Checking slot={}, booked={}", slot.getSlot(), slot.isSlotbooked());
				if (slot.getSlot().equals(oldSlot) && !slot.isSlotbooked()) {
					slot.setSlot(newSlot);
					slotUpdated = true;
					log.info("Slot updated successfully, oldSlot={} -> newSlot={}", oldSlot, newSlot);
					break;
				}
			}

			if (!slotUpdated) {
				log.warn("Slot update failed, oldSlot={} not found or already booked", oldSlot);
				return failure("Old slot not found or already booked", HttpStatus.BAD_REQUEST);
			}

			doctorSlot.setAvailableSlots(slotsList);
			slotRepository.save(doctorSlot);
			log.info("Doctor slot updated successfully, doctorId={}, date={}", doctorId, date);
			Response response = new Response();
			response.setSuccess(true);
			response.setData(doctorSlot);
			response.setMessage("Slot updated successfully");
			response.setStatus(HttpStatus.OK.value());
			return response;

		} catch (Exception e) {
			log.error("Exception occurred while updating doctor slot, doctorId={}, date={}", doctorId, date, e);
			return failure("An error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public Response deleteDoctorSlotbyDate(String doctorId, String date) {
		log.info("Delete doctor slots by date request received, doctorId={}, date={}", doctorId, date);
		try {
			DoctorSlot doctorSlot = slotRepository.findByDoctorIdAndDate(doctorId, date);

			if (doctorSlot == null) {
				log.warn("No slots found to delete | doctorId={}, date={}", doctorId, date);
				return failure("No slots found for doctor on this date", HttpStatus.NOT_FOUND);
			}

			slotRepository.delete(doctorSlot);
			log.info("Slots deleted successfully | doctorId={}, date={}", doctorId, date);
			Response response = new Response();
			response.setSuccess(true);
			response.setData(null);
			response.setMessage("All slots deleted successfully for date " + date);
			response.setStatus(HttpStatus.OK.value());
			return response;
		} catch (Exception e) {
			log.error("Exception occurred while deleting slots | doctorId={}, date={}", doctorId, date, e);
			return failure("An error occurred while deleting slots", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public Response deleteDoctorSlotbyDate(String doctorId, String branchId, String date) {
		log.info("Delete doctor slot by branch and date request received | doctorId={}, branchId={}, date={}", doctorId,
				branchId, date);
		Response response = new Response();

		try {
			log.debug("Fetching doctor slots from database | doctorId={}, branchId={}, date={}", doctorId, branchId,
					date);
			DoctorSlot doctorSlot = slotRepository.findByDoctorIdAndBranchIdAndDate(doctorId, branchId, date);

			if (doctorSlot == null) {
				log.warn("No slots found for doctorId={}, branchId={}, date={}", doctorId, branchId, date);
				response.setSuccess(false);
				response.setData(null);
				response.setMessage("No slots found for the doctor in this branch on the given date");
				response.setStatus(HttpStatus.NOT_FOUND.value());
				return response;
			}

			List<DoctorAvailableSlotDTO> allSlots = doctorSlot.getAvailableSlots();
			log.debug("Total slots found: {}", allSlots.size());
			List<DoctorAvailableSlotDTO> bookedSlots = allSlots.stream().filter(DoctorAvailableSlotDTO::isSlotbooked)
					.collect(Collectors.toList());
			log.debug("Total booked slots count={}, Unbooked slots count={}", bookedSlots.size(),
					allSlots.size() - bookedSlots.size());

			if (bookedSlots.isEmpty()) {
				log.info("No booked slots found, deleting entire document | doctorId={}, branchId={}, date={}",
						doctorId, branchId, date);
				slotRepository.delete(doctorSlot);
				response.setSuccess(true);
				response.setData(null);
				response.setMessage("All unbooked slots deleted successfully (no booked slots found).");
				response.setStatus(HttpStatus.OK.value());
				return response;
			}

			log.info("Deleting unbooked slots and retaining booked slots | doctorId={}, branchId={}, date={}", doctorId,
					branchId, date);
			doctorSlot.setAvailableSlots(bookedSlots);
			slotRepository.save(doctorSlot);

			response.setSuccess(true);
			response.setData(bookedSlots);
			response.setMessage("Unbooked slots deleted successfully, booked slots retained.");
			response.setStatus(HttpStatus.OK.value());
		} catch (Exception e) {
			log.error("Exception occurred while deleting doctor slot by branch and date, exception={}", e.getMessage(),
					e);
			response.setSuccess(false);
			response.setData(null);
			response.setMessage("Internal server error occurred: " + e.getMessage());
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		log.info("Delete doctor slot by branch and date request completed | status={}", response.getStatus());
		return response;
	}

	public boolean updateSlot(String doctorId, String branchId, String date, String time) {
		log.info("Update slot request | doctorId={}, branchId={}, date={}, time={}", doctorId, branchId, date, time);
		if (doctorId == null || date == null || time == null) {
			log.warn("Invalid input received while updating slot");
			return false;
		}
		try {
			log.debug("Fetching doctor slots from DB");
			DoctorSlot doctorSlots = slotRepository.findByDoctorIdAndDateAndBranchId(doctorId, date, branchId);

			if (doctorSlots == null || doctorSlots.getAvailableSlots() == null
					|| doctorSlots.getAvailableSlots().isEmpty()) {
				log.warn("No slots found for doctorId={}, branchId={}, date={}", doctorId, branchId, date);
				return false;
			}
			Optional<DoctorAvailableSlotDTO> matchingSlotOpt = doctorSlots.getAvailableSlots().stream()
					.filter(slot -> time.equalsIgnoreCase(slot.getSlot())).findFirst();
			if (matchingSlotOpt.isPresent()) {
				DoctorAvailableSlotDTO matchingSlot = matchingSlotOpt.get();

				if (matchingSlot.isSlotbooked()) {
					log.warn("Slot already booked | doctorId={}, date={}, time={}", doctorId, date, time);
					return false;
				}
				matchingSlot.setSlotbooked(true);
				slotRepository.save(doctorSlots);
				log.info("Slot successfully booked | doctorId={}, branchId={}, date={}, time={}", doctorId, branchId,
						date, time);
				return true;
			} else {
				log.warn("Requested slot not found | doctorId={}, date={}, time={}", doctorId, date, time);
				return false;
			}
		} catch (Exception e) {
			log.error("Exception while booking slot | doctorId={}, branchId={}, date={}, time={}", doctorId, branchId,
					date, time, e);
			return false;
		}
	}

    public boolean makingFalseDoctorSlot(String doctorId, String branchId, String date, String time) {
        log.info("Unbook slot request | doctorId={}, branchId={}, date={}, time={}", doctorId, branchId, date, time);

        // ✅ Validate inputs
        if (doctorId == null || branchId == null || date == null || time == null) {
            log.warn("Invalid input received while unbooking slot | doctorId={}, branchId={}, date={}, time={}", doctorId, branchId, date, time);
            return false;
        }

        try {
            log.debug("Fetching doctor slots from DB");
            DoctorSlot doctorSlots = slotRepository.findByDoctorIdAndDateAndBranchId(doctorId, date, branchId);

            // ✅ Handle missing slots
            if (doctorSlots == null || CollectionUtils.isEmpty(doctorSlots.getAvailableSlots())) {
                log.warn("No slots found to unbook | doctorId={}, branchId={}, date={}", doctorId, branchId, date);
                return false;
            }

            Optional<DoctorAvailableSlotDTO> matchingSlot = doctorSlots.getAvailableSlots().stream()
                    .filter(slot -> time.equalsIgnoreCase(slot.getSlot()))
                    .findFirst();

            if (matchingSlot.isPresent()) {
                DoctorAvailableSlotDTO slot = matchingSlot.get();
                if (slot.isSlotbooked()) {
                    slot.setSlotbooked(false);
                    slotRepository.save(doctorSlots);
                    log.info("Slot successfully unbooked | doctorId={}, branchId={}, date={}, time={}", doctorId, branchId, date, time);
                    return true;
                } else {
                    log.info("Slot was already unbooked | doctorId={}, branchId={}, date={}, time={}", doctorId, branchId, date, time);
                    return false;
                }}
            log.warn("Requested slot not found for unbooking | doctorId={}, branchId={}, date={}, time={}", doctorId, branchId, date, time);
            return false;

        } catch (Exception e) {
            log.error("Exception occurred while unbooking slot | doctorId={}, branchId={}, date={}, time={}", doctorId, branchId, date, time, e);
            return false;
        }
    }


	@Override
	public Response saveDoctorSlot(String hospitalId, String branchId, String doctorId, DoctorSlotDTO dto) {
		log.info("Save doctor slot called, hospitalId={}, branchId={}, doctorId={}", hospitalId, branchId, doctorId);
		Response response = new Response();

		try {
			if (dto == null || dto.getAvailableSlots() == null || dto.getAvailableSlots().isEmpty()) {
				log.warn("Invalid slot details, doctorId={}, dto={}", doctorId, dto);
				throw new IllegalArgumentException("Invalid slot details provided");
			}

			Optional<Doctors> getDoctor = doctorsRepository.findByDoctorId(doctorId);
			if (getDoctor.isEmpty()) {
				log.warn("Doctor not found with ID: {}", doctorId);
				response.setSuccess(false);
				response.setMessage("Doctor not found with ID: " + doctorId);
				response.setStatus(HttpStatus.NOT_FOUND.value());
				return response;
			}
			log.debug("Doctor found | doctorId={}", doctorId);

			List<DoctorSlot> doctorSlotsOnDate = slotRepository.findAllByDoctorIdAndDate(doctorId, dto.getDate());
			log.debug("Existing slots found for date {}: {}", dto.getDate(), doctorSlotsOnDate.size());

			List<DoctorAvailableSlotDTO> slotsWithAvailability = dto.getAvailableSlots().stream().map(incomingSlot -> {
				Optional<DoctorSlot> conflictingSlot = doctorSlotsOnDate.stream().filter(slot -> slot
						.getAvailableSlots().stream().anyMatch(s -> s.getSlot().equals(incomingSlot.getSlot())))
						.findFirst();

				if (conflictingSlot.isPresent()) {
					String existingBranchName = conflictingSlot.get().getBranchName();
					incomingSlot.setAvailable(false);
					incomingSlot.setReason("Already exists in " + existingBranchName + " Branch");
					log.info("Slot conflict | doctorId={}, slot={}, branch={}", doctorId, incomingSlot.getSlot(),
							existingBranchName);
				} else {
					incomingSlot.setAvailable(true);
					incomingSlot.setReason(null);
					log.debug("Slot available | doctorId={}, slot={}", doctorId, incomingSlot.getSlot());
				}

				return incomingSlot;
			}).sorted(Comparator.comparing(slot -> LocalTime.parse(normalizeTime(slot.getSlot()), SLOT_TIME_FORMATTER)))
					.toList();

			List<DoctorAvailableSlotDTO> slotsToSave = slotsWithAvailability.stream()
					.filter(DoctorAvailableSlotDTO::isAvailable).toList();
			log.info("Slots requested={}, slots eligible for save={}", slotsWithAvailability.size(),
					slotsToSave.size());

			DoctorSlot savedSlot = null;

			if (!slotsToSave.isEmpty()) {
				DoctorSlot existingSlot = slotRepository.findByDoctorIdAndBranchIdAndDate(doctorId, branchId,
						dto.getDate());
				if (existingSlot != null) {
					log.info("Updating existing slots | doctorId={}, branchId={}, date={}", doctorId, branchId,
							dto.getDate());
					List<DoctorAvailableSlotDTO> currentSlots = existingSlot.getAvailableSlots();

					List<DoctorAvailableSlotDTO> newUniqueSlots = slotsToSave.stream().filter(incoming -> currentSlots
							.stream().noneMatch(existing -> existing.getSlot().equals(incoming.getSlot()))).toList();
					log.debug("New unique slots count={}", newUniqueSlots.size());
					currentSlots.addAll(newUniqueSlots);
					existingSlot.setAvailableSlots(currentSlots);
					savedSlot = slotRepository.save(existingSlot);
				} else {
					log.info("Creating new slot entry | doctorId={}, branchId={}, date={}", doctorId, branchId,
							dto.getDate());
					DoctorSlot newSlot = DoctorSlotMapper.doctorSlotDTOtoEntity(dto);

					ResponseEntity<Response> branchResponse = adminServiceClient.getBranchById(branchId);
					Branch branchDetails = objectMapper.convertValue(branchResponse.getBody().getData(), Branch.class);

					newSlot.setDoctorId(doctorId);
					newSlot.setHospitalId(hospitalId);
					newSlot.setBranchId(branchId);
					if (branchDetails != null) {
						newSlot.setBranchName(branchDetails.getBranchName());
					}
					newSlot.setAvailableSlots(slotsToSave);
					savedSlot = slotRepository.save(newSlot);
				}
				log.info("Slots saved successfully | slotId={}", savedSlot != null ? savedSlot.getId() : null);
			}

			response.setSuccess(true);
			response.setData(slotsWithAvailability);
			response.setMessage("Slots processed successfully. Unavailable slots are flagged with branch info.");
			response.setStatus(HttpStatus.OK.value());

		} catch (IllegalArgumentException e) {
			log.error("Validation error | doctorId={} | message={}", doctorId, e.getMessage());
			response.setSuccess(false);
			response.setMessage("Validation Error: " + e.getMessage());
			response.setStatus(HttpStatus.BAD_REQUEST.value());

		} catch (Exception e) {
			log.error("Exception while saving slots | doctorId={}, branchId={}, error={}", doctorId, branchId,
					e.getMessage(), e);
			response.setSuccess(false);
			response.setMessage("An error occurred while saving slots: " + e.getMessage());
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		log.info("SaveDoctorSlot completed | doctorId={}, branchId={}", doctorId, branchId);
		return response;
	}
	@Override
	public Response generateDoctorSlots(String doctorId, String branchId, String date, int intervalMinutes,
	        String openingTime, String closingTime) {

	    Response response = new Response();

	    try {
	        openingTime = normalizeTime(URLDecoder.decode(openingTime, StandardCharsets.UTF_8));
	        closingTime = normalizeTime(URLDecoder.decode(closingTime, StandardCharsets.UTF_8));

	        List<DoctorAvailableSlotDTO> generatedSlots = generateSlots(openingTime, closingTime, intervalMinutes, date,
	                CLINIC_ZONE_ID);

	        List<DoctorSlot> doctorSlotsOnDate = slotRepository.findAllByDoctorIdAndDate(doctorId, date);

	        List<DoctorAvailableSlotDTO> existingSlots = doctorSlotsOnDate.stream()
	                .flatMap(ds -> ds.getAvailableSlots().stream().map(s -> {
	                    DoctorAvailableSlotDTO d = new DoctorAvailableSlotDTO();
	                    d.setSlot(normalizeTime(s.getSlot()));
	                    d.setAvailable(s.isAvailable());
	                    d.setReason(ds.getBranchName());
	                    return d;
	                })).toList();

	        List<DoctorAvailableSlotDTO> finalSlots = new ArrayList<>();

	        for (DoctorAvailableSlotDTO slot : generatedSlots) {
	            boolean available = slot.isAvailable();
	            String reason = slot.getReason();

	            if (available) {
	                DoctorAvailableSlotDTO conflictSlot = existingSlots.stream()
	                        .filter(existing -> isOverlapping(slot.getSlot(), intervalMinutes, List.of(existing), 30))
	                        .findFirst().orElse(null);

	                if (conflictSlot != null) {
	                    available = false;
	                    reason = "Already exists in " + conflictSlot.getReason() + " Branch";
	                }
	            }

	            slot.setAvailable(available);
	            slot.setReason(reason);
	            finalSlots.add(slot);
	        }

	        long unavailableCount = finalSlots.stream().filter(s -> !s.isAvailable()).count();

	        response.setSuccess(true);
	        response.setData(finalSlots);
	        response.setMessage("Slots generated successfully. " + unavailableCount
	                + " slot(s) are unavailable due to branch conflicts.");
	        response.setStatus(200);

	    } catch (Exception e) {
	        log.error("Error generating slots | doctorId={}, branchId={}, date={}", doctorId, branchId, date, e);
	        response.setSuccess(false);
	        response.setMessage("Error generating slots: " + e.getMessage());
	        response.setStatus(500);
	    }

	    return response;
	}

	// ---------------- Helper Methods ----------------

	private List<DoctorAvailableSlotDTO> generateSlots(String openingTime, String closingTime, int intervalMinutes,
	        String date, ZoneId zoneId) {

	    LocalTime start;
	    LocalTime end;

	    try {
	        start = LocalTime.parse(openingTime.trim().toUpperCase(), SLOT_TIME_FORMATTER);
	        end = LocalTime.parse(closingTime.trim().toUpperCase(), SLOT_TIME_FORMATTER);
	    } catch (DateTimeParseException e) {
	        throw new RuntimeException("Failed to parse time: " + e.getParsedString(), e);
	    }

	    List<DoctorAvailableSlotDTO> generatedSlots = new ArrayList<>();

	    while (!start.isAfter(end.minusMinutes(intervalMinutes))) {

	        DoctorAvailableSlotDTO slot = new DoctorAvailableSlotDTO();
	        slot.setSlot(start.format(SLOT_TIME_FORMATTER));
	        slot.setSlotbooked(false);
	        slot.setAvailable(true);
	        slot.setReason(null);

	        generatedSlots.add(slot);
	        start = start.plusMinutes(intervalMinutes);
	    }

	    return generatedSlots;
	}

	private String normalizeTime(String time) {
	    time = time.trim().replaceAll("\\s+", " ").toUpperCase();
	    time = time.replaceAll("(?<=\\d)(AM|PM)", " $1");
	    return time;
	}

	private boolean isOverlapping(String newSlot, int newInterval, List<DoctorAvailableSlotDTO> existingSlots,
	        int existingInterval) {

	    newSlot = normalizeTime(newSlot);
	    LocalTime newStart = LocalTime.parse(newSlot, SLOT_TIME_FORMATTER);
	    LocalTime newEnd = newStart.plusMinutes(newInterval);

	    for (DoctorAvailableSlotDTO existing : existingSlots) {
	        if (existing.getSlot() == null)
	            continue;

	        String existingSlotStr = normalizeTime(existing.getSlot());
	        LocalTime existStart = LocalTime.parse(existingSlotStr, SLOT_TIME_FORMATTER);

	        int effectiveInterval = existingInterval > 0 ? existingInterval : newInterval;
	        LocalTime existEnd = existStart.plusMinutes(effectiveInterval);

	        boolean overlaps = newStart.isBefore(existEnd) && newEnd.isAfter(existStart);
	        if (overlaps) {
	            return true;
	        }
	    }
	    return false;
	}
//	@Override
//	public Response generateDoctorSlots(String doctorId, String branchId, String date, int intervalMinutes,
//			String openingTime, String closingTime) {
//
//		Response response = new Response();
//
//		try {
//			openingTime = normalizeTime(URLDecoder.decode(openingTime, StandardCharsets.UTF_8));
//			closingTime = normalizeTime(URLDecoder.decode(closingTime, StandardCharsets.UTF_8));
//
//			ZonedDateTime nowZoned = ZonedDateTime.now(CLINIC_ZONE_ID);
//			LocalDate today = nowZoned.toLocalDate();
//			LocalTime now = nowZoned.toLocalTime();
//
//			List<DoctorAvailableSlotDTO> generatedSlots = generateSlots(openingTime, closingTime, intervalMinutes, date,
//					CLINIC_ZONE_ID);
//
//			List<DoctorSlot> doctorSlotsOnDate = slotRepository.findAllByDoctorIdAndDate(doctorId, date);
//
//			List<DoctorAvailableSlotDTO> existingSlots = doctorSlotsOnDate.stream()
//					.flatMap(ds -> ds.getAvailableSlots().stream().map(s -> {
//						DoctorAvailableSlotDTO d = new DoctorAvailableSlotDTO();
//						d.setSlot(normalizeTime(s.getSlot()));
//						d.setAvailable(s.isAvailable());
//						d.setReason(ds.getBranchName());
//						return d;
//					})).toList();
//
//			LocalDate slotDate = LocalDate.parse(date);
//			List<DoctorAvailableSlotDTO> finalSlots = new ArrayList<>();
//
//			for (DoctorAvailableSlotDTO slot : generatedSlots) {
//				LocalTime slotTime = LocalTime.parse(normalizeTime(slot.getSlot()), SLOT_TIME_FORMATTER);
//				boolean available = slot.isAvailable();
//				String reason = slot.getReason();
//
//				if (available) {
//					DoctorAvailableSlotDTO conflictSlot = existingSlots.stream()
//							.filter(existing -> isOverlapping(slot.getSlot(), intervalMinutes, List.of(existing), 30))
//							.findFirst().orElse(null);
//
//					if (conflictSlot != null) {
//						available = false;
//						reason = "Already exists in " + conflictSlot.getReason() + " Branch";
//					}
//				}
//
//				if (slotDate.isBefore(today)) {
//					available = false;
//					reason = "Date already passed";
//				} else if (slotDate.equals(today) && slotTime.isBefore(now)) {
//					available = false;
//					reason = "Time already passed";
//				}
//
//				slot.setAvailable(available);
//				slot.setReason(reason);
//				finalSlots.add(slot);
//			}
//
//			long unavailableCount = finalSlots.stream().filter(s -> !s.isAvailable()).count();
//
//			response.setSuccess(true);
//			response.setData(finalSlots);
//			response.setMessage("Slots generated successfully. " + unavailableCount
//					+ " slot(s) are unavailable due to branch conflicts or past time.");
//			response.setStatus(200);
//
//		} catch (Exception e) {
//			log.error("Error generating slots | doctorId={}, branchId={}, date={}", doctorId, branchId, date, e);
//			response.setSuccess(false);
//			response.setMessage("Error generating slots: " + e.getMessage());
//			response.setStatus(500);
//		}
//
//		return response;
//	}
//
//	// ---------------- Helper Methods ----------------
//
//	private List<DoctorAvailableSlotDTO> generateSlots(String openingTime, String closingTime, int intervalMinutes,
//			String date, ZoneId zoneId) {
//
//		LocalTime start;
//		LocalTime end;
//
//		try {
//			start = LocalTime.parse(openingTime.trim().toUpperCase(), SLOT_TIME_FORMATTER);
//			end = LocalTime.parse(closingTime.trim().toUpperCase(), SLOT_TIME_FORMATTER);
//		} catch (DateTimeParseException e) {
//			throw new RuntimeException("Failed to parse time: " + e.getParsedString(), e);
//		}
//
//		List<DoctorAvailableSlotDTO> generatedSlots = new ArrayList<>();
//		LocalDate today = ZonedDateTime.now(zoneId).toLocalDate();
//		LocalTime now = ZonedDateTime.now(zoneId).toLocalTime();
//		LocalDate slotDate = LocalDate.parse(date);
//
//		if (slotDate.isBefore(today)) {
//			return generatedSlots;
//		}
//
//		while (!start.isAfter(end.minusMinutes(intervalMinutes))) {
//
//			if (slotDate.equals(today) && start.isBefore(now)) {
//				start = start.plusMinutes(intervalMinutes);
//				continue;
//			}
//
//			DoctorAvailableSlotDTO slot = new DoctorAvailableSlotDTO();
//			slot.setSlot(start.format(SLOT_TIME_FORMATTER));
//			slot.setSlotbooked(false);
//			slot.setAvailable(true);
//			slot.setReason(null);
//
//			generatedSlots.add(slot);
//			start = start.plusMinutes(intervalMinutes);
//		}
//
//		return generatedSlots;
//	}
//
//	private String normalizeTime(String time) {
//		time = time.trim().replaceAll("\\s+", " ").toUpperCase();
//		time = time.replaceAll("(?<=\\d)(AM|PM)", " $1");
//		return time;
//	}
//
//	private boolean isOverlapping(String newSlot, int newInterval, List<DoctorAvailableSlotDTO> existingSlots,
//			int existingInterval) {
//
//		newSlot = normalizeTime(newSlot);
//		LocalTime newStart = LocalTime.parse(newSlot, SLOT_TIME_FORMATTER);
//		LocalTime newEnd = newStart.plusMinutes(newInterval);
//
//		for (DoctorAvailableSlotDTO existing : existingSlots) {
//			if (existing.getSlot() == null)
//				continue;
//
//			String existingSlotStr = normalizeTime(existing.getSlot());
//			LocalTime existStart = LocalTime.parse(existingSlotStr, SLOT_TIME_FORMATTER);
//
//			int effectiveInterval = existingInterval > 0 ? existingInterval : newInterval;
//			LocalTime existEnd = existStart.plusMinutes(effectiveInterval);
//
//			boolean overlaps = newStart.isBefore(existEnd) && newEnd.isAfter(existStart);
//			if (overlaps) {
//				return true;
//			}
//		}
//		return false;
//	}
//
	@Override
	public Response getDoctorSlots(String hospitalId, String branchId, String doctorId) {
		List<DoctorSlot> slotsFound = slotRepository.findByHospitalIdAndBranchIdAndDoctorId(hospitalId, branchId,
				doctorId);

		Response response = new Response();
		if (slotsFound == null || slotsFound.isEmpty()) {
			response.setSuccess(true);
			response.setData(null);
			response.setMessage("Slots Not Found");
			response.setStatus(HttpStatus.OK.value());
			return response;
		}

		LocalDate today = ZonedDateTime.now(CLINIC_ZONE_ID).toLocalDate();
		LocalTime now = ZonedDateTime.now(CLINIC_ZONE_ID).toLocalTime();

		for (DoctorSlot slotEntity : slotsFound) {
			LocalDate slotDate = LocalDate.parse(slotEntity.getDate());
			for (DoctorAvailableSlotDTO slot : slotEntity.getAvailableSlots()) {
				LocalTime slotTime = LocalTime.parse(normalizeTime(slot.getSlot()), SLOT_TIME_FORMATTER);

				if (slot.isSlotbooked()) {
					slot.setAvailable(false);
					slot.setReason("Already booked");
				} else if (slotDate.isBefore(today)) {
					slot.setAvailable(false);
					slot.setReason("Date already passed");
				} else if (slotDate.equals(today) && slotTime.isBefore(now)) {
					slot.setAvailable(false);
					slot.setReason("Time already passed");
				} else {
					slot.setAvailable(true);
					slot.setReason(null);
				}
			}
		}

		response.setSuccess(true);
		response.setData(slotsFound);
		response.setMessage("Slots fetched successfully");
		response.setStatus(HttpStatus.OK.value());
		return response;
	}

	// ----------------------------- slots end ------------------------------------------------------------------

	// ------------------- Simplified Mapper ----------------------------------------------
	private ClinicWithDoctorsDTO mapToClinicWithDoctorsDTO(ClinicDTO clinic, List<Doctors> doctorList) {
		ClinicWithDoctorsDTO dto = objectMapper.convertValue(clinic, ClinicWithDoctorsDTO.class);

		List<DoctorsDTO> doctorDTOs = doctorList.stream()
				.map(doc -> DoctorMapper.mapDoctorEntityToDoctorDTO(doc, s3Service)).collect(Collectors.toList());

		dto.setDoctors(doctorDTOs);
		return dto;
	}

	// NOTIFICATION OF DOCTOR
	public ResponseEntity<?> notificationToClinic(String hospitalId) {
		try {
			return notificationFeign.sendNotificationToClinic(hospitalId);
		} catch (FeignException e) {
			ResBody<List<String>> res = new ResBody<>(ExtractFeignMessage.clearMessage(e), e.status(), null);
			return ResponseEntity.status(e.status()).body(res);
		}
	}

	// ----------------------------- GET CLINICS AND DOCTORS BY RECOMMENDATION == TRUE ---------------------------------
	@Override
	public Response getRecommendedClinicsAndDoctors() {
		Response finalResponse = new Response();

		try {
			ResponseEntity<Response> responseEntity = adminServiceClient.getHospitalUsingRecommendentaion();
			Response responseBody = responseEntity.getBody();

			List<ClinicWithDoctorsDTO> result = new ArrayList<>();

			if (responseBody != null && responseBody.isSuccess()) {
				Object rawData = responseBody.getData();

				List<ClinicDTO> clinics = objectMapper.convertValue(rawData, new TypeReference<List<ClinicDTO>>() {
				});

				for (ClinicDTO clinicDTO : clinics) {
					ClinicWithDoctorsDTO clinic = objectMapper.convertValue(clinicDTO, ClinicWithDoctorsDTO.class);

					List<Doctors> doctorEntities = doctorsRepository.findByHospitalId(clinic.getHospitalId());

					List<DoctorsDTO> doctors = doctorEntities.stream().map(doc -> {
						DoctorsDTO d = DoctorMapper.mapDoctorEntityToDoctorDTO(doc, s3Service);

						if (doc.getDoctorFees() != null) {
							d.setDoctorFees(DoctorMapper.mapDoctorFeeEntityToDTO(doc.getDoctorFees()));
						}

						if (doc.getDoctorSignature() != null && !doc.getDoctorSignature().isBlank())
							d.setDoctorSignature(doc.getDoctorSignature());

						return d;
					}).collect(Collectors.toList());

					clinic.setDoctors(doctors);
					result.add(clinic);
				}
			}

			finalResponse.setSuccess(true);
			finalResponse.setStatus(HttpStatus.OK.value());
			finalResponse.setMessage("Recommended clinics with doctors retrieved successfully.");
			finalResponse.setData(result);

		} catch (Exception e) {
			log.error("Error fetching recommended clinics and doctors: {}", e.getMessage(), e);
			finalResponse.setSuccess(false);
			finalResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			finalResponse.setMessage("Internal error: " + e.getMessage());
		}

		return finalResponse;
	}

	@Override
	public Response getRecommendedClinicsAndOneDoctors(List<String> keyPointsFromUser) {
		Logger localLog = LoggerFactory.getLogger(getClass());

		ResponseEntity<Response> responseEntity = adminServiceClient.getHospitalUsingRecommendentaion();
		Response responseBody = responseEntity.getBody();

		List<ClinicWithDoctorsDTO> result = new ArrayList<>();
		boolean anyDoctorMatched = false;

		if (responseBody != null && responseBody.isSuccess()) {
			Object rawData = responseBody.getData();
			localLog.info("Raw clinic data from Feign: {}", rawData);

			List<ClinicWithDoctorsDTO> clinics = objectMapper.convertValue(rawData,
					new TypeReference<List<ClinicWithDoctorsDTO>>() {
					});

			localLog.info("Converted clinic list size: {}", clinics.size());

			for (ClinicWithDoctorsDTO clinic : clinics) {
				localLog.info("Processing clinic: {} | ID: {}", clinic.getName(), clinic.getHospitalId());

				if (clinic.getHospitalId() == null) {
					localLog.warn("Clinic missing hospitalId, skipping...");
					continue;
				}

				List<Doctors> doctorEntities = doctorsRepository.findByHospitalId(clinic.getHospitalId());
				localLog.info("Doctors found for clinic {}: {}", clinic.getHospitalId(), doctorEntities.size());

				List<DoctorsDTO> matchedDoctors = new ArrayList<>();

				for (Doctors doctor : doctorEntities) {
					DoctorsDTO dto = DoctorMapper.mapDoctorEntityToDoctorDTO(doctor, s3Service);
					boolean relevant = isDoctorRelevant(dto, keyPointsFromUser);
					localLog.info("Doctor: {} | Relevant: {}", dto.getDoctorName(), relevant);

					if (relevant) {
						matchedDoctors.add(dto);
						anyDoctorMatched = true;
					}
				}

				clinic.setDoctors(matchedDoctors);
				result.add(clinic);
			}

			if (!anyDoctorMatched) {
				localLog.info("No doctor matched. Returning all clinics and doctors.");
				result.clear();

				for (ClinicWithDoctorsDTO clinic : clinics) {
					if (clinic.getHospitalId() == null)
						continue;

					List<Doctors> doctorEntities = doctorsRepository.findByHospitalId(clinic.getHospitalId());
					List<DoctorsDTO> allDoctors = doctorEntities.stream()
							.map(doc -> DoctorMapper.mapDoctorEntityToDoctorDTO(doc, s3Service)).toList();
					clinic.setDoctors(allDoctors);
					result.add(clinic);
				}
			}

		} else {
			localLog.warn("Feign response unsuccessful or null");
		}

		return Response.builder().success(true).status(HttpStatus.OK.value()).data(result)
				.message("Matched clinics and doctors").build();
	}

	private boolean isDoctorRelevant(DoctorsDTO doctor, List<String> keyPoints) {
		if (keyPoints == null || keyPoints.isEmpty())
			return false;

		for (String key : keyPoints) {
			String lowerKey = key.toLowerCase();
			if (doctor.getSpecialization() != null && doctor.getSpecialization().toLowerCase().contains(lowerKey)) {
				log.debug("Matched specialization: {} with keyword: {}", doctor.getSpecialization(), key);
				return true;
			}
		}
		return true;
	}

	// ---------------- get All doctors with respective their clinics --------------------------
	@Override
	public Response getAllDoctorsWithRespectiveClinic() {
		Response response = new Response();

		try {
			ResponseEntity<Response> clinicsResponse = adminServiceClient.firstRecommendedTureClincs();
			Object clinicObj = clinicsResponse.getBody().getData();

			List<ClinicDTO> clinics = objectMapper.convertValue(clinicObj, new TypeReference<List<ClinicDTO>>() {
			});

			List<ClinicWithDoctorsDTO> clinicsWithDoctors = clinics.stream().map(clinicDTO -> {
				List<Doctors> doctorsDbData = doctorsRepository.findByHospitalId(clinicDTO.getHospitalId());

				List<DoctorsDTO> doctorDTOs = doctorsDbData.stream()
						.map(doc -> DoctorMapper.mapDoctorEntityToDoctorDTO(doc, s3Service))
						.collect(Collectors.toList());

				ClinicWithDoctorsDTO clDTO = objectMapper.convertValue(clinicDTO, ClinicWithDoctorsDTO.class);
				clDTO.setDoctors(doctorDTOs);

				return clDTO;
			}).collect(Collectors.toList());

			response.setSuccess(true);
			response.setData(clinicsWithDoctors);
			response.setMessage("Fetched clinics with respective doctors");
			response.setStatus(HttpStatus.OK.value());

		} catch (Exception e) {
			response.setSuccess(false);
			response.setMessage("Error fetching clinics and doctors: " + e.getMessage());
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return response;
	}

	@Override
	public Response getAllDoctorsWithRespectiveClinic(int consultationType) {
		Response response = new Response();

		try {
			ResponseEntity<Response> clinicsResponse = adminServiceClient.firstRecommendedTureClincs();
			Object clinicObj = clinicsResponse.getBody().getData();

			List<ClinicDTO> clinics = objectMapper.convertValue(clinicObj, new TypeReference<List<ClinicDTO>>() {
			});

			List<ClinicWithDoctorsDTO> clinicsWithDoctors = clinics.stream()
					.map(clinicDTO -> objectMapper.convertValue(clinicDTO, ClinicWithDoctorsDTO.class))
					.collect(Collectors.toList());

			response.setSuccess(true);
			response.setData(clinicsWithDoctors);
			response.setMessage("Fetched clinics with respective doctors filtered by consultation type");
			response.setStatus(HttpStatus.OK.value());

		} catch (Exception e) {
			response.setSuccess(false);
			response.setMessage("Error fetching clinics and doctors: " + e.getMessage());
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return response;
	}

	@Override
	public Response getAllDoctorsWithRespectiveClinic(String hospitalId, int consultationType) {
		Response response = new Response();

		try {
			ResponseEntity<Response> clinicsResponse = adminServiceClient.firstRecommendedTureClincs();
			Object clinicObj = clinicsResponse.getBody().getData();

			List<ClinicDTO> clinics = objectMapper.convertValue(clinicObj, new TypeReference<List<ClinicDTO>>() {
			});

			List<ClinicDTO> filteredClinics = clinics.stream()
					.filter(clinic -> clinic.getHospitalId().equals(hospitalId)).collect(Collectors.toList());

			List<ClinicWithDoctorsDTO> clinicsWithDoctors = filteredClinics.stream()
					.map(clinicDTO -> objectMapper.convertValue(clinicDTO, ClinicWithDoctorsDTO.class))
					.collect(Collectors.toList());

			response.setSuccess(true);
			response.setData(clinicsWithDoctors);
			response.setMessage("Fetched clinics with respective doctors filtered by hospitalId and consultation type");
			response.setStatus(HttpStatus.OK.value());

		} catch (Exception e) {
			response.setSuccess(false);
			response.setMessage("Error fetching clinics and doctors: " + e.getMessage());
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return response;
	}

	@Override
	public Response loginUsingRoles(DoctorAndStaffLoginDto dto) {
		Response response = new Response();

		try {
			Optional<DoctorAndStaffLoginCredentials> credentialsOpt = credentialsRepository
					.findByUsername(dto.getUserName());

			if (credentialsOpt.isEmpty()) {
				response.setSuccess(false);
				response.setMessage("Invalid credentials");
				response.setStatus(HttpStatus.UNAUTHORIZED.value());
				return response;
			}

			DoctorAndStaffLoginCredentials credentials = credentialsOpt.get();
			credentials.setDeviceId(dto.getDeviceId());
			credentialsRepository.save(credentials);

			boolean passwordMatch = passwordEncoder != null
					&& passwordEncoder.matches(dto.getPassword(), credentials.getPassword());
			boolean roleMatch = dto.getRole() != null && credentials.getRole().equalsIgnoreCase(dto.getRole());

			if (!passwordMatch || !roleMatch) {
				response.setSuccess(false);
				response.setMessage("Invalid credentials");
				response.setStatus(HttpStatus.UNAUTHORIZED.value());
				return response;
			}

			DoctorAndStaffLoginDto resDto = new DoctorAndStaffLoginDto();
			resDto.setUserName(credentials.getUsername());
			resDto.setRole(credentials.getRole());
			resDto.setDeviceId(dto.getDeviceId());
			resDto.setStaffId(credentials.getStaffId());
			resDto.setStaffName(credentials.getStaffName());
			resDto.setHospitalId(credentials.getHospitalId());
			resDto.setHospitalName(credentials.getHospitalName());
			resDto.setBranchId(credentials.getBranchId());
			resDto.setBranchName(credentials.getBranchName());
			resDto.setPermissions(credentials.getPermissions());

			response.setSuccess(true);
			response.setMessage("Login successful");
			response.setData(resDto);
			response.setStatus(HttpStatus.OK.value());

		} catch (Exception e) {
			response.setSuccess(false);
			response.setMessage("Login error: " + e.getMessage());
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return response;
	}

	// ------------------------ Update password with username and role ---------------------------
	@Override
	public Response changePasswordWithRole(ClinicStaffUpdatedPassword updateDTO) {

		log.info("Change password request received for username={}", updateDTO.getUsername());

		Response response = new Response();

		try {
			String username = updateDTO.getUsername() != null ? updateDTO.getUsername().trim() : null;
			String role = updateDTO.getRole() != null ? updateDTO.getRole().trim() : null;

			if (username == null || username.isBlank()) {
				return failure("Username is required", HttpStatus.BAD_REQUEST);
			}

			if (role == null || role.isBlank()) {
				return failure("Role is required", HttpStatus.BAD_REQUEST);
			}

			if (updateDTO.getCurrentPassword() == null || updateDTO.getCurrentPassword().isBlank()) {
				return failure("Current password is required", HttpStatus.BAD_REQUEST);
			}

			if (updateDTO.getNewPassword() == null || updateDTO.getNewPassword().isBlank()) {
				return failure("New password is required", HttpStatus.BAD_REQUEST);
			}

			if (updateDTO.getConfirmPassword() == null || updateDTO.getConfirmPassword().isBlank()) {
				return failure("Confirm password is required", HttpStatus.BAD_REQUEST);
			}

			if (!updateDTO.getNewPassword().equals(updateDTO.getConfirmPassword())) {
				return failure("New password and confirm password do not match", HttpStatus.BAD_REQUEST);
			}

			// ================= ADMIN =================
			if ("ADMIN".equalsIgnoreCase(role)) {
				ResponseEntity<Response> adminResponse = adminServiceClient
						.updateClinicCredentialsWithUserNameAndRole(updateDTO);

				if (adminResponse != null && adminResponse.getBody() != null) {
					return adminResponse.getBody();
				}

				return failure("Failed to get response from Admin Service", HttpStatus.INTERNAL_SERVER_ERROR);
			}

			// ================= Doctor / Therapist / Receptionist / Nurse / Staff =================
			Optional<DoctorAndStaffLoginCredentials> optional = credentialsRepository.findByUsernameAndRole(username,
					role);

			if (optional.isEmpty()) {
				return failure("User not found", HttpStatus.NOT_FOUND);
			}

			DoctorAndStaffLoginCredentials credentials = optional.get();

			if (!passwordEncoder.matches(updateDTO.getCurrentPassword(), credentials.getPassword())) {
				return failure("Current password is incorrect", HttpStatus.UNAUTHORIZED);
			}

			if (passwordEncoder.matches(updateDTO.getNewPassword(), credentials.getPassword())) {
				return failure("New password cannot be the same as the current password", HttpStatus.BAD_REQUEST);
			}

			credentials.setPassword(passwordEncoder.encode(updateDTO.getNewPassword()));
			credentialsRepository.save(credentials);

			response.setSuccess(true);
			response.setStatus(HttpStatus.OK.value());
			response.setMessage("Password updated successfully");
			response.setData(null);

			return response;

		} catch (Exception e) {
			log.error("Error while updating password", e);
			return failure("Failed to update password: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public String getByTherapistDeviceId(String therapistId) {
		try {
			Optional<DoctorAndStaffLoginCredentials> credentialsOpt = credentialsRepository.findByUsername(therapistId);

			if (credentialsOpt.isEmpty()) {
				return null;
			} else {
				return credentialsOpt.get().getDeviceId();
			}
		} catch (Exception e) {
			return null;
		}
	}

	// ----------------------------- best one doctor using keyword -------------------------------------------
	@Override
	public Response getRecommendedClinicsAndDoctors(List<String> keyPointsFromUser) {

		ResponseEntity<Response> responseEntity = adminServiceClient.getHospitalUsingRecommendentaion();
		Response responseBody = responseEntity.getBody();

		ClinicWithDoctorsDTO bestClinic = null;
		DoctorsDTO bestDoctor = null;
		int bestScore = 0;

		if (responseBody != null && responseBody.isSuccess()) {
			Object rawData = responseBody.getData();
			List<ClinicWithDoctorsDTO> clinics = objectMapper.convertValue(rawData,
					new TypeReference<List<ClinicWithDoctorsDTO>>() {
					});

			for (ClinicWithDoctorsDTO clinic : clinics) {
				if (clinic.getHospitalId() == null)
					continue;

				List<Doctors> doctorEntities = doctorsRepository.findByHospitalId(clinic.getHospitalId());

				for (Doctors doctor : doctorEntities) {
					DoctorsDTO dto = DoctorMapper.mapDoctorEntityToDoctorDTO(doctor, s3Service);
					int score = calculateDoctorScore(dto, keyPointsFromUser);

					log.info("Doctor: {} | Score: {}", dto.getDoctorName(), score);

					if (score > bestScore) {
						bestScore = score;
						bestDoctor = dto;
						bestClinic = clinic;
					}
				}
			}
		}

		if (bestDoctor != null && bestClinic != null) {
			bestClinic.setDoctors(List.of(bestDoctor));
			return Response.builder().success(true).status(HttpStatus.OK.value()).data(bestClinic)
					.message("Best doctor recommendation based on keywords, ratings, experience, and qualifications")
					.build();
		}

		return Response.builder().success(false).status(HttpStatus.NOT_FOUND.value())
				.message("No matching doctor found").build();
	}

	private int calculateDoctorScore(DoctorsDTO doctor, List<String> keyPoints) {
		int score = 0;

		if (keyPoints != null && !keyPoints.isEmpty()) {
			for (String key : keyPoints) {
				String lowerKey = key.toLowerCase();
				if (doctor.getSpecialization() != null && doctor.getSpecialization().toLowerCase().contains(lowerKey)) {
					score += 6;
				}
			}
		}

		score += (int) (doctor.getDoctorAverageRating() * 10);

		try {
			int years = Integer.parseInt(doctor.getExperience().replaceAll("[^0-9]", ""));
			score += years * 2;
		} catch (Exception e) {
			// ignore if parsing fails
		}

		if (doctor.getQualification() != null) {
			String q = doctor.getQualification().toLowerCase();
			if (q.contains("dm"))
				score += 30;
			else if (q.contains("md"))
				score += 20;
			else if (q.contains("ms"))
				score += 15;
			else if (q.contains("mbbs"))
				score += 10;
		}

		return score;
	}

	@Override
	public Response getRecommendedClinicsAndDoctors(List<String> keyPointsFromUser, int consultationType) {

		ResponseEntity<Response> responseEntity = adminServiceClient.getHospitalUsingRecommendentaion();
		Response responseBody = responseEntity.getBody();

		ClinicWithDoctorsDTO bestClinic = null;
		DoctorsDTO bestDoctor = null;
		int bestScore = 0;

		if (responseBody != null && responseBody.isSuccess()) {
			Object rawData = responseBody.getData();
			List<ClinicWithDoctorsDTO> clinics = objectMapper.convertValue(rawData,
					new TypeReference<List<ClinicWithDoctorsDTO>>() {
					});

			for (ClinicWithDoctorsDTO clinic : clinics) {
				if (clinic.getHospitalId() == null)
					continue;

				List<Doctors> doctorEntities = doctorsRepository.findByHospitalId(clinic.getHospitalId());

				for (Doctors doctor : doctorEntities) {
					DoctorsDTO dto = DoctorMapper.mapDoctorEntityToDoctorDTO(doctor, s3Service);

					int score = calculateDoctorScore(dto, keyPointsFromUser);
					log.info("Doctor: {} | Score: {}", dto.getDoctorName(), score);

					if (score > bestScore) {
						bestScore = score;
						bestDoctor = dto;
						bestClinic = clinic;
					}
				}
			}
		}

		if (bestDoctor != null && bestClinic != null) {
			bestClinic.setDoctors(List.of(bestDoctor));
			return Response.builder().success(true).status(HttpStatus.OK.value()).data(bestClinic).message(
					"Best doctor recommendation based on consultation type, keywords, ratings, and qualifications")
					.build();
		}

		return Response.builder().success(false).status(HttpStatus.NOT_FOUND.value())
				.message("No matching doctor found for the given consultation type").build();
	}

	/**
	 * Helper method to check numeric consultation type.
	 */
	private boolean matchesConsultationType(ConsultationTypeDTO doctorConsultation, int consultationType) {
		if (doctorConsultation == null)
			return false;

		switch (consultationType) {
		case 1:
			return doctorConsultation.getInClinic() == 1;
		case 2:
			return doctorConsultation.getVideoOrOnline() == 2;
		case 3:
			return doctorConsultation.getServiceAndTreatments() == 3;
		default:
			return false;
		}
	}

	@Override
	public Response getRecommendedClinicsAndDoctors(String hospitalId, List<String> keyPointsFromUser,
			int consultationType) {

		ResponseEntity<Response> responseEntity = adminServiceClient.getHospitalUsingRecommendentaion();
		Response responseBody = responseEntity.getBody();

		ClinicWithDoctorsDTO bestClinic = null;
		DoctorsDTO bestDoctor = null;
		int bestScore = 0;

		if (responseBody != null && responseBody.isSuccess()) {
			Object rawData = responseBody.getData();
			List<ClinicWithDoctorsDTO> clinics = objectMapper.convertValue(rawData,
					new TypeReference<List<ClinicWithDoctorsDTO>>() {
					});

			for (ClinicWithDoctorsDTO clinic : clinics) {
				if (clinic.getHospitalId() == null || !clinic.getHospitalId().equals(hospitalId))
					continue;

				List<Doctors> doctorEntities = doctorsRepository.findByHospitalId(hospitalId);

				for (Doctors doctor : doctorEntities) {
					DoctorsDTO dto = DoctorMapper.mapDoctorEntityToDoctorDTO(doctor, s3Service);

					int score = calculateDoctorScore(dto, keyPointsFromUser);
					log.info("Doctor: {} | Score: {}", dto.getDoctorName(), score);

					if (score > bestScore) {
						bestScore = score;
						bestDoctor = dto;
						bestClinic = clinic;
					}
				}
			}
		}

		if (bestDoctor != null && bestClinic != null) {
			bestClinic.setDoctors(List.of(bestDoctor));
			return Response.builder().success(true).status(HttpStatus.OK.value()).data(bestClinic)
					.message("Best doctor recommendation for given hospital, consultation type, and keywords.").build();
		}

		return Response.builder().success(false).status(HttpStatus.NOT_FOUND.value())
				.message("No matching doctor found for the given hospital and consultation type.").build();
	}

	@Override
	public Response getDoctorsByHospitalIdAndBranchId(String hospitalId, String branchId) {
		Response response = new Response();
		try {
			List<Doctors> doctorList = doctorsRepository.findByHospitalIdAndBranchIdIncludingBranches(hospitalId,
					branchId);

			doctorList = doctorList.stream().filter(doc -> doc.getBranches() != null
					&& doc.getBranches().stream().anyMatch(b -> branchId.equals(b.getBranchId()))).toList();

			if (!doctorList.isEmpty()) {
				List<DoctorsDTO> dtos = doctorList.stream()
						.map(doc -> DoctorMapper.mapDoctorEntityToDoctorDTO(doc, s3Service)).toList();
				response.setSuccess(true);
				response.setData(dtos);
				response.setMessage(
						"Doctors fetched successfully for hospitalId: " + hospitalId + " and branchId: " + branchId);
				response.setStatus(HttpStatus.OK.value());
			} else {
				response.setSuccess(true);
				response.setData(Collections.emptyList());
				response.setMessage("No doctors found for hospitalId: " + hospitalId + " and branchId: " + branchId);
				response.setStatus(HttpStatus.OK.value());
			}
		} catch (Exception e) {
			response.setSuccess(false);
			response.setMessage("Error fetching doctors: " + e.getMessage());
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return response;
	}

	public boolean blockingSlot(TempBlockingSlot tempBlockingSlot) {
		if (tempBlockingSlot == null || tempBlockingSlot.getDoctorId() == null
				|| tempBlockingSlot.getServiceDate() == null || tempBlockingSlot.getServicetime() == null) {
			return false;
		}
		try {
			DoctorSlot doctorSlots = slotRepository.findByDoctorIdAndDateAndBranchId(tempBlockingSlot.getDoctorId(),
					tempBlockingSlot.getServiceDate(), tempBlockingSlot.getBranchId());
			if (doctorSlots == null || doctorSlots.getAvailableSlots() == null
					|| doctorSlots.getAvailableSlots().isEmpty()) {
				return false;
			}
			Optional<DoctorAvailableSlotDTO> matchingSlotOpt = doctorSlots.getAvailableSlots().stream()
					.filter(slot -> tempBlockingSlot.getServicetime().equalsIgnoreCase(slot.getSlot())).findFirst();
			if (matchingSlotOpt.isPresent()) {
				DoctorAvailableSlotDTO matchingSlot = matchingSlotOpt.get();
				if (matchingSlot.isSlotbooked()) {
					return true;
				} else {
					matchingSlot.setSlotbooked(true);
					slotRepository.save(doctorSlots);
					tempBlockingSlot.setTimeInMillis(System.currentTimeMillis());
					slots.add(tempBlockingSlot);
					return true;
				}
			} else {
				return false;
			}
		} catch (Exception e) {
			log.error("Error while blocking slot: {}", e.getMessage(), e);
			return false;
		} finally {
			log.debug("Slot blocking process completed for doctor: {}", tempBlockingSlot.getDoctorId());
		}
	}

	@Scheduled(fixedRate = 30000)
	public void checkingSlots() {
		try {
			long currentMillis = System.currentTimeMillis();
			List<TempBlockingSlot> objectsToRemove = new CopyOnWriteArrayList<>();
			List<TempBlockingSlot> expiredSlots = slots.stream()
					.filter(n -> Math.abs(currentMillis - n.getTimeInMillis()) >= 90000).collect(Collectors.toList());
			expiredSlots.forEach(n -> {
				try {
					BookingResponse bkng = null;
					try {
						bkng = bookingFeign.blockingSlot(n);
					} catch (Exception e) {
						log.error("Feign error while releasing expired slot: {}", e.getMessage());
					}
					if (bkng == null) {
						DoctorSlot doctorSlots = slotRepository.findByDoctorIdAndDateAndBranchId(n.getDoctorId(),
								n.getServiceDate(), n.getBranchId());
						if (doctorSlots != null) {
							doctorSlots.getAvailableSlots().stream()
									.filter(slot -> slot.getSlot().equalsIgnoreCase(n.getServicetime()))
									.forEach(slot -> slot.setSlotbooked(false));

							slotRepository.save(doctorSlots);
							objectsToRemove.add(n);
						}
					} else {
						objectsToRemove.add(n);
					}
				} catch (Exception e) {
					log.error("Error processing expired slot: {}", e.getMessage());
				}
			});
			slots.removeAll(objectsToRemove);
		} catch (Exception e) {
			log.error("Error in checkingSlots scheduled task: {}", e.getMessage(), e);
		}
	}


    public String getType(String doctorId){
        try{
            return doctorsRepository.findByDoctorId(doctorId).get().getProviderType();
        }catch (Exception e){
            return null;
        }
    }

	public void updatePermissions(String username, Map<String, List<String>> permissions) {
		try {
			Optional<DoctorAndStaffLoginCredentials> credentialsOpt = credentialsRepository
					.findByUsername(username);
			if(credentialsOpt.isPresent()){
				credentialsOpt.get().setPermissions(permissions);
				credentialsRepository.save(credentialsOpt.get());
			}
		} catch (Exception e) {
		}
	}

	public void deleteAllPermissions(String username) {
		try {
			Optional<DoctorAndStaffLoginCredentials> credentialsOpt = credentialsRepository.findByUsername(username);

			if (credentialsOpt.isPresent()) {
				DoctorAndStaffLoginCredentials credentials = credentialsOpt.get();

				Map<String, List<String>> rolePermissions = credentials.getPermissions();

				if (rolePermissions != null) {
					// Clear all entries in the map
					rolePermissions.clear();

					// Save updated entity
					credentials.setPermissions(rolePermissions);
					credentialsRepository.save(credentials);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	@Override
	public String getDeviceId(String doctorId) {

		DoctorAndStaffLoginCredentials doctor = credentialsRepository.findByUsername(doctorId)
				.orElseThrow();

		return doctor.getDeviceId();
	}


	@Override
	public ResponseEntity<Response> getDoctor(
			ClinicLoginRequestDTO clinicLoginRequestDTO) {

		// 1. Find credentials using username
		DoctorAndStaffLoginCredentials credentials =
				credentialsRepository
						.findByUsername(clinicLoginRequestDTO.getUserName())
						.orElseThrow(() ->
								new RuntimeException("Invalid username or password"));

		// 2. Verify hashed password
		if (!passwordEncoder.matches(
				clinicLoginRequestDTO.getPassword(),
				credentials.getPassword())) {

			throw new RuntimeException("Invalid username or password");
		}

		// 3. Extract staffId
		String staffId = credentials.getStaffId();

		if (staffId == null || staffId.isBlank()) {
			throw new RuntimeException("Staff ID not found");
		}

		// 4. Find doctor using staffId
		// Assuming staffId is stored as doctorId in Doctors collection
		Doctors doctor = doctorsRepository
				.findByDoctorId(staffId)
				.orElseThrow(() ->
						new RuntimeException(
								"Doctor not found for staff ID: " + staffId));
		
		String doctorProfileUrl = null;

		if (doctor.getDoctorPicture() != null
		        && !doctor.getDoctorPicture().isBlank()) {

		    doctorProfileUrl = s3Service.generateSignedUrl(
		            doctor.getDoctorPicture());
		}

		// 5. Map doctor + credentials to response
		DoctorResponse doctorResponse = DoctorResponse.builder()
				.doctorId(doctor.getDoctorId())
				.clinicId(doctor.getHospitalId())
				.clinicName(doctor.getHospitalName())
				.branchId(doctor.getBranchId())
				.branchName(doctor.getBranches().get(0).getBranchName())
				.doctorName(doctor.getDoctorName())
				.doctorMobileNumber(doctor.getDoctorMobileNumber())
//				.doctorProfile(doctor.getDoctorPicture())
				.doctorProfile(doctorProfileUrl)
				.branches(doctor.getBranches())
				.build();

		// 6. Build response
		Response response = Response.builder()
				.success(true)
				.data(doctorResponse)
				.message("Doctor details fetched successfully")
				.status(200)
				.build();

		return ResponseEntity.ok(response);
	}
}