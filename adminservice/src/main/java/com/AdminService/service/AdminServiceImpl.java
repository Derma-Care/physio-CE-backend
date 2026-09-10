package com.AdminService.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import com.AdminService.dto.*;
import com.AdminService.feign.BookingFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

//import com.AdminService.dto.CategoryDto;


//import com.AdminService.dto.ServicesDto;
//import com.AdminService.dto.SubServicesDto;
//import com.AdminService.dto.SubServicesInfoDto;

import com.AdminService.entity.Admin;
import com.AdminService.entity.Branch;
import com.AdminService.entity.BranchCounter;
import com.AdminService.entity.BranchCredentials;
import com.AdminService.entity.Clinic;
import com.AdminService.entity.ClinicCredentials;
import com.AdminService.entity.Counter;
import com.AdminService.feign.ClinicAdminFeign;

import com.AdminService.repository.AdminRepository;
import com.AdminService.repository.BranchCredentialsRepository;
import com.AdminService.repository.BranchRepository;
import com.AdminService.repository.ClinicCredentialsRepository;
import com.AdminService.repository.ClinicRep;
import com.AdminService.util.ExtractFeignMessage;
import com.AdminService.util.PermissionsUtil;
import com.AdminService.util.Response;
import com.AdminService.util.ResponseStructure;
import com.fasterxml.jackson.databind.ObjectMapper;

import feign.FeignException;

@Service
public class AdminServiceImpl implements AdminService {

	@Autowired
	private AdminRepository adminRepository;

	@Autowired
	private ClinicRep clinicRep;

    @Autowired
	private ClinicCredentialsRepository clinicCredentialsRepository;

	@Autowired
	private ClinicAdminFeign clinicAdminFeign;

	@Autowired
	private BranchRepository branchRepository;

	@Autowired

	private BranchCredentialsRepository branchCredentialsRepository;

	@Autowired
	private MongoOperations mongoOperations;

	@Autowired
	private EmailService emailService; // ✅ ADD THIS

    @Autowired
    private BookingFeign bookingFeign;

	// @Autowired
//	private QuetionsAndAnswerForAddClinicRepository quetionsAndAnswerForAddClinicRepository;

	@Override

	public Response adminRegister(AdminHelper helperAdmin) {

		Response response = new Response();

		try {

			Optional<Admin> userName = adminRepository.findByUserName(helperAdmin.getUserName());

			Admin mobileNumber = adminRepository.findByMobileNumber(helperAdmin.getMobileNumber());

			if (mobileNumber != null) {

				response.setMessage("MobileNumber is Already Exist");

				response.setStatus(409);

				response.setSuccess(false);

				return response;
			}

			if (userName.isPresent()) {

				response.setMessage("UserName already exist");

				response.setStatus(409);

				response.setSuccess(false);

				return response;

			} else {

				Admin entityAdmin = new Admin();

				entityAdmin.setUserName(helperAdmin.getUserName());

				entityAdmin.setPassword(helperAdmin.getPassword());

				entityAdmin.setMobileNumber(helperAdmin.getMobileNumber());

				adminRepository.save(entityAdmin);

				response.setMessage("Credentials Are saved successfully");

				response.setStatus(200);

				response.setSuccess(true);

				return response;

			}
		} catch (Exception e) {

			response.setMessage(e.getMessage());

			response.setStatus(500);

			response.setSuccess(false);

			return response;

		}

	}

	@Override
	public Response adminLogin(String userName, String password) {
		Response response = new Response();

		try {
			Optional<Admin> userOptional = adminRepository.findByUserName(userName);

			if (userOptional.isPresent()) {
				Admin user = userOptional.get();

				// Check if password matches
				if (user.getPassword().equals(password)) {
					response.setMessage("Login Successful");
					response.setStatus(200);
					response.setSuccess(true);
				} else {
					response.setMessage("Incorrect Password");
					response.setStatus(401);
					response.setSuccess(false);
				}
			} else {
				// Check if password matches any other user
				List<Admin> allAdmins = adminRepository.findAll();
				boolean passwordExists = allAdmins.stream().anyMatch(admin -> admin.getPassword().equals(password));

				if (passwordExists) {
					response.setMessage("Incorrect UserName");
				} else {
					response.setMessage("Incorrect UserName and Password");
				}

				response.setStatus(401);
				response.setSuccess(false);
			}

		} catch (Exception e) {
			response.setMessage("Internal Server Error: " + e.getMessage());
			response.setStatus(500);
			response.setSuccess(false);
		}

		return response;
	}

	@Override
	public Response createClinic(ClinicDTO clinic) {

		Response response = new Response();

		try {
			// ---------------- Duplicate checks ----------------
			 if (clinic.getContactNumber() != null && !clinic.getContactNumber().isBlank()) {
			        if (clinicRep.findByContactNumber(clinic.getContactNumber()) != null) {
			            response.setMessage("ContactNumber already exists");
			            response.setSuccess(false);
			            response.setStatus(409);
			            return response;
			        }
			    }

			    if (clinic.getLicenseNumber() != null && !clinic.getLicenseNumber().isBlank()) {
			        if (clinicRep.findByLicenseNumber(clinic.getLicenseNumber()) != null) {
			            response.setMessage("LicenseNumber already exists");
			            response.setSuccess(false);
			            response.setStatus(409);
			            return response;
			        }
			    }

			    if (clinic.getEmailAddress() != null && !clinic.getEmailAddress().isBlank()) {
			        if (clinicRep.findByEmailAddress(clinic.getEmailAddress()) != null) {
			            response.setMessage("EmailAddress already exists");
			            response.setSuccess(false);
			            response.setStatus(409);
			            return response;
			        }
			    }

			// ---------------- Save clinic ----------------
			Clinic savedClinic = new Clinic();
			savedClinic.setName(clinic.getName());
			savedClinic.setHospitalId(generateHospitalId());
			savedClinic.setBranch(clinic.getBranch());
			savedClinic.setAddress(clinic.getAddress());
			savedClinic.setCity(clinic.getCity());
			savedClinic.setContactNumber(clinic.getContactNumber());
			savedClinic.setOpeningTime(clinic.getOpeningTime());
			savedClinic.setClosingTime(clinic.getClosingTime());
			savedClinic.setEmailAddress(clinic.getEmailAddress());
			savedClinic.setWebsite(clinic.getWebsite());
			savedClinic.setLicenseNumber(clinic.getLicenseNumber());
			savedClinic.setIssuingAuthority(clinic.getIssuingAuthority());
			savedClinic.setRecommended(clinic.isRecommended());
			savedClinic.setClinicType(clinic.getClinicType());
			savedClinic.setHospitalOverallRating(0.0);
			savedClinic.setSubscription(clinic.getSubscription());
			savedClinic.setFreeFollowUps(clinic.getFreeFollowUps());
			savedClinic.setLatitude(clinic.getLatitude());
			savedClinic.setLongitude(clinic.getLongitude());
			savedClinic.setWalkthrough(clinic.getWalkthrough());
			savedClinic.setNabhScore(clinic.getNabhScore());
			savedClinic.setLoyaltyPoints(clinic.getLoyaltyPoints());
            savedClinic.setLocation(clinic.getLocation());
			savedClinic.setServer(clinic.getServer());
			savedClinic.setSubscriptionStartDate(clinic.getSubscriptionStartDate());
			savedClinic.setSubscriptionDates(clinic.getSubscriptionDates());
			savedClinic.setSubscriptionEndDate(clinic.getSubscriptionEndDate());

			// ---------------- NGK CORE ----------------
			savedClinic.setStatus("PENDING");
			savedClinic.setRole("ADMIN");
			savedClinic.setPermissions(clinic.getPermissions());
			savedClinic.setCreatedAt(String.valueOf(Instant.now())); // FIXED

			// ❌ Credentials are NOT created here

			decodeBase64Documents(clinic, savedClinic);

			if (clinic.getConsultationExpiration() == null || clinic.getConsultationExpiration().isBlank()) {
				throw new IllegalArgumentException("Consultation expiration is required");
			}
			savedClinic.setConsultationExpiration(clinic.getConsultationExpiration());

			savedClinic.setInstagramHandle(clinic.getInstagramHandle());
			savedClinic.setTwitterHandle(clinic.getTwitterHandle());
			savedClinic.setFacebookHandle(clinic.getFacebookHandle());

			Clinic saved = clinicRep.save(savedClinic);

			// ---------------- Create default branch ----------------
			BranchCounter counter = mongoOperations.findAndModify(
					Query.query(Criteria.where("_id").is(saved.getHospitalId())), new Update().inc("seq", 1),
					FindAndModifyOptions.options().returnNew(true).upsert(true), BranchCounter.class);

			String branchId = String.format("%04d%02d", Integer.parseInt(saved.getHospitalId()), counter.getSeq());

			Branch branch = new Branch();
			branch.setClinicId(saved.getHospitalId());
			branch.setHospitalName(saved.getName());
			branch.setBranchId(branchId);
			branch.setBranchName(clinic.getBranch() != null && !clinic.getBranch().isEmpty() ? clinic.getBranch()
					: saved.getName() + " Main Branch");
			branch.setAddress(saved.getAddress());
			branch.setCity(saved.getCity());
			branch.setContactNumber(saved.getContactNumber());
			branch.setEmail(saved.getEmailAddress());
			branch.setRole("ADMIN");
			branch.setLatitude(String.valueOf(saved.getLatitude()));
			branch.setLongitude(String.valueOf(saved.getLongitude()));
			branch.setPermissions(clinic.getPermissions());
			branch.setLoyaltyPoints(saved.getLoyaltyPoints());
            branch.setLocation(saved.getLocation());

			branch.setSubscriptionStartDate(saved.getSubscriptionStartDate());
			branch.setSubscriptionDates(saved.getSubscriptionDates());
			branch.setSubscriptionEndDate(saved.getSubscriptionEndDate());

			Branch savedBranch = branchRepository.save(branch);

			saved.setBranches(List.of(savedBranch));
			clinicRep.save(saved);

			// ---------------- Email acknowledgement ----------------
			Map<String, String> mailData = new HashMap<>();
			mailData.put("clinicName", saved.getName());
			mailData.put("subject", "Clinic Registration Pending");
			mailData.put("message", "Your clinic registration has been received successfully.\n"
					+ "Our team will verify your details and update you shortly.");
			emailService.sendEmail(saved.getEmailAddress(), mailData);

			// ---------------- Prepare response ----------------
			Map<String, Object> data = new HashMap<>();
			data.put("clinicId", saved.getHospitalId());
			data.put("branchId", savedBranch.getBranchId());
			data.put("status", saved.getStatus());

			response.setSuccess(true);
			response.setStatus(200);
			response.setMessage("Clinic registered successfully. Verification pending.");
			response.setData(data);

			return response;

		} catch (Exception e) {
			Response error = new Response();
			error.setMessage("Error occurred while creating clinic: " + e.getMessage());
			error.setSuccess(false);
			error.setStatus(500);
			return error;
		}
	}

