package com.clinicadmin.service.impl;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.clinicadmin.dto.BookingInfoByInput;
import com.clinicadmin.dto.CustomerLoginDTO;
import com.clinicadmin.dto.CustomerOnbordingDTO;
import com.clinicadmin.dto.CustomerResponseDTO;
import com.clinicadmin.dto.Response;
import com.clinicadmin.entity.CustomerCredentials;
import com.clinicadmin.entity.CustomerOnbording;
import com.clinicadmin.repository.CustomerCredentialsRepository;
import com.clinicadmin.repository.CustomerOnboardingRepository;
import com.clinicadmin.service.CustomerOnboardingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.Row;
import feign.FeignException;

@Service
public class CustomerOnboardingServiceImpl implements CustomerOnboardingService {

	@Autowired
	private CustomerOnboardingRepository onboardingRepository;

	@Autowired
	private CustomerCredentialsRepository credentialsRepository;

	@Autowired
	private SequenceGeneratorService sequenceGeneratorService;

//	@Autowired
//	private MongoTemplate mongoTemplate;

	private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	// ----------------- CREATE (ONBOARD) -----------------
	@Override
	public Response onboardCustomer(CustomerOnbordingDTO dto) {
		Response response = new Response();

		try {
//			Optional<CustomerOnbording> existingCustomer =
//					onboardingRepository.findByMobileNumber(dto.getMobileNumber());

//			if (existingCustomer.isPresent()) {
//				response.setSuccess(false);
//				response.setMessage("Mobile number already exists");
//				response.setStatus(400);
//				return response;
//			}
			// Generate unique IDs
			long customerSeq = sequenceGeneratorService.getNextSequence(dto.getBranchId() + "_customer");
			long patientSeq = sequenceGeneratorService.getNextSequence(dto.getBranchId() + "_patient");

			String customerId = dto.getBranchId() + "_CR_" + String.format("%05d", customerSeq);
			String patientId = dto.getBranchId() + "_PT_" + String.format("%05d", patientSeq);

			// Generate Referral Code
			String prefix = dto.getFullName().replaceAll("\\s+", "").toUpperCase().substring(0,
					Math.min(3, dto.getFullName().length()));

			long referralSeq = sequenceGeneratorService.getNextSequence("referral_code_seq");
			String referralCode = prefix + "_" + String.format("%05d", referralSeq);

			// Convert DTO -> Entity
			CustomerOnbording entity = convertToEntity(dto);
			entity.setCustomerId(customerId);
			entity.setPatientId(patientId);
			entity.setReferralCode(referralCode);

			onboardingRepository.save(entity);

			// Save credentials
			CustomerCredentials credentials = new CustomerCredentials();
			credentials.setUserName(customerId);
			credentials.setPassword(passwordEncoder.encode(dto.getMobileNumber())); // mobile = default password
			credentials.setHospitalId(dto.getHospitalId());
			credentials.setBranchId(dto.getBranchId());
			credentials.setHospitalName(dto.getHospitalName());
			credentialsRepository.save(credentials);

			CustomerOnbordingDTO resDTO = convertToDTO(entity);
			resDTO.setUserName(customerId);
			resDTO.setPassword(dto.getMobileNumber());

			response.setSuccess(true);
			response.setMessage("Customer onboarded successfully");
			response.setData(resDTO);
			response.setStatus(201);

		} catch (Exception e) {
			response.setSuccess(false);
			response.setMessage("Error during onboarding: " + e.getMessage());
			response.setStatus(500);
		}

		return response;
	}

	// ----------------- READ ALL -----------------
	@Override
	public Response getAllCustomers() {
		Response response = new Response();
		try {
			List<CustomerOnbordingDTO> customers = onboardingRepository.findAll().stream().map(this::convertToDTO)
					.collect(Collectors.toList());

			response.setSuccess(true);
			response.setMessage(customers.isEmpty() ? "No customers found" : "Customers retrieved successfully");
			response.setData(customers);
			response.setStatus(200);
		} catch (Exception e) {
			response.setSuccess(false);
			response.setMessage("Error fetching customers: " + e.getMessage());
			response.setStatus(500);
		}
		return response;
	}

	@Override
	public Response getCustomerById(String id) {
		Response response = new Response();
		try {
			Optional<CustomerOnbording> optional = onboardingRepository.findByCustomerId(id);
			if (optional.isPresent()) {
				response.setSuccess(true);
				response.setMessage("Customer found successfully");
				response.setData(convertToDTO(optional.get()));
				response.setStatus(200);
			} else {
				response.setSuccess(false);
				response.setMessage("Customer not found with ID: " + id);
				response.setStatus(404);
			}
		} catch (Exception e) {
			response.setSuccess(false);
			response.setMessage("Error fetching customer: " + e.getMessage());
			response.setStatus(500);
		}
		return response;
	}

