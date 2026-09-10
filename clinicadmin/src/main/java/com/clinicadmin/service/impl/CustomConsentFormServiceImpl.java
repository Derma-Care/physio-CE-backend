package com.clinicadmin.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.clinicadmin.dto.CustomConsentFormDTO;
import com.clinicadmin.dto.Response;
import com.clinicadmin.dto.ResponseStructure;
//import com.clinicadmin.dto.SubServicesDto;
import com.clinicadmin.entity.CustomConsentForm;
//import com.clinicadmin.feignclient.ServiceFeignClient;
import com.clinicadmin.repository.CustomConsentFormRepository;
import com.clinicadmin.service.CustomConsentFormService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CustomConsentFormServiceImpl implements CustomConsentFormService {

	@Autowired
	private CustomConsentFormRepository customConsentFormRepository;

//	@Autowired
//	private ServiceFeignClient serviceFeignClient;

	// ------------------------------- Add Consent Form
	// -------------------------------
//	@Override
//	public Response addCustomConsentForm(String hospitalId, String consentFormType, CustomConsentFormDTO dto) {
//		Response response = new Response();
//
//		// Basic Validations
//		if (hospitalId == null || hospitalId.trim().isEmpty()) {
//			return buildErrorResponse("Hospital ID cannot be null or empty", 400);
//		}
//		if (!isValidConsentFormType(consentFormType)) {
//			return buildErrorResponse("Invalid Consent Form Type. Allowed values: 1 (Generic), 2 (Procedure)", 400);
//		}
//		if (dto == null || dto.getConsentFormQuestions() == null || dto.getConsentFormQuestions().isEmpty()) {
//			return buildErrorResponse("Consent form questions cannot be empty", 400);
//		}
//		
//
//		try {
//		    dto.getConsentFormQuestions().forEach(heading -> {
//		        if (heading.getQuestionsAndAnswers() != null) {
//		            heading.getQuestionsAndAnswers().forEach(qa -> {
//		            	if (qa.isAnswer() ) {  
//		            	    qa.setAnswer(true);       
//		            	}
//
//		            });
//		        }
//		    });
//
//			// Generic Consent Form (only one per hospital)
//			if (consentFormType.equals("1")) {
//				if (customConsentFormRepository.findByHospitalIdAndConsentFormType(hospitalId, "1").isPresent()) {
//					return buildErrorResponse("Generic Consent Form already exists for this hospital", 409);
//				}
//
//				CustomConsentForm newForm = new CustomConsentForm();
//				newForm.setHospitalId(hospitalId);
//				newForm.setConsentFormType(consentFormType);
//				newForm.setConsentFormQuestions(dto.getConsentFormQuestions());
//				CustomConsentForm savedForm = customConsentFormRepository.save(newForm);
//				CustomConsentFormDTO savedDTO = new CustomConsentFormDTO();
//				savedDTO.setId(savedForm.getId());
//				savedDTO.setHospitalId(savedForm.getHospitalId());
//				savedDTO.setConsentFormType(savedForm.getConsentFormType());
//				
//				savedDTO.setConsentFormQuestions(savedForm.getConsentFormQuestions());
//
//				return buildSuccessResponse(savedDTO, "Generic Consent Questions added successfully");
//			}
//
//			// Procedure Consent Form (only one per subService)
//			else {
//				if (dto.getSubServiceid() == null) {
//					return buildErrorResponse("SubService ID is required for Procedure Consent Form", 400);
//				}
//
//				SubServicesDto subDTO = validateAndGetSubService(hospitalId, dto.getSubServiceid());
//				if (subDTO == null) {
//					return buildErrorResponse("SubService not found for ID: " + dto.getSubServiceid(), 404);
//				}
//
//				if (customConsentFormRepository.findByHospitalIdAndSubServiceid(hospitalId, subDTO.getSubServiceId())
//						.isPresent()) {
//					return buildErrorResponse(
//							"Procedure Consent Form already exists for SubService: " + subDTO.getSubServiceName(), 409);
//				}
//
//				CustomConsentForm newForm = new CustomConsentForm(null, hospitalId, subDTO.getSubServiceId(),
//						subDTO.getSubServiceName(), consentFormType, dto.getConsentFormQuestions());
//				CustomConsentForm savedForm = customConsentFormRepository.save(newForm);
//
//				return buildSuccessResponse(mapToDTO(savedForm), "Procedure Consent Questions added successfully");
//			}
//		} catch (Exception ex) {
//			log.error("Error while saving Consent Form", ex);
//			return buildErrorResponse("Error while saving Consent Form: " + ex.getMessage(), 500);
//		}
//	}

	// ------------------------------- Update Consent Form
	// -------------------------------
//	@Override
//	public Response updateCustomConsentForm(String hospitalId, String consentFormType, CustomConsentFormDTO dto) {
//		Response response = new Response();
//
//		// Basic Validations
//		if (hospitalId == null || hospitalId.trim().isEmpty()) {
//			return buildErrorResponse("Hospital ID cannot be null or empty", 400);
//		}
//		if (!isValidConsentFormType(consentFormType)) {
//			return buildErrorResponse("Invalid Consent Form Type. Allowed values: 1 (Generic), 2 (Procedure)", 400);
//		}
//		if (dto == null || dto.getConsentFormQuestions() == null || dto.getConsentFormQuestions().isEmpty()) {
//			return buildErrorResponse("Consent form questions cannot be empty", 400);
//		}
//
//		try {
//			if (consentFormType.equals("1")) {
//				CustomConsentForm existingForm = customConsentFormRepository
//						.findByHospitalIdAndConsentFormType(hospitalId, "1").orElse(null);
//				if (existingForm == null) {
//					return buildErrorResponse("Generic Consent Form not found for this hospital", 404);
//				}
//
//				existingForm.setConsentFormQuestions(dto.getConsentFormQuestions());
//				CustomConsentForm updatedForm = customConsentFormRepository.save(existingForm);
//				return buildSuccessResponse(mapToDTO(updatedForm), "Generic Consent Form updated successfully");
//			} else {
//				if (dto.getSubServiceid() == null) {
//					return buildErrorResponse("SubService ID is required for Procedure Consent Form", 400);
//				}
//
//				SubServicesDto subDTO = validateAndGetSubService(hospitalId, dto.getSubServiceid());
//				if (subDTO == null) {
//					return buildErrorResponse("SubService not found for ID: " + dto.getSubServiceid(), 404);
//				}
//
//				CustomConsentForm existingForm = customConsentFormRepository
//						.findByHospitalIdAndSubServiceid(hospitalId, subDTO.getSubServiceId()).orElse(null);
//				if (existingForm == null) {
//					return buildErrorResponse(
//							"Procedure Consent Form not found for SubService: " + subDTO.getSubServiceName(), 404);
//				}
//
//				existingForm.setConsentFormQuestions(dto.getConsentFormQuestions());
//				existingForm.setSubServiceName(subDTO.getSubServiceName());
//
//				CustomConsentForm updatedForm = customConsentFormRepository.save(existingForm);
//				return buildSuccessResponse(mapToDTO(updatedForm), "Procedure Consent Form updated successfully");
//			}
//		} catch (Exception ex) {
//			log.error("Error while updating Consent Form", ex);
//			return buildErrorResponse("Error while updating Consent Form: " + ex.getMessage(), 500);
//		}
//	}

	// ------------------------------- Get Consent Form
	// -------------------------------
	@Override
	public Response getConsentForm(String hospitalId, String consentFormType) {
		if (hospitalId == null || hospitalId.trim().isEmpty()) {
			return buildErrorResponse("Hospital ID cannot be null or empty", 400);
		}
		if (!isValidConsentFormType(consentFormType)) {
			return buildErrorResponse("Invalid Consent Form Type. Allowed values: 1 (Generic), 2 (Procedure)", 400);
		}

		try {
			if (consentFormType.equals("1")) {
			    CustomConsentForm form = customConsentFormRepository
			            .findByHospitalIdAndConsentFormType(hospitalId, "1")
			            .orElse(null);

			    if (form == null) {
			        return buildErrorResponse("Generic Consent Form not found for this hospital", 200);
			    }

			    return buildSuccessResponse(mapToDTO(form), "Generic Consent Form retrieved successfully");

			} else if (consentFormType.equals("2")) {
			    List<CustomConsentForm> formsList = customConsentFormRepository
			            .findAllByHospitalIdAndConsentFormType(hospitalId, "2");

			    if (formsList.isEmpty()) {
			        return buildErrorResponse("Procedure Consent Forms not found for this hospital", 200);
			    }

			    List<CustomConsentFormDTO> formsListDTO =
			            formsList.stream().map(this::mapToDTO).collect(Collectors.toList());

			    return buildSuccessResponse(formsListDTO, "Procedure Consent Forms retrieved successfully");

			} else {
			    return buildErrorResponse("Use getProcedureConsentForm API for procedure type", 400);
			}

		} catch (Exception ex) {
			log.error("Error while fetching Consent Form", ex);
			return buildErrorResponse("Error while fetching Consent Form: " + ex.getMessage(), 500);
		}
	}

	@Override
	public Response getProcedureConsentForm(String hospitalId, String subServiceId) {
		if (hospitalId == null || hospitalId.trim().isEmpty()) {
			return buildErrorResponse("Hospital ID cannot be null or empty", 400);
		}
		if (subServiceId == null || subServiceId.trim().isEmpty()) {
			return buildErrorResponse("SubService ID cannot be null or empty", 400);
		}

		try {
			CustomConsentForm form = customConsentFormRepository
					.findByHospitalIdAndSubServiceid(hospitalId, subServiceId).orElse(null);
			if (form == null) {
				return buildErrorResponse("Procedure Consent Form not found for SubService: " + subServiceId, 404);
			}
			return buildSuccessResponse(mapToDTO(form), "Procedure Consent Form retrieved successfully");
		} catch (Exception ex) {
			log.error("Error while fetching Procedure Consent Form", ex);
			return buildErrorResponse("Error while fetching Procedure Consent Form: " + ex.getMessage(), 500);
		}
	}

	// ------------------------------- Helper Methods
	// -------------------------------
	private boolean isValidConsentFormType(String type) {
		return type != null && (type.equals("1") || type.equals("2"));
	}

//	private SubServicesDto validateAndGetSubService(String hospitalId, String subServiceId) {
//		ResponseEntity<ResponseStructure<SubServicesDto>> subServiceResponse = serviceFeignClient
//				.getSubServiceByServiceId(hospitalId, subServiceId);
//
//		if (subServiceResponse != null && subServiceResponse.getBody() != null) {
//			return subServiceResponse.getBody().getData();
//		}
//		return null;
//	}

	private CustomConsentFormDTO mapToDTO(CustomConsentForm form) {
		return new CustomConsentFormDTO(form.getId(), form.getHospitalId(), form.getSubServiceid(),
				form.getSubServiceName(), form.getConsentFormType(), form.getConsentFormQuestions());
	}

	private Response buildErrorResponse(String message, int status) {
		Response response = new Response();
		response.setSuccess(false);
		response.setMessage(message);
		response.setStatus(status);
		return response;
	}

	private Response buildSuccessResponse(Object data, String message) {
		Response response = new Response();
		response.setSuccess(true);
		response.setData(data);
		response.setMessage(message);
		response.setStatus(200);
		return response;
	}

//    ---------------------------get all conset forms----------------------------------
	// ------------------------------- Get All Consent Forms by Hospital
	// -------------------------------
	@Override
	public Response getAllConsentFormsByHospital(String hospitalId) {
		if (hospitalId == null || hospitalId.trim().isEmpty()) {
			return buildErrorResponse("Hospital ID cannot be null or empty", 400);
		}

		try {
			// Fetch all consent forms for the hospital
			List<CustomConsentForm> forms = customConsentFormRepository.findByHospitalId(hospitalId);

			if (forms == null || forms.isEmpty()) {
				return buildErrorResponse("No Consent Forms found for Hospital ID: " + hospitalId, 404);
			}

			// Map entities to DTOs
			List<CustomConsentFormDTO> formDTOs = forms.stream().map(this::mapToDTO).toList();

			return buildSuccessResponse(formDTOs, "All Consent Forms retrieved successfully");
		} catch (Exception ex) {
			log.error("Error while fetching all Consent Forms for Hospital: {}", hospitalId, ex);
			return buildErrorResponse("Error while fetching Consent Forms: " + ex.getMessage(), 500);
		}
	}
	// ------------------------------- Delete Consent Form by ID -------------------------------
	@Override
	public Response deleteConsentFormById(String formId) {
	    if (formId == null || formId.trim().isEmpty()) {
	        return buildErrorResponse("Consent Form ID cannot be null or empty", 400);
	    }

	    try {
	        return customConsentFormRepository.findById(formId)
	                .map(form -> {
	                    customConsentFormRepository.deleteById(formId);
	                    return buildSuccessResponse(null, "Consent Form deleted successfully");
	                })
	                .orElseGet(() -> buildErrorResponse("Consent Form not found for ID: " + formId, 404));
	    } catch (Exception ex) {
	        log.error("Error while deleting Consent Form with ID: {}", formId, ex);
	        return buildErrorResponse("Error while deleting Consent Form: " + ex.getMessage(), 500);
	    }
	}

	

}