	private void decodeBase64Documents(ClinicDTO clinic, Clinic savedClinic) {

		if (clinic.getHospitalLogo() != null && !clinic.getHospitalLogo().isEmpty()) {

			savedClinic.setHospitalLogo(Base64.getDecoder().decode(clinic.getHospitalLogo()));
		}

		if (clinic.getContractorDocuments() != null && !clinic.getContractorDocuments().isEmpty()) {

			savedClinic.setContractorDocuments(Base64.getDecoder().decode(clinic.getContractorDocuments()));
		}

		if (clinic.getHospitalDocuments() != null && !clinic.getHospitalDocuments().isEmpty()) {

			savedClinic.setHospitalDocuments(Base64.getDecoder().decode(clinic.getHospitalDocuments()));
		}

		if (clinic.getClinicalEstablishmentCertificate() != null
				&& !clinic.getClinicalEstablishmentCertificate().isEmpty()) {

			savedClinic.setClinicalEstablishmentCertificate(
					Base64.getDecoder().decode(clinic.getClinicalEstablishmentCertificate()));
		}

		if (clinic.getBusinessRegistrationCertificate() != null
				&& !clinic.getBusinessRegistrationCertificate().isEmpty()) {

			savedClinic.setBusinessRegistrationCertificate(
					Base64.getDecoder().decode(clinic.getBusinessRegistrationCertificate()));
		}

		if (clinic.getDrugLicenseCertificate() != null && !clinic.getDrugLicenseCertificate().isEmpty()) {

			savedClinic.setDrugLicenseCertificate(Base64.getDecoder().decode(clinic.getDrugLicenseCertificate()));
		}

		if (clinic.getDrugLicenseFormType() != null && !clinic.getDrugLicenseFormType().isEmpty()) {

			savedClinic.setDrugLicenseFormType(Base64.getDecoder().decode(clinic.getDrugLicenseFormType()));
		}

		if (clinic.getPharmacistCertificate() != null && !clinic.getPharmacistCertificate().isEmpty()) {

			savedClinic.setPharmacistCertificate(Base64.getDecoder().decode(clinic.getPharmacistCertificate()));
		}

		if (clinic.getBiomedicalWasteManagementAuth() != null && !clinic.getBiomedicalWasteManagementAuth().isEmpty()) {

			savedClinic.setBiomedicalWasteManagementAuth(
					Base64.getDecoder().decode(clinic.getBiomedicalWasteManagementAuth()));
		}

		if (clinic.getTradeLicense() != null && !clinic.getTradeLicense().isEmpty()) {

			savedClinic.setTradeLicense(Base64.getDecoder().decode(clinic.getTradeLicense()));
		}

		if (clinic.getFireSafetyCertificate() != null && !clinic.getFireSafetyCertificate().isEmpty()) {

			savedClinic.setFireSafetyCertificate(Base64.getDecoder().decode(clinic.getFireSafetyCertificate()));
		}

		if (clinic.getProfessionalIndemnityInsurance() != null
				&& !clinic.getProfessionalIndemnityInsurance().isEmpty()) {

			savedClinic.setProfessionalIndemnityInsurance(
					Base64.getDecoder().decode(clinic.getProfessionalIndemnityInsurance()));
		}

		if (clinic.getGstRegistrationCertificate() != null && !clinic.getGstRegistrationCertificate().isEmpty()) {

			savedClinic
					.setGstRegistrationCertificate(Base64.getDecoder().decode(clinic.getGstRegistrationCertificate()));
		}
	}

	@Override
	public Response startVerificationProcess(String clinicId) {

		Response response = new Response();

		try {
			Clinic clinic = findClinic(clinicId);

			if (!"PENDING".equals(clinic.getStatus())) {
				response.setSuccess(false);
				response.setStatus(400);
				response.setMessage("Clinic is not in PENDING state");
				return response;
			}

			clinic.setStatus("VERIFICATION_IN_PROGRESS");
			clinicRep.save(clinic);

			// Email notification
			Map<String, String> mailData = new HashMap<>();
			mailData.put("clinicName", clinic.getName());
			mailData.put("subject", "Clinic Verification Started");
			mailData.put("message", "Your clinic verification process has started.\n"
					+ "Our team is reviewing your submitted documents.");
			emailService.sendEmail(clinic.getEmailAddress(), mailData);

			response.setSuccess(true);
			response.setStatus(200);
			response.setMessage("Verification started successfully");
			response.setHospitalId(clinic.getHospitalId());
			response.setHospitalName(clinic.getName());

			return response;

		} catch (Exception e) {
			response.setSuccess(false);
			response.setStatus(500);
			response.setMessage("Failed to start verification: " + e.getMessage());
			return response;
		}
	}

	@Override
	public Response verifyClinic(String clinicId) {

		Response response = new Response();

		try {
			Clinic clinic = clinicRep.findByHospitalId(clinicId);

			if (clinic == null) {
				response.setSuccess(false);
				response.setStatus(404);
				response.setMessage("Clinic not found");
				return response;
			}

			if (!"VERIFICATION_IN_PROGRESS".equals(clinic.getStatus())) {
				response.setSuccess(false);
				response.setStatus(400);
				response.setMessage("Clinic is not under verification");
				return response;
			}

			// 🔐 Generate secure password (YOUR METHOD)
			String tempPassword = generatePassword(9);
			// 🔐 Save clinic credentials
			ClinicCredentials credentials = new ClinicCredentials();
			credentials.setHospitalName(clinic.getName());
			credentials.setUserName(clinic.getHospitalId());
			credentials.setPassword(tempPassword);
			credentials.setEmail(clinic.getEmailAddress());
			
			credentials.setMobilenumber(clinic.getContactNumber());
			credentials.setRole("ADMIN");

			// 🔧 FIX: permissions type mismatch
			Map<String, Map<String, List<String>>> permissions = new HashMap<>();
			permissions.put("ADMIN", clinic.getPermissions());

			credentials.setPermissions(permissions);
			clinicCredentialsRepository.save(credentials);

			// ✅ Update clinic status
			clinic.setStatus("VERIFIED");
			clinicRep.save(clinic);

			// 📧 Send email
			Map<String, String> mailData = new HashMap<>();
			mailData.put("clinicName", clinic.getName());
			mailData.put("subject", "Clinic Verified Successfully");
			mailData.put("message", "Congratulations! Your clinic has been verified successfully.");
			mailData.put("username", credentials.getUserName());
			mailData.put("password", tempPassword);

			emailService.sendEmail(clinic.getEmailAddress(), mailData);

			// ✅ Response
			response.setSuccess(true);
			response.setStatus(200);
			response.setMessage("Clinic verified successfully");
			response.setHospitalId(clinic.getHospitalId());
			response.setRole("ADMIN");
			response.setPermissions(clinic.getPermissions());
			return response;

		} catch (Exception e) {
			response.setSuccess(false);
			response.setStatus(500);
			response.setMessage("Failed to verify clinic: " + e.getMessage());
			return response;
		}
	}

	@Override
	public Response rejectClinic(String clinicId, String reason) {

		Response response = new Response();

		try {
			Clinic clinic = findClinic(clinicId);

			if ("VERIFIED".equals(clinic.getStatus())) {
				response.setSuccess(false);
				response.setStatus(400);
				response.setMessage("Verified clinic cannot be rejected");
				return response;
			}

			clinic.setStatus("REJECTED");
			clinicRep.save(clinic);

			// Rejection email
			Map<String, String> mailData = new HashMap<>();
			mailData.put("clinicName", clinic.getName());
			mailData.put("subject", "Clinic Registration Rejected");
			mailData.put("message", "Unfortunately, your clinic registration has been rejected.");
			mailData.put("reason", reason);

			emailService.sendEmail(clinic.getEmailAddress(), mailData);

			response.setSuccess(true);
			response.setStatus(200);
			response.setMessage("Clinic rejected successfully");
			response.setHospitalId(clinic.getHospitalId());

			return response;

		} catch (Exception e) {
			response.setSuccess(false);
			response.setStatus(500);
			response.setMessage("Failed to reject clinic: " + e.getMessage());
			return response;
		}
	}

	private Clinic findClinic(String clinicId) {

		Clinic clinic = clinicRep.findByHospitalId(clinicId);

		if (clinic == null) {
			throw new RuntimeException("Clinic not found with id: " + clinicId);
		}

		return clinic;
	}