	@Override
	public Map<String, String> getCustomerByMobilenumberAndName(String mobilenumber, String name) {
		Map<String, String> details = new LinkedHashMap<>();
		try {
			Optional<CustomerOnbording> optional = onboardingRepository.findByMobileNumberAndFullName(mobilenumber,
					name);
			if (optional.isPresent()) {
				details.put("customerId", optional.get().getCustomerId());
				details.put("patientId", optional.get().getPatientId());
				//// System.out.println(details);
				return details;
			} else {
				return null;
			}
		} catch (Exception e) {
			return null;
		}

	}

	@Override
	public Response getCustomerByMobiileNumber(String mobilenumber) {
		Response response = new Response();
		try {
			List<CustomerOnbording> customers = onboardingRepository.findByMobileNumber(mobilenumber);

			if (customers != null && !customers.isEmpty()) {
				List<CustomerOnbordingDTO> dtoList = customers.stream().map(this::convertToDTO)
						.collect(Collectors.toList());

				response.setSuccess(true);
				response.setMessage("Customer(s) found successfully");
				response.setData(dtoList);
				response.setStatus(200);
			} else {
				response.setSuccess(false);
				response.setMessage("Customer not found with mobile number: " + mobilenumber);
				response.setStatus(404);
			}
		} catch (Exception e) {
			response.setSuccess(false);
			response.setMessage("Error fetching customer: " + e.getMessage());
			response.setStatus(500);
		}
		return response;
	}
//	@Override
//	public Response getCustomerByMobiileNumber(String mobilenumber) {
//		Response response = new Response();
//		try {
//			Optional<CustomerOnbording> optional = onboardingRepository.findByMobileNumber(mobilenumber);
//			if (optional.isPresent()) {
//				response.setSuccess(true);
//				response.setMessage("Customer found successfully");
//				response.setData(convertToDTO(optional.get()));
//				response.setStatus(200);
//			} else {
//				response.setSuccess(false);
//				response.setMessage("Customer not found with ID: " + mobilenumber);
//				response.setStatus(404);
//			}
//		} catch (Exception e) {
//			response.setSuccess(false);
//			response.setMessage("Error fetching customer: " + e.getMessage());
//			response.setStatus(500);
//		}
//		return response;
//	}

	@Override
	public CustomerOnbordingDTO getCustomerByMobileNumberAndClinicId(String mobileNumber, String hospitalId) {
	    try {
			if (mobileNumber == null || mobileNumber.length() < 3) {
				throw new IllegalArgumentException(
						"Mobile number must contain at least 3 digits"
				);
			}

			List<CustomerOnbording> customers =
					onboardingRepository
							.findByMobileNumberStartingWithAndHospitalId(
									mobileNumber,
									hospitalId
							);
	         if (customers != null && !customers.isEmpty()) {
	            return convertToDTO(customers.get(0));
	        } else {
	            return null;
	        }
	    } catch (Exception e) {
	        return null;
	    }
	}
	// ----------------- UPDATE -----------------
	@Override
	public Response updateCustomer(String customerId, CustomerOnbordingDTO dto) {
		Response response = new Response();

		try {
			Optional<CustomerOnbording> optional = onboardingRepository.findByCustomerId(customerId);
			if (optional.isEmpty()) {
				response.setSuccess(false);
				response.setMessage("Customer not found");
				response.setStatus(404);
				return response;
			}

			CustomerOnbording entity = optional.get();

			// Null checks before updating fields
			if (dto.getFullName() != null && !dto.getFullName().isBlank()) {
				entity.setFullName(dto.getFullName());
			}
			if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
				entity.setEmail(dto.getEmail());
			}
			if (dto.getCountryCode() != null && !dto.getCountryCode().isBlank()) {
				entity.setMobileNumber(dto.getCountryCode());
			}
			if (dto.getMobileNumber() != null && !dto.getMobileNumber().isBlank()) {
				entity.setMobileNumber(dto.getMobileNumber());
			}

			if (dto.getAge() != null && !dto.getAge().isBlank()) {
				entity.setAge(dto.getAge());
			}
			if (dto.getAddress() != null) {
				entity.setAddress(dto.getAddress());
			}
			if (dto.getHospitalId() != null && !dto.getHospitalId().isBlank()) {
				entity.setHospitalId(dto.getHospitalId());
			}
			if (dto.getHospitalName() != null && !dto.getHospitalName().isBlank()) {
				entity.setHospitalName(dto.getHospitalName());
			}
			if (dto.getBranchId() != null && !dto.getBranchId().isBlank()) {
				entity.setBranchId(dto.getBranchId());
			}
			if (dto.getCustomerId() != null && !dto.getCustomerId().isBlank()) {
				entity.setCustomerId(dto.getCustomerId());
			}
			if (dto.getPatientId() != null && !dto.getPatientId().isBlank()) {
				entity.setPatientId(dto.getPatientId());
			}
			if (dto.getGender() != null && !dto.getGender().isBlank()) {
				entity.setGender(dto.getGender());
			}

			entity.setUpdatedDate(LocalDate.now().toString());

			onboardingRepository.save(entity);

			response.setSuccess(true);
			response.setMessage("Customer updated successfully");
			response.setData(convertToDTO(entity));
			response.setStatus(200);

		} catch (Exception e) {
			response.setSuccess(false);
			response.setMessage("Error updating customer: " + e.getMessage());
			response.setStatus(500);
		}