	@Override
	public Response getClinicById(String clinicId) {

		Response response = new Response();

		try {

			Clinic clinic = clinicRep.findByHospitalId(clinicId);

			if (clinic != null) {

				ClinicDTO clnc = new ClinicDTO();

				clnc.setAddress(clinic.getAddress() != null ? clinic.getAddress() : "");

				clnc.setCity(clinic.getCity() != null ? clinic.getCity() : "");

				clnc.setHospitalId(clinic.getHospitalId() != null ? clinic.getHospitalId() : "");

				clnc.setName(clinic.getName() != null ? clinic.getName() : "");

				clnc.setEmailAddress(clinic.getEmailAddress() != null ? clinic.getEmailAddress() : "");

				clnc.setWebsite(clinic.getWebsite() != null ? clinic.getWebsite() : "");

				clnc.setLicenseNumber(clinic.getLicenseNumber() != null ? clinic.getLicenseNumber() : "");

				clnc.setIssuingAuthority(clinic.getIssuingAuthority() != null ? clinic.getIssuingAuthority() : "");

				clnc.setClosingTime(clinic.getClosingTime() != null ? clinic.getClosingTime() : "");

				clnc.setOpeningTime(clinic.getOpeningTime() != null ? clinic.getOpeningTime() : "");

				clnc.setContactNumber(clinic.getContactNumber() != null ? clinic.getContactNumber() : "");
				 clnc.setLoyaltyPoints(clinic.getLoyaltyPoints());
				clnc.setRecommended(clinic.isRecommended());
				clnc.setSubscription(clinic.getSubscription());
				clnc.setHospitalOverallRating(clinic.getHospitalOverallRating());
				clnc.setFreeFollowUps(clinic.getFreeFollowUps());
                clnc.setLocation(clinic.getLocation());
				clnc.setLatitude(clinic.getLatitude());
				clnc.setLongitude(clinic.getLongitude());
				clnc.setWalkthrough(clinic.getWalkthrough());
				clnc.setNabhScore(clinic.getNabhScore());
				clnc.setBranch(clinic.getBranch());
				clnc.setRole(clinic.getRole());
				clnc.setPermissions(clinic.getPermissions());
				clnc.setLoyaltyPoints(clinic.getLoyaltyPoints());
				clnc.setSubscriptionStartDate(clinic.getSubscriptionStartDate());
				clnc.setSubscriptionDates(clinic.getSubscriptionDates());
				clnc.setSubscriptionEndDate(clinic.getSubscriptionEndDate());


				clnc.setBranches(clinic.getBranches());

				// Hospital Logo

				clnc.setHospitalLogo(

						clinic.getHospitalLogo() != null ? Base64.getEncoder().encodeToString(clinic.getHospitalLogo())
								: ""

				);

				// Hospital Documents (single)

				clnc.setHospitalDocuments(

						clinic.getHospitalDocuments() != null
								? Base64.getEncoder().encodeToString(clinic.getHospitalDocuments())
								: ""

				);

				// Contractor Documents (single)

				clnc.setContractorDocuments(

						clinic.getContractorDocuments() != null
								? Base64.getEncoder().encodeToString(clinic.getContractorDocuments())
								: ""

				);

				// Pharmacist Info

				clnc.setHasPharmacist(clinic.getHasPharmacist() != null ? clinic.getHasPharmacist() : "");

				clnc.setPharmacistCertificate(

						clinic.getPharmacistCertificate() != null
								? Base64.getEncoder().encodeToString(clinic.getPharmacistCertificate())
								: ""

				);

				// Medicines Handling

				clnc.setMedicinesSoldOnSite(
						clinic.getMedicinesSoldOnSite() != null ? clinic.getMedicinesSoldOnSite() : "");

				clnc.setDrugLicenseCertificate(

						clinic.getDrugLicenseCertificate() != null
								? Base64.getEncoder().encodeToString(clinic.getDrugLicenseCertificate())
								: ""

				);

				clnc.setDrugLicenseFormType(

						clinic.getDrugLicenseFormType() != null
								? Base64.getEncoder().encodeToString(clinic.getDrugLicenseFormType())
								: ""

				);

				// Extended Certifications (single files)

				clnc.setClinicType(clinic.getClinicType() != null ? clinic.getClinicType() : "");

				clnc.setClinicalEstablishmentCertificate(

						clinic.getClinicalEstablishmentCertificate() != null
								? Base64.getEncoder().encodeToString(clinic.getClinicalEstablishmentCertificate())
								: ""

				);

				clnc.setBusinessRegistrationCertificate(

						clinic.getBusinessRegistrationCertificate() != null
								? Base64.getEncoder().encodeToString(clinic.getBusinessRegistrationCertificate())
								: ""

				);

				clnc.setBiomedicalWasteManagementAuth(

						clinic.getBiomedicalWasteManagementAuth() != null
								? Base64.getEncoder().encodeToString(clinic.getBiomedicalWasteManagementAuth())
								: ""

				);

				clnc.setTradeLicense(

						clinic.getTradeLicense() != null ? Base64.getEncoder().encodeToString(clinic.getTradeLicense())
								: ""

				);

				clnc.setFireSafetyCertificate(

						clinic.getFireSafetyCertificate() != null
								? Base64.getEncoder().encodeToString(clinic.getFireSafetyCertificate())
								: ""

				);

				clnc.setProfessionalIndemnityInsurance(

						clinic.getProfessionalIndemnityInsurance() != null
								? Base64.getEncoder().encodeToString(clinic.getProfessionalIndemnityInsurance())
								: ""

				);

				clnc.setGstRegistrationCertificate(

						clinic.getGstRegistrationCertificate() != null
								? Base64.getEncoder().encodeToString(clinic.getGstRegistrationCertificate())
								: ""

				);

				// Others – list of base64 strings

				List<String> othersEncoded = new ArrayList<>();

				if (clinic.getOthers() != null) {

					for (byte[] file : clinic.getOthers()) {

						if (file != null) {

							othersEncoded.add(Base64.getEncoder().encodeToString(file));

						}

					}

				}

				clnc.setOthers(othersEncoded);

				// Consultation Expiration

				clnc.setConsultationExpiration(
						clinic.getConsultationExpiration() != null ? clinic.getConsultationExpiration() : "");

				// Social Media Handles

				clnc.setInstagramHandle(clinic.getInstagramHandle() != null ? clinic.getInstagramHandle() : "");

				clnc.setTwitterHandle(clinic.getTwitterHandle() != null ? clinic.getTwitterHandle() : "");

				clnc.setFacebookHandle(clinic.getFacebookHandle() != null ? clinic.getFacebookHandle() : "");
				
				clnc.setBranches(clinic.getBranches());

				clnc.setServer(clinic.getServer());

				response.setMessage("Clinic fetched successfully");

				response.setSuccess(true);

				response.setStatus(200);

				response.setData(clnc);

				return response;

			} else {

				response.setMessage("Clinic not found");

				response.setSuccess(false);

				response.setStatus(404);

				return response;

			}

		} catch (Exception e) {

			response.setMessage("Error occurred while fetching clinic: " + e.getMessage());

			response.setSuccess(false);

			response.setStatus(500);

			return response;

		}

	}

	@Override
	public Response getAllClinics() {

		Response response = new Response();

		try {

			List<Clinic> clinics = clinicRep.findAll();

			List<ClinicDTO> list = new ArrayList<>();

			if (!clinics.isEmpty()) {

				for (Clinic clinic : clinics) {

					ClinicDTO clnc = new ClinicDTO();

					// Simple fields

					clnc.setAddress(clinic.getAddress() != null ? clinic.getAddress() : "");

					clnc.setCity(clinic.getCity() != null ? clinic.getCity() : "");

					clnc.setHospitalId(clinic.getHospitalId() != null ? clinic.getHospitalId() : "");

					clnc.setEmailAddress(clinic.getEmailAddress() != null ? clinic.getEmailAddress() : "");

					clnc.setWebsite(clinic.getWebsite() != null ? clinic.getWebsite() : "");

					clnc.setLicenseNumber(clinic.getLicenseNumber() != null ? clinic.getLicenseNumber() : "");

					clnc.setIssuingAuthority(clinic.getIssuingAuthority() != null ? clinic.getIssuingAuthority() : "");

					clnc.setClosingTime(clinic.getClosingTime() != null ? clinic.getClosingTime() : "");

					clnc.setContactNumber(clinic.getContactNumber() != null ? clinic.getContactNumber() : "");

					clnc.setName(clinic.getName() != null ? clinic.getName() : "");

					clnc.setOpeningTime(clinic.getOpeningTime() != null ? clinic.getOpeningTime() : "");

					clnc.setRecommended(clinic.isRecommended());
					clnc.setSubscription(clinic.getSubscription());
					clnc.setServer(clinic.getServer());
					clnc.setHospitalOverallRating(clinic.getHospitalOverallRating());
					clnc.setFreeFollowUps(clinic.getFreeFollowUps());				
		            clnc.setLoyaltyPoints(clinic.getLoyaltyPoints());             
					clnc.setLatitude(clinic.getLatitude());
					clnc.setLongitude(clinic.getLongitude());
					clnc.setWalkthrough(clinic.getWalkthrough());
					clnc.setNabhScore(clinic.getNabhScore());
					clnc.setBranch(clinic.getBranch());
					clnc.setRole(clinic.getRole());
					clnc.setPermissions(clinic.getPermissions());
					clnc.setStatus(clinic.getStatus());
					clnc.setBranches(clinic.getBranches());
                    clnc.setLocation(clinic.getLocation());

					clnc.setSubscriptionStartDate(clinic.getSubscriptionStartDate());
					clnc.setSubscriptionDates(clinic.getSubscriptionDates());
					clnc.setSubscriptionEndDate(clinic.getSubscriptionEndDate());


					clnc.setHospitalLogo(

							clinic.getHospitalLogo() != null

									? Base64.getEncoder().encodeToString(clinic.getHospitalLogo())

									: ""

					);

					// Hospital Documents

					clnc.setHospitalDocuments(

							clinic.getHospitalDocuments() != null

									? Base64.getEncoder().encodeToString(clinic.getHospitalDocuments())

									: ""

					);

					// Contractor Documents

					clnc.setContractorDocuments(

							clinic.getContractorDocuments() != null

									? Base64.getEncoder().encodeToString(clinic.getContractorDocuments())

									: ""

					);

					// Medicines Sold On Site

					clnc.setMedicinesSoldOnSite(
							clinic.getMedicinesSoldOnSite() != null ? clinic.getMedicinesSoldOnSite() : "");

					if ("Yes".equalsIgnoreCase(clinic.getMedicinesSoldOnSite())) {

						clnc.setDrugLicenseCertificate(

								clinic.getDrugLicenseCertificate() != null

										? Base64.getEncoder().encodeToString(clinic.getDrugLicenseCertificate())

										: ""

						);

						clnc.setDrugLicenseFormType(

								clinic.getDrugLicenseFormType() != null

										? Base64.getEncoder().encodeToString(clinic.getDrugLicenseFormType())

										: ""

						);

					} else {

						clnc.setDrugLicenseCertificate("");

						clnc.setDrugLicenseFormType("");

					}

					// Pharmacist Certificate

					clnc.setHasPharmacist(clinic.getHasPharmacist() != null ? clinic.getHasPharmacist() : "");

					clnc.setPharmacistCertificate(

							"Yes".equalsIgnoreCase(clinic.getHasPharmacist())
									&& clinic.getPharmacistCertificate() != null

											? Base64.getEncoder().encodeToString(clinic.getPharmacistCertificate())

											: ""

					);
					// Extended Certifications

					clnc.setClinicType(clinic.getClinicType() != null ? clinic.getClinicType() : "");

					clnc.setClinicalEstablishmentCertificate(

							clinic.getClinicalEstablishmentCertificate() != null

									? Base64.getEncoder().encodeToString(clinic.getClinicalEstablishmentCertificate())

									: ""

					);

					clnc.setBusinessRegistrationCertificate(

							clinic.getBusinessRegistrationCertificate() != null

									? Base64.getEncoder().encodeToString(clinic.getBusinessRegistrationCertificate())

									: ""

					);

					clnc.setBiomedicalWasteManagementAuth(

							clinic.getBiomedicalWasteManagementAuth() != null

									? Base64.getEncoder().encodeToString(clinic.getBiomedicalWasteManagementAuth())

									: ""

					);

					clnc.setTradeLicense(

							clinic.getTradeLicense() != null

									? Base64.getEncoder().encodeToString(clinic.getTradeLicense())

									: ""

					);

					clnc.setFireSafetyCertificate(

							clinic.getFireSafetyCertificate() != null

									? Base64.getEncoder().encodeToString(clinic.getFireSafetyCertificate())

									: ""

					);

					clnc.setProfessionalIndemnityInsurance(

							clinic.getProfessionalIndemnityInsurance() != null

									? Base64.getEncoder().encodeToString(clinic.getProfessionalIndemnityInsurance())

									: "");
					clnc.setGstRegistrationCertificate(

							clinic.getGstRegistrationCertificate() != null

									? Base64.getEncoder().encodeToString(clinic.getGstRegistrationCertificate())

									: ""

					);

					// Others – list of documents

					List<String> othersList = new ArrayList<>();

					if (clinic.getOthers() != null) {

						for (byte[] doc : clinic.getOthers()) {

							if (doc != null) {

								othersList.add(Base64.getEncoder().encodeToString(doc));

							}

						}

					}

					clnc.setOthers(othersList);

					// Consultation Expiration

					clnc.setConsultationExpiration(

							clinic.getConsultationExpiration() != null ? clinic.getConsultationExpiration() : ""

					);

					// Social Media

					clnc.setInstagramHandle(clinic.getInstagramHandle() != null ? clinic.getInstagramHandle() : "");

					clnc.setTwitterHandle(clinic.getTwitterHandle() != null ? clinic.getTwitterHandle() : "");

					clnc.setFacebookHandle(clinic.getFacebookHandle() != null ? clinic.getFacebookHandle() : "");

					list.add(clnc);

				}
				response.setData(list);

				response.setMessage("Clinics fetched successfully");

				response.setSuccess(true);

				response.setStatus(200);

			} else {

				response.setData(null);

				response.setMessage("Clinics Not Found");

				response.setSuccess(true); // Still success, but no data

				response.setStatus(200);

			}

		} catch (Exception e) {

			response.setData(null);

			response.setMessage("Error: " + e.getMessage());

			response.setSuccess(false);

			response.setStatus(500);

		}

		return response;

	}

	@Override
	public Response updateClinic(String clinicId, ClinicDTO clinic) {

		Response response = new Response();

		try {

			Clinic savedClinic = clinicRep.findByHospitalId(clinicId);

			if (savedClinic != null) {
				if (!savedClinic.getContactNumber().equalsIgnoreCase(clinic.getContactNumber())) {
					if (clinicRep.findByContactNumber(clinic.getContactNumber()) != null) {
						response.setMessage("ContactNumber already exists");
						response.setSuccess(false);
						response.setStatus(409);
						return response;
					}
				}

				if (!savedClinic.getLicenseNumber().equalsIgnoreCase(clinic.getLicenseNumber())) {
					if (clinicRep.findByLicenseNumber(clinic.getLicenseNumber()) != null) {
						response.setMessage("LicenseNumber already exists");
						response.setSuccess(false);
						response.setStatus(409);
						return response;
					}
				}

				if (!savedClinic.getEmailAddress().equalsIgnoreCase(clinic.getEmailAddress())) {
					if (clinicRep.findByEmailAddress(clinic.getEmailAddress()) != null) {
						response.setMessage("EmailAddress already exists");
						response.setSuccess(false);
						response.setStatus(409);
						return response;
					}
				}

				if (clinic.getAddress() != null)
					savedClinic.setAddress(clinic.getAddress());

				if (clinic.getCity() != null)
					savedClinic.setCity(clinic.getCity());

				if (clinic.getName() != null) {

					savedClinic.setName(clinic.getName());

					// Update hospital name in credentials

					List<ClinicCredentials> credsList = clinicCredentialsRepository
							.findAllByUserName(savedClinic.getHospitalId());

					for (ClinicCredentials creds : credsList) {

						creds.setHospitalName(clinic.getName());

						clinicCredentialsRepository.save(creds);

					}

				}
				try{
				if (savedClinic.getBranches() != null && !savedClinic.getBranches().isEmpty()) {
			        Branch br = savedClinic.getBranches().get(0);

			        // Map fields with if-else checks
			        if (clinic.getHospitalId() != null) {
			            br.setClinicId(clinic.getHospitalId());
			        } else {
			            br.setClinicId(br.getClinicId()); // keep existing
			        }

			        if (clinic.getName()!= null) {
			            br.setHospitalName(clinic.getName());
			        } else {
			            br.setHospitalName(br.getHospitalName());
			        }			    

			        if (clinic.getAddress() != null) {
			            br.setAddress(clinic.getAddress());
			        } else {
			            br.setAddress(br.getAddress());
			        }

			        if (clinic.getCity() != null) {
			            br.setCity(clinic.getCity());
			        } else {
			            br.setCity(br.getCity());
			        }

			        if (clinic.getContactNumber() != null) {
			            br.setContactNumber(clinic.getContactNumber());
			        } else {
			            br.setContactNumber(br.getContactNumber());
			        }

			        if (clinic.getEmailAddress() != null) {
			            br.setEmail(clinic.getEmailAddress());
			        } else {
			            br.setEmail(br.getEmail());
			        }

			        if (clinic.getLatitude() != 0.0) {
			            br.setLatitude(String.valueOf(clinic.getLatitude()));
			        } else {
			            br.setLatitude(br.getLatitude());
			        }

			        if (clinic.getLongitude() != 0.0) {
			            br.setLongitude(String.valueOf(clinic.getLongitude()));
			        } else {
			            br.setLongitude(br.getLongitude());
			        }

			        if (clinic.getRole() != null) {
			            br.setRole(clinic.getRole());
			        } else {
			            br.setRole(br.getRole());
			        }

			        if (clinic.getPermissions() != null) {
			            br.setPermissions(clinic.getPermissions());
			        } else {
			            br.setPermissions(br.getPermissions());
			        }

					if (clinic.getSubscriptionStartDate() != null) {
						br.setSubscriptionStartDate(clinic.getSubscriptionStartDate());}
						if (clinic.getSubscriptionDates() != null) {
							br.setSubscriptionDates(clinic.getSubscriptionDates());}
							if (clinic.getSubscriptionEndDate() != null) {
								br.setSubscriptionEndDate(clinic.getSubscriptionEndDate());}


					if (clinic.getStatus() != null) {
			            br.setStatus(clinic.getStatus());
			        } else {
			            br.setStatus(br.getStatus());
			        }

			        if (clinic.getLoyaltyPoints() != null) {
			            br.setLoyaltyPoints(clinic.getLoyaltyPoints());
			        } else {
			            br.setLoyaltyPoints(br.getLoyaltyPoints());
			        }

                    if (clinic.getLocation()!= null || clinic.getLocation().isEmpty()) {
                        br.setLocation(clinic.getLocation());}

                    if (clinic.getWalkthrough()!= null || clinic.getWalkthrough().isEmpty()) {
                        br.setVirtualClinicTour(clinic.getWalkthrough());}

                    savedClinic.getBranches().add(br);
                    branchRepository.save(br);
			    }}catch (Exception e){}

				// Hospital Logo

				if (clinic.getHospitalLogo() != null && !clinic.getHospitalLogo().isEmpty()) {

					savedClinic.setHospitalLogo(Base64.getDecoder().decode(clinic.getHospitalLogo()));

				}

				// Hospital Documents

				if (clinic.getHospitalDocuments() != null && !clinic.getHospitalDocuments().isEmpty()) {

					savedClinic.setHospitalDocuments(Base64.getDecoder().decode(clinic.getHospitalDocuments()));

				}else{
					savedClinic.setHospitalDocuments(null);
				}

				  if (clinic.getLoyaltyPoints() != null && !clinic.getLoyaltyPoints().isBlank()) {
					  savedClinic.setLoyaltyPoints(clinic.getLoyaltyPoints());
	                }
				// Contractor Documents

				if (clinic.getContractorDocuments() != null && !clinic.getContractorDocuments().isEmpty()) {

					savedClinic.setContractorDocuments(Base64.getDecoder().decode(clinic.getContractorDocuments()));

				}else{
					savedClinic.setContractorDocuments(null);
				}

				if (clinic.getHospitalOverallRating() != 0.0) {

					savedClinic.setHospitalOverallRating(clinic.getHospitalOverallRating());

				}

				if (clinic.getClosingTime() != null)
					savedClinic.setClosingTime(clinic.getClosingTime());

				if (clinic.getOpeningTime() != null)
					savedClinic.setOpeningTime(clinic.getOpeningTime());

				if (clinic.getContactNumber() != null)
					savedClinic.setContactNumber(clinic.getContactNumber());

				if (clinic.getEmailAddress() != null)
					savedClinic.setEmailAddress(clinic.getEmailAddress());

				if (clinic.getWebsite() != null)
					savedClinic.setWebsite(clinic.getWebsite());

				if (clinic.getLicenseNumber() != null)
					savedClinic.setLicenseNumber(clinic.getLicenseNumber());

				if (clinic.getSubscription() != null && !clinic.getSubscription().isEmpty()) {

					savedClinic.setSubscription(clinic.getSubscription());
				}

				if (clinic.getIssuingAuthority() != null)
					savedClinic.setIssuingAuthority(clinic.getIssuingAuthority());

				// Optional hospital ID update (not recommended usually)

				if (clinic.getHospitalId() != null && !clinic.getHospitalId().equals(clinicId)) {

					savedClinic.setHospitalId(clinic.getHospitalId());

				}

				if (clinic.getOpeningTime() != null){
					savedClinic.setSubscriptionStartDate(clinic.getSubscriptionStartDate());}
				if (clinic.getOpeningTime() != null){
					savedClinic.setSubscriptionDates(clinic.getSubscriptionDates());}
				if (clinic.getOpeningTime() != null){
					savedClinic.setSubscriptionEndDate(clinic.getSubscriptionEndDate());}


				if (clinic.getLocation() != null || !clinic.getLocation().isEmpty()){
                    savedClinic.setLocation(clinic.getLocation());}


                // Medicines Sold On Site

				savedClinic.setMedicinesSoldOnSite(clinic.getMedicinesSoldOnSite());

				if ("Yes".equalsIgnoreCase(clinic.getMedicinesSoldOnSite())) {

					if (clinic.getDrugLicenseCertificate() != null && !clinic.getDrugLicenseCertificate().isEmpty()) {

						savedClinic.setDrugLicenseCertificate(
								Base64.getDecoder().decode(clinic.getDrugLicenseCertificate()));

					}

					if (clinic.getDrugLicenseFormType() != null && !clinic.getDrugLicenseFormType().isEmpty()) {

						savedClinic.setDrugLicenseFormType(Base64.getDecoder().decode(clinic.getDrugLicenseFormType()));

					}

				} else {

					savedClinic.setDrugLicenseCertificate(null);

					savedClinic.setDrugLicenseFormType(null);

				}

				// Pharmacist Section

				savedClinic.setHasPharmacist(clinic.getHasPharmacist());

				if ("Yes".equalsIgnoreCase(clinic.getHasPharmacist())) {

					if (clinic.getPharmacistCertificate() != null && !clinic.getPharmacistCertificate().isEmpty()) {

						savedClinic.setPharmacistCertificate(
								Base64.getDecoder().decode(clinic.getPharmacistCertificate()));

					}

				} else {

					savedClinic.setPharmacistCertificate(null);

				}

				// Other Certificates

				if (clinic.getClinicType() != null)
					savedClinic.setClinicType(clinic.getClinicType());

				if (clinic.getClinicalEstablishmentCertificate() != null
						&& !clinic.getClinicalEstablishmentCertificate().isEmpty()) {

					savedClinic.setClinicalEstablishmentCertificate(
							Base64.getDecoder().decode(clinic.getClinicalEstablishmentCertificate()));
				}else{
					savedClinic.setClinicalEstablishmentCertificate(
						null);

				}

				if (clinic.getBusinessRegistrationCertificate() != null
						&& !clinic.getBusinessRegistrationCertificate().isEmpty()) {

					savedClinic.setBusinessRegistrationCertificate(
							Base64.getDecoder().decode(clinic.getBusinessRegistrationCertificate()));
				}else{
					savedClinic.setBusinessRegistrationCertificate(
							null);
				}

				if (clinic.getBiomedicalWasteManagementAuth() != null
						&& !clinic.getBiomedicalWasteManagementAuth().isEmpty()) {

					savedClinic.setBiomedicalWasteManagementAuth(
							Base64.getDecoder().decode(clinic.getBiomedicalWasteManagementAuth()));
				}else{
					savedClinic.setBiomedicalWasteManagementAuth(
							null);
				}

				if (clinic.getTradeLicense() != null && !clinic.getTradeLicense().isEmpty()) {

					savedClinic.setTradeLicense(Base64.getDecoder().decode(clinic.getTradeLicense()));
				}else{
					savedClinic.setTradeLicense(null);
				}

				if (clinic.getFireSafetyCertificate() != null && !clinic.getFireSafetyCertificate().isEmpty()) {

					savedClinic.setFireSafetyCertificate(Base64.getDecoder().decode(clinic.getFireSafetyCertificate()));
				}else{
					savedClinic.setFireSafetyCertificate(null);
				}

				if (clinic.getProfessionalIndemnityInsurance() != null
						&& !clinic.getProfessionalIndemnityInsurance().isEmpty()) {

					savedClinic.setProfessionalIndemnityInsurance(
							Base64.getDecoder().decode(clinic.getProfessionalIndemnityInsurance()));
				}else{
					savedClinic.setProfessionalIndemnityInsurance(
						null);
				}

				if (clinic.getGstRegistrationCertificate() != null && !clinic.getGstRegistrationCertificate().isEmpty()) {

					savedClinic.setGstRegistrationCertificate(
							Base64.getDecoder().decode(clinic.getGstRegistrationCertificate()));
				}else{
					savedClinic.setGstRegistrationCertificate(
							null);
				}
				// Others - List<byte[]>

				if (clinic.getOthers() != null) {

					List<byte[]> othersList = new ArrayList<>();

					for (String base64File : clinic.getOthers()) {

						if (base64File != null && !base64File.isEmpty()) {

							othersList.add(Base64.getDecoder().decode(base64File));
						}else{
							othersList.add(null);
						}
					}
					savedClinic.setOthers(othersList);}
                if (clinic.getFreeFollowUps() != 0) {
                    savedClinic.setFreeFollowUps(clinic.getFreeFollowUps());
                }


                if (clinic.getLatitude() != 0.0) {
                    savedClinic.setLatitude(clinic.getLatitude());
                }

                if (clinic.getLongitude() != 0.0) {
                    savedClinic.setLongitude(clinic.getLongitude());
                }

                if (clinic.getWalkthrough() != null || clinic.getWalkthrough().isEmpty()) {
                    savedClinic.setWalkthrough(clinic.getWalkthrough()); // "" will be stored if client sends empty string
                }

                if (clinic.getNabhScore() != 0) {
                    savedClinic.setNabhScore(clinic.getNabhScore());
                }

                if (clinic.getBranch() != null) {
                    savedClinic.setBranch(clinic.getBranch());
                }

                if (clinic.getBranches() != null) {
                    savedClinic.setBranches(clinic.getBranches());
                }


                // Consultation Expiration

				if (clinic.getConsultationExpiration() != null && !clinic.getConsultationExpiration().isEmpty()) {

					savedClinic.setConsultationExpiration(clinic.getConsultationExpiration());

				}

				// Social Media

				if (clinic.getInstagramHandle() != null)
					savedClinic.setInstagramHandle(clinic.getInstagramHandle());

				if (clinic.getServer()!= null)
					savedClinic.setServer(clinic.getServer());


				if (clinic.getTwitterHandle() != null)
					savedClinic.setTwitterHandle(clinic.getTwitterHandle());

				if (clinic.getFacebookHandle() != null)
					savedClinic.setFacebookHandle(clinic.getFacebookHandle());

				// Recommended
				savedClinic.setRecommended(clinic.isRecommended());



				// Save updates

				clinicRep.save(savedClinic);

				response.setMessage("Clinic updated successfully");

				response.setSuccess(true);

				response.setStatus(200);

			} else {

				response.setMessage("Clinic not found for update");

				response.setSuccess(false);

				response.setStatus(404);

			}

		} catch (Exception e) {

			response.setMessage("Error occurred while updating the clinic: " + e.getMessage());

			response.setSuccess(false);

			response.setStatus(500);

		}

		return response;

	}