		return response;
	}

	// ----------------- DELETE -----------------
	@Override
	public Response deleteCustomer(String id) {
		Response response = new Response();

		try {
			Optional<CustomerOnbording> optional = onboardingRepository.findByCustomerId(id);
			if (optional.isEmpty()) {
				response.setSuccess(false);
				response.setMessage("Customer not found");
				response.setStatus(404);
				return response;
			}

			CustomerOnbording entity = optional.get();
			onboardingRepository.deleteByCustomerId(id);

			// Delete credentials also
			credentialsRepository.deleteByUserName(entity.getCustomerId());

			response.setSuccess(true);
			response.setMessage("Customer deleted successfully");
			response.setStatus(200);

		} catch (Exception e) {
			response.setSuccess(false);
			response.setMessage("Error deleting customer: " + e.getMessage());
			response.setStatus(500);
		}

		return response;
	}

	@Override
	public Response getCustomersByHospitalId(String hospitalId, String branchId) {
		Response response = new Response();
		try {
			List<CustomerOnbordingDTO> customers = onboardingRepository
					.findByHospitalIdAndBranchId(hospitalId, branchId).stream().map(this::convertToDTO)
					.collect(Collectors.toList());

			// ✅ Keep only the last 10
			if (customers.size() > 10) {
				customers = customers.subList(customers.size() - 10, customers.size());
			}

			response.setSuccess(true);
			response.setMessage(customers.isEmpty() ? "No customers found for hospitalId: " + hospitalId
					: "Last " + customers.size() + " customers retrieved successfully");
			response.setData(customers);
			response.setStatus(200);
		} catch (Exception e) {
			response.setSuccess(false);
			response.setMessage("Error fetching customers: " + e.getMessage());
			response.setStatus(500);
		}
		return response;
	}

	@Override
	public Response getCustomersByHospitalId(String clinicId, String branchId, String searchInput) {
		Response response = new Response();
		try {
			// Fetch all customers for clinic + branch
			List<CustomerOnbordingDTO> customers = onboardingRepository.findByHospitalIdAndBranchId(clinicId, branchId)
					.stream().map(this::convertToDTO).collect(Collectors.toList());

			// ✅ Detect input type and filter
			if (searchInput != null && !searchInput.isBlank()) {
				String lowerSearch = searchInput.toLowerCase();

				if (searchInput.matches("\\d+")) {
					// Input is numeric → treat as mobile number
					customers = customers.stream()
							.filter(c -> c.getMobileNumber() != null && c.getMobileNumber().contains(searchInput))
							.collect(Collectors.toList());

				} else if (searchInput.matches("\\d+_PT_\\d+")) {
					// Input matches patientId format (e.g. 000201_PT_00004)
					customers = customers.stream()
							.filter(c -> c.getPatientId() != null && c.getPatientId().equalsIgnoreCase(searchInput))
							.collect(Collectors.toList());

				} else {
					// Otherwise treat as name (ignore case)
					customers = customers.stream()
							.filter(c -> c.getFullName() != null && c.getFullName().toLowerCase().contains(lowerSearch))
							.collect(Collectors.toList());
				}
			}

			response.setSuccess(true);
			response.setMessage(
					customers.isEmpty() ? "No customers found for clinicId: " + clinicId + " and branchId: " + branchId
							: "Customers retrieved successfully");
			response.setData(customers);
			response.setStatus(200);

		} catch (Exception e) {
			response.setSuccess(false);
			response.setMessage("Error fetching customers: " + e.getMessage());
			response.setStatus(500);
		}
		return response;
	}

	@Override
	public Response getCustomersByPatientId(String patientId, String clinicId) {
		Response response = new Response();
		try {
			CustomerOnbording customers = onboardingRepository.findByPatientIdAndHospitalId(patientId, clinicId);
			// System.out.println(customers);
			if (customers != null) {
				response.setSuccess(true);
				response.setMessage("Customers retrieved successfully");
				response.setData(new ObjectMapper().convertValue(customers, CustomerOnbordingDTO.class));
				response.setStatus(200);
			} else {
				response.setSuccess(false);
				response.setMessage("Customers Object Not Found");
				response.setStatus(200);
			}
		} catch (Exception e) {
			response.setSuccess(false);
			response.setMessage("Error fetching customers: " + e.getMessage());
			response.setStatus(500);
		}
		return response;
	}

	@Override
	public Response getCustomersByBranchId(String branchId) {
		Response response = new Response();
		try {
			List<CustomerOnbordingDTO> customers = onboardingRepository.findByBranchId(branchId).stream()
					.map(this::convertToDTO).collect(Collectors.toList());

			response.setSuccess(true);
			response.setMessage(customers.isEmpty() ? "No customers found for branchId: " + branchId
					: "Customers retrieved successfully");
			response.setData(customers);
			response.setStatus(200);
		} catch (Exception e) {
			response.setSuccess(false);
			response.setMessage("Error fetching customers: " + e.getMessage());
			response.setStatus(500);
		}
		return response;
	}

	@Override
	public Response getCustomersByHospitalIdAndBranchId(String hospitalId, String branchId) {
		Response response = new Response();
		try {
			List<CustomerOnbordingDTO> customers = onboardingRepository
					.findByHospitalIdAndBranchId(hospitalId, branchId).stream().map(this::convertToDTO)
					.collect(Collectors.toList());

			response.setSuccess(true);
			response.setMessage(customers.isEmpty()
					? "No customers found for hospitalId: " + hospitalId + " and branchId: " + branchId
					: "Customers retrieved successfully");
			response.setData(customers);
			response.setStatus(200);
		} catch (Exception e) {
			response.setSuccess(false);
			response.setMessage("Error fetching customers: " + e.getMessage());
			response.setStatus(500);
		}
		return response;
	}

	// ----------------- LOGIN -----------------
	@Override
	public Response login(CustomerLoginDTO dto) {
		Response response = new Response();

		try {
			Optional<CustomerCredentials> optional = credentialsRepository.findByUserName(dto.getUserName());
			if (optional.isEmpty()) {
				response.setSuccess(false);
				response.setMessage("Invalid username");
				response.setStatus(401);
				return response;
			}

			CustomerCredentials credentials = optional.get();

			// check password
			if (!passwordEncoder.matches(dto.getPassword(), credentials.getPassword())) {
				response.setSuccess(false);
				response.setMessage("Invalid password");
				response.setStatus(401);
				return response;
			}

			// fetch customer onboarding details using userName (or customerId if you store
			// it in credentials)
			Optional<CustomerOnbording> customerOpt = onboardingRepository.findByCustomerId(credentials.getUserName());

			if (customerOpt.isEmpty()) {
				response.setSuccess(false);
				response.setMessage("Customer profile not found");
				response.setStatus(404);
				return response;
			}

			CustomerOnbording customer = customerOpt.get();

			customer.setDeviceId(dto.getDeviceId());
			CustomerOnbording cs = onboardingRepository.save(customer);

			// map to response DTO
			CustomerResponseDTO resDTO = new CustomerResponseDTO();
			resDTO.setUserName(credentials.getUserName());
			resDTO.setCustomerName(customer.getFullName());
			resDTO.setCustomerId(customer.getCustomerId());
			resDTO.setPatientId(customer.getPatientId());
			resDTO.setDeviceId(cs.getDeviceId());
			resDTO.setHospitalName(customer.getHospitalName());
			resDTO.setHospitalId(customer.getHospitalId());
			resDTO.setBranchId(customer.getBranchId());

			// final response
			response.setSuccess(true);
			response.setMessage("Login successful");
			response.setData(resDTO);
			response.setStatus(200);

		} catch (Exception e) {
			response.setSuccess(false);
			response.setMessage("Login error: " + e.getMessage());
			response.setStatus(500);
		}

		return response;
	}

	public String customerDeviceId(String customerId) {
		try {
			Optional<CustomerOnbording> cs = onboardingRepository.findByCustomerId(customerId);
			if (cs.isPresent()) {
				return cs.get().getDeviceId();
			} else {
				return null;
			}
		} catch (Exception e) {
			return null;
		}
	}
	// ----------------- RESET PASSWORD -----------------