	@Override
	public Response deleteClinic(String clinicId) {
		Response response = new Response();

		try {
			Clinic clinic = clinicRep.findByHospitalId(clinicId);

			if (clinic != null) {

				// Delete Clinic
				clinicRep.deleteByHospitalId(clinicId);

				// Delete clinic credentials
				try {
					clinicCredentialsRepository.deleteByUserName(clinicId);
				} catch (Exception e) {
					// Ignore if credentials not found
				}

				// Delete doctors
				boolean doctorsDeleted = true;
				try {
					ResponseEntity<Response> doctorDeleteResponse = clinicAdminFeign.deleteDoctorsByClinic(clinicId);
					doctorsDeleted = doctorDeleteResponse.getStatusCode().is2xxSuccessful();
				} catch (Exception e) {
					doctorsDeleted = e.getMessage().contains("404");
				}

				// Delete branches and branch credentials
				boolean branchesDeleted = true;
				try {
					List<Branch> branches = branchRepository.findByClinicId(clinicId);
					for (Branch branch : branches) {
						String branchId = branch.getBranchId();
						branchRepository.deleteByBranchId(branchId);
						branchCredentialsRepository.deleteByBranchId(branchId);
					}
				} catch (Exception e) {
					branchesDeleted = false;
				}

			} else {
				response.setMessage("Clinic not found for deletion");
				response.setSuccess(false);
				response.setStatus(404);
			}

		} catch (Exception e) {
			response.setMessage("Error occurred while deleting the clinic: " + e.getMessage());
			response.setSuccess(false);
			response.setStatus(500);
		}

		return response;
	}

	// GENERATE RANDOM PASSWORD

	private static String generatePassword(int length) {

		if (length < 4) {

			throw new IllegalArgumentException("Password length must be at least 4.");

		}

		String upperCaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

		String lowerCaseLetters = "abcdefghijklmnopqrstuvwxyz";

		String digits = "0123456789";

		String specialChars = "!@#$&_";

		Random random = new Random();

		// First character - must be uppercase

		char firstChar = upperCaseLetters.charAt(random.nextInt(upperCaseLetters.length()));

		// Ensure at least one special character and one digit

		char specialChar = specialChars.charAt(random.nextInt(specialChars.length()));

		char digit = digits.charAt(random.nextInt(digits.length()));

		// Remaining characters pool

		String allChars = upperCaseLetters + lowerCaseLetters + digits + specialChars;

		StringBuilder remaining = new StringBuilder();

		for (int i = 0; i < length - 3; i++) {

			remaining.append(allChars.charAt(random.nextInt(allChars.length())));

		}

		// Build the password and shuffle to randomize the positions (except first char)

		List<Character> passwordChars = new ArrayList<>();

		for (char c : remaining.toString().toCharArray()) {

			passwordChars.add(c);

		}

		// Add guaranteed special and digit

		passwordChars.add(specialChar);

		passwordChars.add(digit);

		// Shuffle rest except first character

		Collections.shuffle(passwordChars);

		StringBuilder password = new StringBuilder();

		password.append(firstChar);

		for (char c : passwordChars) {

			password.append(c);

		}

		return password.toString();

	}

	// METHOD TO GENERATE SEQUANTIAL HOSPITAL ID

	public String generateHospitalId() {
		// Create a query for the counter document
		Query query = new Query();
		query.addCriteria(Criteria.where("_id").is("clinicId"));

		// Increment the sequence by 1
		Update update = new Update().inc("seq", 1);

		// Atomically find & increment, return the updated document
		FindAndModifyOptions options = FindAndModifyOptions.options().upsert(true) // create if not exists
				.returnNew(true); // return the incremented value

		Counter counter = mongoOperations.findAndModify(query, update, options, Counter.class);

		// Format as 4-digit sequential ID: 0001, 0002, ...
		return String.format("%04d", counter.getSeq());
	}

// CLINIC CREDENTIALS CRUD

	@Override

	public Response getClinicCredentials(String userName) {

		Response response = new Response();

		try {

			ClinicCredentials clinicCredentials = clinicCredentialsRepository.findByUserName(userName);

			if (clinicCredentials != null) {

				ClinicCredentialsDTO clinicCredentialsDTO = new ClinicCredentialsDTO();

				clinicCredentialsDTO.setUserName(clinicCredentials.getUserName());

				clinicCredentialsDTO.setPassword(clinicCredentials.getPassword());

				clinicCredentialsDTO.setHospitalName(clinicCredentials.getHospitalName());

				clinicCredentialsDTO.setEmail(clinicCredentials.getEmail());

				clinicCredentialsDTO.setMobilenumber(clinicCredentials.getMobilenumber());

				response.setSuccess(true);

				response.setData(clinicCredentialsDTO);

				response.setMessage("Clinic Credentials Found.");

				response.setStatus(200); // HTTP status for OK

				return response;

			} else {

				response.setSuccess(true);

				response.setMessage("Clinic Credentials Are Not Found.");

				response.setStatus(200); // HTTP status for Not Found

				return response;

			}

		} catch (Exception e) {

			response.setSuccess(false);

			response.setMessage("Error Retrieving Clinic Credentials: " + e.getMessage());

			response.setStatus(500); // Internal server error

		}

		return response;

	}

	@Override

	public Response updateClinicCredentials(UpdateClinicCredentials credentials, String userName) {

		Response response = new Response();

		try {

			ClinicCredentials existingCredentials = clinicCredentialsRepository.

					findByUserNameAndPassword(userName, credentials.getPassword());

			ClinicCredentials existUserName = clinicCredentialsRepository.findByUserName(userName);

			if (existUserName == null) {

				response.setSuccess(false);

				response.setMessage("Incorrect UserName");

				response.setStatus(401);

				return response;

			}

			if (existingCredentials != null) {

				if (credentials.getNewPassword().equalsIgnoreCase(credentials.getConfirmPassword())) {

					existingCredentials.setPassword(credentials.getNewPassword());

					ClinicCredentials c = clinicCredentialsRepository.save(existingCredentials);

					if (c != null) {

						response.setSuccess(true);

						response.setData(null);

						response.setMessage("Clinic Credentials Updated Successfully.");

						response.setStatus(200);

						return response;

					} else {

						response.setSuccess(false);

						response.setMessage("Failed To Upddate Clinic Credentials.");

						response.setStatus(404);

						return response;// HTTP status for Not Found

					}
				} else {

					response.setSuccess(false);

					response.setMessage("New password and confirm password do not match.");

					response.setStatus(401);

					return response;

				}
			} else {

				response.setSuccess(false);

				response.setMessage("Incorrect Password.");

				response.setStatus(401);

				return response;

			}
		}

		catch (Exception e) {

			response.setSuccess(false);

			response.setMessage("Error updating clinic credentials: " + e.getMessage());

			response.setStatus(500); // Internal server error

			return response;
		}

	}

	@Override
	public Response updateClinicCredentialsWithUserNameAndRole(UpdateClinicCredentialsWithUserNameAndRole credentials) {

		Response response = new Response();

		try {

			String role = credentials.getRole().trim().toUpperCase();

			// Check Username and Role
			ClinicCredentials existingCredentials = clinicCredentialsRepository
					.findByUserNameAndRole(credentials.getUsername(), role);

			if (existingCredentials == null) {
				response.setSuccess(false);
				response.setMessage("Invalid Username or Role.");
				response.setStatus(401);
				return response;
			}

			// Validate Current Password
			if (!existingCredentials.getPassword().equals(credentials.getCurrentPassword())) {
				response.setSuccess(false);
				response.setMessage("Incorrect Current Password.");
				response.setStatus(401);
				return response;
			}

			// Validate New Password and Confirm Password
			if (!credentials.getNewPassword().equals(credentials.getConfirmPassword())) {
				response.setSuccess(false);
				response.setMessage("New Password and Confirm Password do not match.");
				response.setStatus(400);
				return response;
			}

			// Optional: Prevent same password
			if (credentials.getCurrentPassword().equals(credentials.getNewPassword())) {
				response.setSuccess(false);
				response.setMessage("New Password cannot be the same as the Current Password.");
				response.setStatus(400);
				return response;
			}

			// Update Password
			existingCredentials.setPassword(credentials.getNewPassword());

			clinicCredentialsRepository.save(existingCredentials);

			response.setSuccess(true);
			response.setData(null);
			response.setMessage("Clinic Credentials Updated Successfully.");
			response.setStatus(200);

		} catch (Exception e) {
			response.setSuccess(false);
			response.setMessage("Error updating clinic credentials: " + e.getMessage());
			response.setStatus(500);
		}

		return response;
	}

	@Override
	public Response deleteClinicCredentials(String userName) {
		Response response = new Response();

		try {

			ClinicCredentials clinicCredentials = clinicCredentialsRepository.findByUserName(userName);

			if (clinicCredentials != null) {

				clinicCredentialsRepository.delete(clinicCredentials);

				clinicRep.deleteByHospitalId(userName);

				response.setSuccess(true);

				response.setMessage("Clinic Credentials Deleted Successfully.");

				response.setStatus(200); // HTTP status for OK

				return response;

			} else {

				response.setSuccess(false);

				response.setMessage("Clinic Credentials Are Not Found.");

				response.setStatus(404); // HTTP status for Not Found

				return response;

			}

		} catch (Exception e) {

			response.setSuccess(false);

			response.setMessage("Error Deleting Clinic Credentials: " + e.getMessage());

			response.setStatus(500); // Internal server error

		}

		return response;

	}

	@Override
	public Response login(ClinicCredentialsDTO credentials) {

		Response response = new Response();

		try {

			String userName = credentials.getUserName();
			String password = credentials.getPassword();

			if (userName == null || userName.isBlank()) {
				response.setSuccess(false);
				response.setMessage("Username is required");
				response.setStatus(400);
				return response;
			}

			if (password == null || password.isBlank()) {
				response.setSuccess(false);
				response.setMessage("Password is required");
				response.setStatus(400);
				return response;
			}

			// ================= CLINIC LOGIN =================
			ClinicCredentials clinicCredentials = clinicCredentialsRepository.findByUserNameIgnoreCaseAndPasswordIgnoreCase(userName,
					password);

			if (clinicCredentials != null) {

				String loginRole = credentials.getRole();

				if (credentials.getFcmToken() != null && !credentials.getFcmToken().isBlank()) {

					clinicCredentials.setFcmToken(
							updateFcmTokens(clinicCredentials.getFcmToken(), loginRole, credentials.getFcmToken()));

					clinicCredentialsRepository.save(clinicCredentials);
				}
				response.setFcmTokens(clinicCredentials.getFcmToken());

				Clinic clinicEntity = clinicRep.findByHospitalId(clinicCredentials.getUserName());

				Branch defaultBranch = branchRepository.findFirstByClinicId(clinicCredentials.getUserName());

				response.setSuccess(true);
				response.setMessage("Clinic login successful");
				response.setStatus(200);
				 response.setMainBranch(true);
				response.setLoginType("MAIN_BRANCH");
				response.setHospitalId(clinicCredentials.getUserName());

				response.setHospitalName(
						clinicEntity != null ? clinicEntity.getName() : clinicCredentials.getHospitalName());

				response.setBranchId(defaultBranch != null ? defaultBranch.getBranchId() : null);

				response.setBranchName(defaultBranch != null ? defaultBranch.getBranchName() : null);

				String role = (clinicEntity != null && clinicEntity.getRole() != null) ? clinicEntity.getRole()
						: "admin";

				response.setRole(role);

				if (clinicCredentials.getPermissions() != null
				        && clinicCredentials.getPermissions().containsKey("ADMIN")) {

				    response.setPermissions(clinicCredentials.getPermissions().get("ADMIN"));

				} else if (clinicEntity != null) {

				    response.setPermissions(clinicEntity.getPermissions());

				} else {

				    response.setPermissions(new HashMap<>());
				}
				return response;
			}else {
				// ================= BRANCH LOGIN =================
				BranchCredentials branchCredentials = branchCredentialsRepository.findByUserNameIgnoreCaseAndPasswordIgnoreCase(userName,
						password);

				if (branchCredentials != null) {

					String role = credentials.getRole();

					if (credentials.getFcmToken() != null && !credentials.getFcmToken().isBlank()) {

						branchCredentials.setFcmTokens(
								updateFcmTokens(branchCredentials.getFcmTokens(), role, credentials.getFcmToken()));

						branchCredentialsRepository.save(branchCredentials);
					}
					response.setMainBranch(false);
					response.setLoginType("BRANCH");

					response.setFcmTokens(branchCredentials.getFcmTokens());

					String branchId = branchCredentials.getBranchId();

					Optional<Branch> branchOpt = branchRepository.findByBranchId(branchId);

					Branch branchEntity = branchOpt.orElse(null);

					String clinicId;

					if (branchEntity != null && branchEntity.getClinicId() != null) {

						clinicId = branchEntity.getClinicId();

					} else {

						clinicId = branchId.length() >= 4 ? branchId.substring(0, 4) : branchId;
					}

					Clinic clinicEntity = clinicRep.findByHospitalId(clinicId);

					response.setSuccess(true);
					response.setMessage("Branch login successful");
					response.setStatus(200);

					response.setHospitalId(clinicId);

					response.setHospitalName(clinicEntity != null ? clinicEntity.getName() : "Unknown Clinic");

					response.setBranchId(branchId);

					response.setBranchName(
							branchEntity != null ? branchEntity.getBranchName() : branchCredentials.getBranchName());

					String loginrole = (branchEntity != null && branchEntity.getRole() != null) ? branchEntity.getRole()
							: "admin";

					response.setRole(role);

					if (clinicEntity != null) {
						response.setPermissions(clinicEntity.getPermissions());
					} else {
						response.setPermissions(new HashMap<>());
					}

					return response;
				}else{
				// ================= INVALID LOGIN =================
				response.setSuccess(false);
				response.setMessage("Invalid username or password");
				response.setStatus(401);
				return response;}

			}} catch (Exception e) {
			response.setSuccess(false);
			response.setMessage("Error during login: " + e.getMessage());
			response.setStatus(500);
			return response;
		}
	}