//	@Override
//	public Response resetPassword(ChangeDoctorPasswordDTO dto) {
//		Response response = new Response();
//
//		try {
//			Optional<CustomerCredentials> optional = credentialsRepository.findByUserName(dto.getUserName());
//			if (optional.isEmpty()) {
//				response.setSuccess(false);
//				response.setMessage("Invalid username");
//				response.setStatus(404);
//				return response;
//			}
//
//			CustomerCredentials credentials = optional.get();
//
//			if (!passwordEncoder.matches(dto.getCurrentPassword(), credentials.getPassword())) {
//				response.setSuccess(false);
//				response.setMessage("Old password is incorrect");
//				response.setStatus(400);
//				return response;
//			}
//
//			credentials.setPassword(passwordEncoder.encode(newPassword));
//			credentialsRepository.save(credentials);
//
//			response.setSuccess(true);
//			response.setMessage("Password updated successfully");
//			response.setStatus(200);
//
//		} catch (Exception e) {
//			response.setSuccess(false);
//			response.setMessage("Reset password error: " + e.getMessage());
//			response.setStatus(500);
//		}
//
//		return response;
//	}

	private String getIndianDateTime() {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss a");

		return LocalDateTime.now(ZoneId.of("Asia/Kolkata")).format(formatter);
	}

	// ------------------ DTO ↔ Entity Conversion ------------------
	private CustomerOnbording convertToEntity(CustomerOnbordingDTO dto) {

		CustomerOnbording entity = new CustomerOnbording();
        entity.setCountryCode(dto.getCountryCode());
		entity.setId(dto.getId());
		entity.setMobileNumber(dto.getMobileNumber());
		entity.setEmail(dto.getEmail());
		entity.setFullName(dto.getFullName());
		entity.setAge(dto.getAge());
		entity.setAddress(dto.getAddress());

		entity.setHospitalId(dto.getHospitalId());
		entity.setHospitalName(dto.getHospitalName());
		entity.setBranchId(dto.getBranchId());

		entity.setGender(dto.getGender());
		entity.setCustomerId(dto.getCustomerId());
		entity.setPatientId(dto.getPatientId());

//	    entity.setDeviceId(dto.getDeviceId());

		entity.setReferralCode(dto.getReferralCode());
		entity.setReferredBy(dto.getReferredBy());

		entity.setCreatedBy(dto.getCreatedBy());

		// Store Indian Date & Time
		entity.setCreatedAt(getIndianDateTime());

		return entity;
	}

	private CustomerOnbordingDTO convertToDTO(CustomerOnbording entity) {

		CustomerOnbordingDTO dto = new CustomerOnbordingDTO();

		dto.setId(entity.getId());
		dto.setCountryCode(entity.getCountryCode());
		dto.setMobileNumber(entity.getMobileNumber());
		dto.setEmail(entity.getEmail());
		dto.setFullName(entity.getFullName());

		dto.setAge(entity.getAge());
		dto.setAddress(entity.getAddress());

		dto.setGender(entity.getGender());
		dto.setHospitalId(entity.getHospitalId());
		dto.setHospitalName(entity.getHospitalName());
		dto.setBranchId(entity.getBranchId());

		dto.setCustomerId(entity.getCustomerId());
		dto.setPatientId(entity.getPatientId());
//
//	    dto.setDeviceId(entity.getDeviceId());

		dto.setReferralCode(entity.getReferralCode());
		dto.setReferredBy(entity.getReferredBy());

		dto.setCreatedBy(entity.getCreatedBy());
		dto.setCreatedAt(entity.getCreatedAt());
		dto.setUpdatedDate(entity.getUpdatedDate());

		return dto;
	}

	public CustomerOnbordingDTO getCustomerByToken(String token) {
		try {
			CustomerOnbording cstmr = onboardingRepository.findByDeviceId(token);
			if (cstmr != null) {
				CustomerOnbordingDTO cusmrdto = new ObjectMapper().convertValue(cstmr, CustomerOnbordingDTO.class);
				return cusmrdto;
			} else {
				return null;
			}
		} catch (FeignException e) {
			return null;
		}
	}

	public String getCustomername(String patientId) {
		try {
			CustomerOnbording cstmr = onboardingRepository.findByPatientId(patientId);
			if (cstmr != null) {
				return cstmr.getFullName();
			} else {
				return null;
			}
		} catch (FeignException e) {
			return null;
		}
	}

	@Override
	public List<BookingInfoByInput> customersByInput(String input, String clinicId) {
		BookingInfoByInput bkng = new BookingInfoByInput();
		CustomerOnbordingDTO b = null;
		List<BookingInfoByInput> lst = new ArrayList<>();
		List<CustomerOnbordingDTO> customerOnbordingDTO = null;
		try {
			b = getCustomerByMobileNumberAndClinicId(input, clinicId);
			// Sysout
			if (b != null) {
				bkng.setAge(b.getAge());
				bkng.setClinicId(b.getHospitalId());
				bkng.setBranchId(b.getBranchId());
				bkng.setCustomerId(b.getCustomerId());
				bkng.setGender(b.getGender());
				bkng.setMobileNumber(b.getMobileNumber());
				bkng.setName(b.getFullName());
				bkng.setPatientAddress(b.getAddress());
				bkng.setPatientId(b.getPatientId());
				bkng.setPatientMobileNumber(b.getMobileNumber());

				bkng.setEmail(b.getEmail());
				bkng.setRelation(null);
				lst.add(bkng);
			}
			if (b == null) {
				Response res = getCustomersByPatientId(input, clinicId);
				b = new ObjectMapper().convertValue(res.getData(), CustomerOnbordingDTO.class);
				if (b != null) {
					bkng.setAge(b.getAge());
					bkng.setEmail(b.getEmail());
					bkng.setClinicId(b.getHospitalId());
					bkng.setBranchId(b.getBranchId());
					bkng.setCustomerId(b.getCustomerId());
					bkng.setGender(b.getGender());
					bkng.setMobileNumber(b.getMobileNumber());
					bkng.setName(b.getFullName());
					bkng.setPatientAddress(b.getAddress());
					bkng.setPatientId(b.getPatientId());
					bkng.setPatientMobileNumber(b.getMobileNumber());

					bkng.setRelation(null);
					lst.add(bkng);
				}
			}
			if (b == null) {
				customerOnbordingDTO = onboardingRepository.findByFullNameContainingIgnoreCaseAndHospitalId(input,
						clinicId);
				/// System.out.println(customerOnbordingDTO);
				for (CustomerOnbordingDTO dto : customerOnbordingDTO) {
					BookingInfoByInput bookingInfoByInput = new BookingInfoByInput();
					bookingInfoByInput.setAge(dto.getAge());
					bookingInfoByInput.setEmail(dto.getEmail());
					bookingInfoByInput.setBranchId(dto.getBranchId());
					bookingInfoByInput.setClinicId(dto.getHospitalId());
					bookingInfoByInput.setCustomerId(dto.getCustomerId());
					bookingInfoByInput.setGender(dto.getGender());
					bookingInfoByInput.setMobileNumber(dto.getMobileNumber());
					bookingInfoByInput.setName(dto.getFullName());
					bookingInfoByInput.setPatientAddress(dto.getAddress());
					bookingInfoByInput.setPatientId(dto.getPatientId());
					bookingInfoByInput.setPatientMobileNumber(dto.getMobileNumber());

					bookingInfoByInput.setRelation(null);
					lst.add(bookingInfoByInput);
				}
			}
		} catch (Exception e) {
			// System.err.println("Error fetching bookings: " + e.getMessage());
			//System.out.println(e.getMessage());
			; // safe fallback
		}
		return lst;
	}

	@Override
	public Response importCustomersFromExcel(MultipartFile file) {

		Response response = new Response();

		int totalRows = 0;
		int savedRows = 0;
		int duplicateRows = 0;
		int failedRows = 0;

		// Store exact customers which failed to import
		List<Map<String, Object>> failedCustomers = new ArrayList<>();

		// Store rows that were saved but shared a mobile number with an existing
		// customer
		List<Map<String, Object>> duplicateNotices = new ArrayList<>();

		try {

			// =====================================================
			// 1. FILE VALIDATION
			// =====================================================

			if (file == null || file.isEmpty()) {

				response.setSuccess(false);
				response.setMessage("Excel file is required");
				response.setStatus(400);

				return response;
			}

			String fileName = file.getOriginalFilename();

			if (fileName == null
					|| (!fileName.toLowerCase().endsWith(".xlsx") && !fileName.toLowerCase().endsWith(".xls"))) {

				response.setSuccess(false);
				response.setMessage("Only Excel files (.xlsx or .xls) are allowed");
				response.setStatus(400);

				return response;
			}

			// =====================================================
			// 2. READ EXCEL FILE
			// =====================================================

			try (InputStream inputStream = file.getInputStream();
					Workbook workbook = WorkbookFactory.create(inputStream)) {

				Sheet sheet = workbook.getSheetAt(0);

				// =================================================
				// 3. SKIP HEADER ROW
				// =================================================

				for (int i = 1; i <= sheet.getLastRowNum(); i++) {

					Row row = sheet.getRow(i);

					if (row == null) {
						continue;
					}

					totalRows++;

					// Excel row number for user
					int excelRowNumber = i + 1;

					// Variables declared outside try so that
					// they can be used in catch
					String fullName = "";
					String mobileNumber = "";

					try {

						// =============================================
						// 4. READ EXCEL COLUMNS
						// =============================================

						fullName = getCellValue(row.getCell(0));

						mobileNumber = getCellValue(row.getCell(1));

						String gender = getCellValue(row.getCell(2));

						String email = getCellValue(row.getCell(3));

						String age = getCellValue(row.getCell(4));

						String address = getCellValue(row.getCell(5));

						String hospitalId = getCellValue(row.getCell(6));

						String hospitalName = getCellValue(row.getCell(7));

						String branchId = getCellValue(row.getCell(8));

						// =============================================
						// 5. VALIDATE FULL NAME
						// =============================================

						if (fullName == null || fullName.isBlank()) {

							failedRows++;

							addFailedCustomer(failedCustomers, excelRowNumber, fullName, mobileNumber,
									"Full name is required");

							continue;
						}

						// =============================================
						// 6. VALIDATE MOBILE NUMBER
						// =============================================

						if (mobileNumber == null || mobileNumber.isBlank()) {

							failedRows++;

							addFailedCustomer(failedCustomers, excelRowNumber, fullName, mobileNumber,
									"Mobile number is required");

							continue;
						}

						// =============================================
						// 7. VALIDATE HOSPITAL ID
						// =============================================

						if (hospitalId == null || hospitalId.isBlank()) {

							failedRows++;

							addFailedCustomer(failedCustomers, excelRowNumber, fullName, mobileNumber,
									"Hospital ID is required");

							continue;
						}

						// =============================================
						// 8. VALIDATE BRANCH ID
						// =============================================

						if (branchId == null || branchId.isBlank()) {

							failedRows++;

							addFailedCustomer(failedCustomers, excelRowNumber, fullName, mobileNumber,
									"Branch ID is required");

							continue;
						}

						// =============================================
						// 9. CHECK DUPLICATE MOBILE NUMBER
						// (informational only — does NOT block import)
						// =============================================

						List<CustomerOnbording> existingCustomer = onboardingRepository
								.findByMobileNumber(mobileNumber);

						boolean isDuplicateMobile = existingCustomer != null && !existingCustomer.isEmpty();

						if (isDuplicateMobile) {
							duplicateRows++;
						}

						// =============================================
						// 10. CREATE CUSTOMER DTO
						// =============================================

						CustomerOnbordingDTO dto = new CustomerOnbordingDTO();

						dto.setFullName(fullName);
						dto.setMobileNumber(mobileNumber);
						dto.setGender(gender);
						dto.setEmail(email);
						dto.setAge(age);
						dto.setAddress(address);

						dto.setHospitalId(hospitalId);
						dto.setHospitalName(hospitalName);
						dto.setBranchId(branchId);

						// =============================================
						// 11. USE EXISTING ONBOARDING METHOD
						// =============================================

						Response onboardResponse = onboardCustomer(dto);

						// =============================================
						// 12. CHECK ONBOARDING RESULT
						// =============================================

						if (onboardResponse != null && onboardResponse.getStatus() == 201) {

							savedRows++;

							// Row saved successfully, but flag it if it shared a
							// mobile number with an existing customer.
							if (isDuplicateMobile) {
								addFailedCustomer(duplicateNotices, excelRowNumber, fullName, mobileNumber,
										"Saved, but mobile number already exists for another customer");
							}

						} else {

							failedRows++;

							String reason = onboardResponse != null && onboardResponse.getMessage() != null
									? onboardResponse.getMessage()
									: "Customer onboarding failed";

							addFailedCustomer(failedCustomers, excelRowNumber, fullName, mobileNumber, reason);
						}

					} catch (Exception e) {

						failedRows++;

						addFailedCustomer(failedCustomers, excelRowNumber, fullName, mobileNumber,
								e.getMessage() != null ? e.getMessage() : "Unexpected error");

						System.out.println("Excel row " + excelRowNumber + " failed: " + e.getMessage());

						e.printStackTrace();
					}
				}
			}

			// =====================================================
			// 13. IMPORT SUMMARY
			// =====================================================

			Map<String, Object> result = new LinkedHashMap<>();

			result.put("totalRows", totalRows);

			result.put("saved", savedRows);

			result.put("duplicates", duplicateRows);

			result.put("failed", failedRows);

			// Exact failed customer details
			result.put("failedCustomers", failedCustomers);

			// Rows saved despite sharing a mobile number with an existing customer
			result.put("duplicateCustomers", duplicateNotices);

			// =====================================================
			// 14. FINAL RESPONSE
			// =====================================================

			response.setSuccess(true);

			response.setMessage("Customer Excel import completed successfully");

			response.setData(result);

			response.setStatus(200);

		} catch (Exception e) {

			response.setSuccess(false);

			response.setMessage("Error importing Excel: " + e.getMessage());

			response.setStatus(500);
		}

		return response;
	}

	private void addFailedCustomer(

			List<Map<String, Object>> failedCustomers, int excelRow, String fullName, String mobileNumber,
			String reason) {

		Map<String, Object> failedCustomer = new LinkedHashMap<>();

		failedCustomer.put("excelRow", excelRow);

		failedCustomer.put("fullName", fullName);

		failedCustomer.put("mobileNumber", mobileNumber);

		failedCustomer.put("reason", reason);

		failedCustomers.add(failedCustomer);
	}

	private String getCellValue(Cell cell) {

		if (cell == null) {
			return "";
		}

		DataFormatter formatter = new DataFormatter();

		return formatter.formatCellValue(cell).trim();
	}

	public String retrievePatientName(String patientId) {
		String name = null;
		try {
			name = onboardingRepository.findByPatientId(patientId).getFullName();
		} catch (Exception e) {
			return null;
		}
		return name;
	}

	@Override
	public ResponseEntity<Response> getCustomersByClinicId(String clinicId) {
		Response response = new Response();
		ObjectMapper objectMapper = new ObjectMapper();

		try {
			List<CustomerOnbordingDTO> customers = onboardingRepository.findByHospitalId(clinicId).stream()
					.map(entity -> objectMapper.convertValue(entity, CustomerOnbordingDTO.class))
					.collect(Collectors.toList());

			if (customers.isEmpty()) {
				response.setSuccess(false);
				response.setMessage("No customers found for clinicId: " + clinicId);
				response.setStatus(200);
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
			}

			response.setSuccess(true);
			response.setMessage("Customers retrieved successfully");
			response.setData(customers);
			response.setStatus(200);

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			response.setSuccess(false);
			response.setMessage("Error fetching customers: " + e.getMessage());
			response.setStatus(500);

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	@Override
	public Response getCustomersByHospitalIdAndClinicId(String clinicId, String searchInput) {
		Response response = new Response();
		try {
			// Fetch all customers for clinic
			List<CustomerOnbordingDTO> customers = onboardingRepository.findByHospitalId(clinicId).stream()
					.map(this::convertToDTO).collect(Collectors.toList());

			// ✅ Detect input type and filter
			if (searchInput != null && !searchInput.isBlank()) {
				String lowerSearch = searchInput.toLowerCase();

				if (searchInput.matches("\\d+")) {
					// Input is numeric → treat as mobile number
					customers = customers.stream()
							.filter(c -> c.getMobileNumber() != null && c.getMobileNumber().contains(searchInput))
							.collect(Collectors.toList());

				} else if (searchInput.matches("\\d+_PT_\\d+")) {
					// Input matches patientId format (e.g. 000201_PT_00004)
					customers = customers.stream()
							.filter(c -> c.getPatientId() != null && c.getPatientId().equalsIgnoreCase(searchInput))
							.collect(Collectors.toList());

				} else {
					// Otherwise treat as name (ignore case)
					customers = customers.stream()
							.filter(c -> c.getFullName() != null && c.getFullName().toLowerCase().contains(lowerSearch))
							.collect(Collectors.toList());
				}
			}

			response.setSuccess(true);
			response.setMessage(customers.isEmpty() ? "No customers found for clinicId: " + clinicId
					: "Customers retrieved successfully");
			response.setData(customers);
			response.setStatus(200);

		} catch (Exception e) {
			response.setSuccess(false);
			response.setMessage("Error fetching customers: " + e.getMessage());
			response.setStatus(500);
		}
		return response;
	}

}