	private List<String> updateFcmTokens(List<String> existingTokens, String role, String newToken) {

		if (newToken == null || newToken.isBlank()) {
			return existingTokens;
		}

		if (existingTokens == null) {
			existingTokens = new ArrayList<>();
		}

		// Remove old token for this role
		existingTokens.removeIf(token -> token.startsWith(role + ":"));

		// Add new token for this role
		existingTokens.add(role + ":" + newToken);

		return existingTokens;
	}

//    public String findEmailByMobileNumber(String mobileNumber) {
//    	String email = null;
//    	try {
//    		ClinicCredentials obj =	clinicCredentialsRepository.findByMobilenumber(mobileNumber);
//    		if(obj != null) {
//    			email = obj.getEmail();
//    		}else {
//    			email = branchCredentialsRepository.findByMobilenumber(mobileNumber).getEmail();}
//    		}catch(Exception e) {return null;}
//    }


	// -----------------------------GET CLINICS BUY RECOMMONDATION ==
	// TRUE---------------------------------


	public Response getClinicsByRecommondation() {

		List<Clinic> clinics = clinicRep.findByRecommendedTrue();

		List<ClinicDTO> clinicsDTO = new ArrayList<>();

		for (Clinic clinic : clinics) {

			ClinicDTO toDto = new ClinicDTO();

			toDto.setHospitalId(clinic.getHospitalId());

			toDto.setName(clinic.getName());

			toDto.setAddress(clinic.getAddress());

			toDto.setCity(clinic.getCity());

			toDto.setContactNumber(clinic.getContactNumber());

			toDto.setHospitalOverallRating(clinic.getHospitalOverallRating());

			toDto.setOpeningTime(clinic.getOpeningTime());

			toDto.setClosingTime(clinic.getClosingTime());

			toDto.setEmailAddress(clinic.getEmailAddress());

			toDto.setWebsite(clinic.getWebsite());

			toDto.setLicenseNumber(clinic.getLicenseNumber());

			toDto.setIssuingAuthority(clinic.getIssuingAuthority());

			// Hospital Logo

			toDto.setHospitalLogo(

					clinic.getHospitalLogo() != null ? Base64.getEncoder().encodeToString(clinic.getHospitalLogo())

							: "");

			// Hospital Documents — single binary

			toDto.setHospitalDocuments(

					clinic.getHospitalDocuments() != null

							? Base64.getEncoder().encodeToString(clinic.getHospitalDocuments())

							: ""

			);

			toDto.setRecommended(clinic.isRecommended());

			clinicsDTO.add(toDto);

		}

		Response response = new Response();

		response.setSuccess(true);

		response.setData(clinicsDTO);

		response.setStatus(200);

		response.setMessage("Clinics Retrive successfully");

		return response;

	}

//	---------------------------get All Clincs first recommonded then another clincs----------------------------------
	@Override
	public Response getAllRecommendClinicThenAnotherClincs() {
		Response response = new Response();
		try {
			List<Clinic> clinics = clinicRep.findAllByOrderByRecommendedDescNameAsc();

			List<ClinicDTO> dtoList = clinics.stream().map(clinic -> {
				ClinicDTO dto = new ClinicDTO();

				dto.setHospitalId(clinic.getHospitalId());
				dto.setName(clinic.getName());
				dto.setAddress(clinic.getAddress());
				dto.setCity(clinic.getCity());
				dto.setHospitalOverallRating(clinic.getHospitalOverallRating());
				dto.setContactNumber(clinic.getContactNumber());
				dto.setOpeningTime(clinic.getOpeningTime());
				dto.setClosingTime(clinic.getClosingTime());

				// Convert byte[] → Base64
				dto.setHospitalLogo(
						clinic.getHospitalLogo() != null ? Base64.getEncoder().encodeToString(clinic.getHospitalLogo())
								: null);
				dto.setEmailAddress(clinic.getEmailAddress());
				dto.setWebsite(clinic.getWebsite());
				dto.setLicenseNumber(clinic.getLicenseNumber());
				dto.setIssuingAuthority(clinic.getIssuingAuthority());

				dto.setContractorDocuments(clinic.getContractorDocuments() != null
						? Base64.getEncoder().encodeToString(clinic.getContractorDocuments())
						: null);
				dto.setHospitalDocuments(clinic.getHospitalDocuments() != null
						? Base64.getEncoder().encodeToString(clinic.getHospitalDocuments())
						: null);

				dto.setRecommended(clinic.isRecommended());
				dto.setClinicalEstablishmentCertificate(clinic.getClinicalEstablishmentCertificate() != null
						? Base64.getEncoder().encodeToString(clinic.getClinicalEstablishmentCertificate())
						: null);
				dto.setBusinessRegistrationCertificate(clinic.getBusinessRegistrationCertificate() != null
						? Base64.getEncoder().encodeToString(clinic.getBusinessRegistrationCertificate())
						: null);

				dto.setClinicType(clinic.getClinicType());
				dto.setMedicinesSoldOnSite(clinic.getMedicinesSoldOnSite());
				dto.setDrugLicenseCertificate(clinic.getDrugLicenseCertificate() != null
						? Base64.getEncoder().encodeToString(clinic.getDrugLicenseCertificate())
						: null);
				dto.setDrugLicenseFormType(clinic.getDrugLicenseFormType() != null
						? Base64.getEncoder().encodeToString(clinic.getDrugLicenseFormType())
						: null);

				dto.setHasPharmacist(clinic.getHasPharmacist());
				dto.setPharmacistCertificate(clinic.getPharmacistCertificate() != null
						? Base64.getEncoder().encodeToString(clinic.getPharmacistCertificate())
						: null);

				dto.setBiomedicalWasteManagementAuth(clinic.getBiomedicalWasteManagementAuth() != null
						? Base64.getEncoder().encodeToString(clinic.getBiomedicalWasteManagementAuth())
						: null);
				dto.setTradeLicense(
						clinic.getTradeLicense() != null ? Base64.getEncoder().encodeToString(clinic.getTradeLicense())
								: null);
				dto.setFireSafetyCertificate(clinic.getFireSafetyCertificate() != null
						? Base64.getEncoder().encodeToString(clinic.getFireSafetyCertificate())
						: null);
				dto.setProfessionalIndemnityInsurance(clinic.getProfessionalIndemnityInsurance() != null
						? Base64.getEncoder().encodeToString(clinic.getProfessionalIndemnityInsurance())
						: null);
				dto.setGstRegistrationCertificate(clinic.getGstRegistrationCertificate() != null
						? Base64.getEncoder().encodeToString(clinic.getGstRegistrationCertificate())
						: null);

				dto.setConsultationExpiration(clinic.getConsultationExpiration());
				dto.setSubscription(clinic.getSubscription());

				// Convert List<byte[]> → List<String>
				dto.setOthers(clinic.getOthers() != null ? clinic.getOthers().stream()
						.map(b -> Base64.getEncoder().encodeToString(b)).collect(Collectors.toList()) : null);

				dto.setFreeFollowUps(clinic.getFreeFollowUps());
				dto.setLatitude(clinic.getLatitude());
				dto.setLongitude(clinic.getLongitude());
				dto.setNabhScore(clinic.getNabhScore());

				dto.setWalkthrough(clinic.getWalkthrough());

				dto.setInstagramHandle(clinic.getInstagramHandle());
				dto.setTwitterHandle(clinic.getTwitterHandle());
				dto.setFacebookHandle(clinic.getFacebookHandle());

				return dto;
			}).collect(Collectors.toList());

			response.setSuccess(true);
			response.setData(dtoList);
			response.setMessage("Clinics fetched successfully (Recommended first).");
			response.setStatus(200);

		} catch (Exception e) {
			response.setSuccess(false);
			response.setMessage("Error occurred while fetching clinics: " + e.getMessage());
			response.setStatus(500);
		}
		return response;
	}

	// === Helper methods ===

//	private ResponseEntity<ResponseStructure<SubServicesDto>> buildErrorResponse(String message, int statusCode) {
//		ResponseStructure<SubServicesDto> errorResponse = ResponseStructure.<SubServicesDto>builder().data(null)
//				.message(extractCleanMessage(message)).httpStatus(HttpStatus.valueOf(statusCode)).statusCode(statusCode)
//				.build();
//		return ResponseEntity.status(statusCode).body(errorResponse);
//	}

//	private ResponseEntity<ResponseStructure<List<SubServicesDto>>> buildErrorResponseList(String message,
//			int statusCode) {
//		ResponseStructure<List<SubServicesDto>> errorResponse = ResponseStructure.<List<SubServicesDto>>builder()
//				.data(null) // <-- changed from null to empty list
//				.message(extractCleanMessage(message)).httpStatus(HttpStatus.valueOf(statusCode)).statusCode(statusCode)
//				.build();
//		return ResponseEntity.status(statusCode).body(errorResponse);
//	}

	private String extractCleanMessage(String rawMessage) {
		// Try to extract the "message" value from JSON string if included
		try {
			int msgStart = rawMessage.indexOf("\"message\":\"");
			if (msgStart != -1) {
				int start = msgStart + 10;
				int end = rawMessage.indexOf("\"", start);
				return rawMessage.substring(start, end);
			}
		} catch (Exception ignored) {
		}
		return rawMessage;
	}

	// --------------------------Forgot password------------------------------------
	private static final long OTP_VALID_MILLIS = 10 * 60 * 1000; // 10 minutes

	@Override
	public Response forgotPassword(String mobileNumber, String role) {

		Response response = new Response();

		try {
			if (mobileNumber == null || mobileNumber.isBlank()) {
				response.setSuccess(false);
				response.setStatus(400);
				response.setMessage("Mobile number is required");
				return response;
			}

			if (role == null || role.isBlank()) {
				response.setSuccess(false);
				response.setStatus(400);
				response.setMessage("Role is required");
				return response;
			}

			String otp = generateOtp();
			long expiry = System.currentTimeMillis() + OTP_VALID_MILLIS;
			role = role.trim().toUpperCase();
			// ---------- Try CLINIC credentials ----------
			ClinicCredentials clinicCreds = clinicCredentialsRepository.findByMobilenumberAndRole(mobileNumber, role);
			System.out.println(clinicCreds);

			if (clinicCreds != null) {

				if (clinicCreds.getEmail() == null || clinicCreds.getEmail().isBlank()) {
					response.setSuccess(false);
					response.setStatus(404);
					response.setMessage("No email registered for this account");
					return response;
				}

				clinicCreds.setOtp(otp);
				clinicCreds.setOtpExpiryMillis(expiry);
				clinicCredentialsRepository.save(clinicCreds);

				sendOtpEmail(clinicCreds.getEmail(), otp, clinicCreds.getHospitalName());

				response.setSuccess(true);
				response.setStatus(200);
				response.setMessage("OTP sent to registered email");
				return response;
			}

			// ---------- Try BRANCH credentials ----------
			BranchCredentials branchCreds = branchCredentialsRepository.findByMobilenumberAndRole(mobileNumber, role);

			if (branchCreds != null) {

				if (branchCreds.getEmail() == null || branchCreds.getEmail().isBlank()) {
					response.setSuccess(false);
					response.setStatus(404);
					response.setMessage("No email registered for this account");
					return response;
				}

				branchCreds.setOtp(otp);
				branchCreds.setOtpExpiryMillis(expiry);
				branchCredentialsRepository.save(branchCreds);

				sendOtpEmail(branchCreds.getEmail(), otp, branchCreds.getBranchName());

				response.setSuccess(true);
				response.setStatus(200);
				response.setMessage("OTP sent to registered email");
				return response;
			}

			response.setSuccess(false);
			response.setStatus(404);
			response.setMessage("No account found with this mobile number and role");
			return response;

		} catch (Exception e) {
			response.setSuccess(false);
			response.setStatus(500);
			response.setMessage("Error sending OTP: " + e.getMessage());
			return response;
		}
	}

	@Override
	public Response verifyForgotPasswordOtp(String mobileNumber, String otp, String role) {

		Response response = new Response();
		if (mobileNumber == null || mobileNumber.isBlank()) {
			response.setSuccess(false);
			response.setStatus(400);
			response.setMessage("Mobile number is required");
			return response;
		}

		if (role == null || role.isBlank()) {
			response.setSuccess(false);
			response.setStatus(400);
			response.setMessage("Role is required");
			return response;
		}

		role = role.trim().toUpperCase();

		try {
			ClinicCredentials clinicCreds = clinicCredentialsRepository.findByMobilenumberAndRole(mobileNumber, role);

			if (clinicCreds != null) {
				return checkOtpValidity(clinicCreds.getOtp(), clinicCreds.getOtpExpiryMillis(), otp, response);
			}

			BranchCredentials branchCreds = branchCredentialsRepository.findByMobilenumberAndRole(mobileNumber, role);

			if (branchCreds != null) {
				return checkOtpValidity(branchCreds.getOtp(), branchCreds.getOtpExpiryMillis(), otp, response);
			}

			response.setSuccess(false);
			response.setStatus(404);
			response.setMessage("No account found with this mobile number and role");
			return response;

		} catch (Exception e) {
			response.setSuccess(false);
			response.setStatus(500);
			response.setMessage("Error verifying OTP: " + e.getMessage());
			return response;
		}
	}

	@Override
	public Response resetPasswordWithOtp(ResetPasswordDTO dto) {

		Response response = new Response();

		try {
			if (dto.getNewPassword() == null || !dto.getNewPassword().equals(dto.getConfirmPassword())) {
				response.setSuccess(false);
				response.setStatus(400);
				response.setMessage("New password and confirm password do not match");
				return response;
			}
			if (dto.getRole() == null || dto.getRole().isBlank()) {
				response.setSuccess(false);
				response.setStatus(400);
				response.setMessage("Role is required");
				return response;
			}

			String role = dto.getRole().trim().toUpperCase();

			ClinicCredentials clinicCreds = clinicCredentialsRepository.findByMobilenumberAndRole(dto.getMobileNumber(),
					role);

			if (clinicCreds != null) {

				Response otpCheck = checkOtpValidity(clinicCreds.getOtp(), clinicCreds.getOtpExpiryMillis(),
						dto.getOtp(), new Response());

				if (!otpCheck.isSuccess())
					return otpCheck;

				clinicCreds.setPassword(dto.getNewPassword());
				clinicCreds.setOtp(null);
				clinicCreds.setOtpExpiryMillis(null);
				clinicCredentialsRepository.save(clinicCreds);

				response.setSuccess(true);
				response.setStatus(200);
				response.setMessage("Password reset successfully");
				return response;
			}

			BranchCredentials branchCreds = branchCredentialsRepository.findByMobilenumberAndRole(dto.getMobileNumber(),
					role);

			if (branchCreds != null) {

				Response otpCheck = checkOtpValidity(branchCreds.getOtp(), branchCreds.getOtpExpiryMillis(),
						dto.getOtp(), new Response());

				if (!otpCheck.isSuccess())
					return otpCheck;

				branchCreds.setPassword(dto.getNewPassword());
				branchCreds.setOtp(null);
				branchCreds.setOtpExpiryMillis(null);
				branchCredentialsRepository.save(branchCreds);

				response.setSuccess(true);
				response.setStatus(200);
				response.setMessage("Password reset successfully");
				return response;
			}

			response.setSuccess(false);
			response.setStatus(404);
			response.setMessage("No account found with this mobile number and role");
			return response;

		} catch (Exception e) {
			response.setSuccess(false);
			response.setStatus(500);
			response.setMessage("Error resetting password: " + e.getMessage());
			return response;
		}
	}

	// ---------- helpers (unchanged) ----------

	private Response checkOtpValidity(String storedOtp, Long expiryMillis, String suppliedOtp, Response response) {

		if (storedOtp == null || expiryMillis == null) {
			response.setSuccess(false);
			response.setStatus(400);
			response.setMessage("No OTP requested for this account. Please request one first.");
			return response;
		}

		if (System.currentTimeMillis() > expiryMillis) {
			response.setSuccess(false);
			response.setStatus(410);
			response.setMessage("OTP has expired. Please request a new one.");
			return response;
		}

		if (!storedOtp.equals(suppliedOtp)) {
			response.setSuccess(false);
			response.setStatus(401);
			response.setMessage("Invalid OTP");
			return response;
		}

		response.setSuccess(true);
		response.setStatus(200);
		response.setMessage("OTP verified successfully");
		return response;
	}

	private String generateOtp() {
		Random random = new Random();
		int otp = 100000 + random.nextInt(900000); // always 6 digits
		return String.valueOf(otp);
	}

	private void sendOtpEmail(String email, String otp, String accountName) {
	    Clinic clinic = clinicRep.findByEmailAddress(email);
	    String clinicName = (clinic != null && clinic.getName() != null && !clinic.getName().isBlank())
	            ? clinic.getName()
	            : accountName; // fallback if no clinic record matches this email

	    emailService.sendForgotPasswordOtp(email, otp, accountName, clinicName);
	}
	
	
	public List<String> getFcmTokens(String cId,String bId){
		try {
			ClinicCredentials c = clinicCredentialsRepository.findByUserName(cId);
			if(c  != null) {
			///	System.out.println(c);
				return c.getFcmToken();
			}else {
				BranchCredentials b =	branchCredentialsRepository.findByUserName(bId);
				if(b != null) {
					return b.getFcmTokens();
				}else {
					return null;
				}
			}
		}catch (Exception e) {
			return null;
		}
	}


    @Override
    public ResponseStructure<List<BookingResponse>> getAllBookedServices() {
        try {
            ResponseEntity<ResponseStructure<List<BookingResponse>>> res = bookingFeign.getAllBookedService();
            return res.getBody(); // return exactly what BookingService sends
        } catch (FeignException e) {
            throw e; // propagate exception to controller
        }
    }

	public int getFreeFollowUpsByHospitalId(String hospitalId) {
		Clinic clinic = clinicRep.findByHospitalId(hospitalId);
		if (clinic == null) {
			return 0;
		}
		return clinic.getFreeFollowUps();
	}

}