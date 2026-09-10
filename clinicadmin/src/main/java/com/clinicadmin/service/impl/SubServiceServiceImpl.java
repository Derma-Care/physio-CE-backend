//package com.clinicadmin.service.impl;
//
//import java.util.List;
//
//import org.slf4j.LoggerFactory;
//import org.slf4j.Logger;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Service;
//
//import com.clinicadmin.dto.ResponseStructure;
//import com.clinicadmin.dto.SubServicesDto;
//import com.clinicadmin.entity.ProbableDiagnosis;
//import com.clinicadmin.entity.Treatment;
//import com.clinicadmin.exceptions.FeignClientException;
//import com.clinicadmin.feignclient.ServiceFeignClient;
//import com.clinicadmin.repository.ProbableDiagnosisRepository;
//import com.clinicadmin.repository.TreatmentRepository;
//import com.clinicadmin.service.SubServiceService;
//import com.clinicadmin.utils.ExtractFeignMessage;
//
//import feign.FeignException;
//
//@Service
//public class SubServiceServiceImpl implements SubServiceService {
//	private static final Logger log = LoggerFactory.getLogger(SubServiceServiceImpl.class);
//	@Autowired
//	private ServiceFeignClient feignClient;
//
////	@Override
////	public ResponseEntity<ResponseStructure<SubServicesDto>> addService(String subServiceId, SubServicesDto dto) {
////		try {
////			ResponseEntity<ResponseStructure<SubServicesDto>> response = feignClient.addService(subServiceId, dto);
////			return ResponseEntity.status(response.getBody().getStatusCode()).body(response.getBody());
////
////		} catch (FeignClientException ex) {
////			return buildErrorResponse(ex.getMessage(), ex.getStatusCode());
////		} catch (FeignException e) {
////			return buildErrorResponse(ExtractFeignMessage.clearMessage(e), HttpStatus.INTERNAL_SERVER_ERROR.value());
////		}
////	}
//	@Autowired
//	private TreatmentRepository treatmentRepository;
//
//	@Autowired
//	private ProbableDiagnosisRepository probableDiagnosisRepository;
//
//	@Override
//	public ResponseEntity<ResponseStructure<SubServicesDto>> addService(String subServiceId, SubServicesDto dto) {
//		log.info("Add SubService request received | subServiceId={}, hospitalId={}", subServiceId, dto.getHospitalId());
//		try {
//			// 1️⃣ Save SubService (via Feign)
//			log.info("Calling Service Management to add SubService | subServiceId={}", subServiceId);
//
//			ResponseEntity<ResponseStructure<SubServicesDto>> response = feignClient.addService(subServiceId, dto);
//			SubServicesDto savedSubService = response.getBody().getData();
//
//			log.info("SubService added successfully via Feign | subServiceName={}",
//					savedSubService.getSubServiceName());
//			// 2️⃣ Save Treatment
//			Treatment treatment = new Treatment();
//			treatment.setTreatmentName(savedSubService.getSubServiceName());
//			treatment.setHospitalId(savedSubService.getHospitalId());
//			treatmentRepository.save(treatment);
//
//			log.info("Treatment saved for SubService | treatmentName={}", savedSubService.getSubServiceName());
//
//			// 3️⃣ Save ProbableDiagnosis
//			ProbableDiagnosis diagnosis = new ProbableDiagnosis();
//			diagnosis.setDiseaseName(savedSubService.getSubServiceName()); // map subServiceName → diseaseName
//			diagnosis.setHospitalId(savedSubService.getHospitalId());
//			probableDiagnosisRepository.save(diagnosis);
//
//			log.info("ProbableDiagnosis saved | diseaseName={}", savedSubService.getSubServiceName());
//
//			// 4️⃣ Return SubService response
//			return ResponseEntity.status(response.getBody().getStatusCode()).body(response.getBody());
//
//		} catch (FeignClientException ex) {
//			log.error("FeignClientException while adding SubService | subServiceId={}, statusCode={}", subServiceId,
//					ex.getStatusCode(), ex);
//			return buildErrorResponse(ex.getMessage(), ex.getStatusCode());
//		} catch (FeignException e) {
//			log.error("FeignException while adding SubService | subServiceId={}", subServiceId, e);
//
//			return buildErrorResponse(ExtractFeignMessage.clearMessage(e), HttpStatus.INTERNAL_SERVER_ERROR.value());
//		}
//	}
//
//	@Override
//	public ResponseEntity<ResponseStructure<List<SubServicesDto>>> getSubServiceByIdCategory(String categoryId) {
//		log.info("Fetching SubServices by categoryId={}", categoryId);
//
//		try {
//			ResponseEntity<ResponseStructure<List<SubServicesDto>>> response = feignClient
//					.getSubServiceByIdCategory(categoryId);
//			log.info("SubServices fetched successfully | categoryId={}", categoryId);
//
//			return ResponseEntity.status(response.getBody().getStatusCode()).body(response.getBody());
//
//		} catch (FeignException e) {
//			log.error("FeignException while fetching SubServices by categoryId={}", categoryId, e);
//
//			return buildErrorResponseList(ExtractFeignMessage.clearMessage(e),
//					HttpStatus.INTERNAL_SERVER_ERROR.value());
//		}
//	}
//
//	@Override
//	public ResponseEntity<ResponseStructure<List<SubServicesDto>>> getSubServicesByServiceId(String serviceId) {
//		log.info("Fetching SubServices by serviceId={}", serviceId);
//
//		try {
//			ResponseEntity<ResponseStructure<List<SubServicesDto>>> response = feignClient
//					.getSubServicesByServiceId(serviceId);
//			log.info("SubServices fetched successfully | serviceId={}", serviceId);
//
//			return ResponseEntity.status(response.getBody().getStatusCode()).body(response.getBody());
//
//		} catch (FeignException ex) {
//			log.error("FeignException while fetching SubServices by serviceId={}", serviceId, ex);
//
//			return buildErrorResponseList(ExtractFeignMessage.clearMessage(ex), ex.status());
//		}
//
//	}
//
//	@Override
//	public ResponseEntity<ResponseStructure<SubServicesDto>> getSubServiceByServiceId(String subServiceId) {
//		log.info("Fetching SubService | subServiceId={}", subServiceId);
//
//		try {
//			ResponseEntity<ResponseStructure<SubServicesDto>> response = feignClient
//					.getSubServiceByServiceId(subServiceId);
//			log.info("SubService fetched successfully | subServiceId={}", subServiceId);
//
//			return ResponseEntity.status(response.getBody().getStatusCode()).body(response.getBody());
//		}
//
//		catch (FeignException e) {
//			log.error("FeignException while fetching SubService | subServiceId={}", subServiceId, e);
//
//			return buildErrorResponse(ExtractFeignMessage.clearMessage(e), HttpStatus.INTERNAL_SERVER_ERROR.value());
//		}
//
//	}
//
//	@Override
//	public ResponseEntity<ResponseStructure<SubServicesDto>> deleteSubService(String hospitalId, String subServiceId) {
//		log.info("Delete SubService request | hospitalId={}, subServiceId={}", hospitalId, subServiceId);
//
//		try {
//			ResponseEntity<ResponseStructure<SubServicesDto>> response = feignClient.deleteSubService(hospitalId,
//					subServiceId);
//			log.info("SubService deleted successfully | subServiceId={}", subServiceId);
//
//			return ResponseEntity.status(response.getBody().getStatusCode()).body(response.getBody());
//		}
//
//		catch (FeignException e) {
//			log.error("FeignException while deleting SubService | subServiceId={}", subServiceId, e);
//
//			return buildErrorResponse(ExtractFeignMessage.clearMessage(e), HttpStatus.INTERNAL_SERVER_ERROR.value());
//		}
//	}
//
//	@Override
//	public ResponseEntity<ResponseStructure<SubServicesDto>> updateBySubServiceId(String hospitalId,
//			String subServiceId, SubServicesDto domainServices) {
//		log.info("Update SubService request | hospitalId={}, subServiceId={}",
//				hospitalId, subServiceId);
//		try {
//			ResponseEntity<ResponseStructure<SubServicesDto>> response = feignClient.updateBySubServiceId(hospitalId,
//					subServiceId, domainServices);
//			log.info("SubService updated successfully | subServiceId={}", subServiceId);
//
//			return ResponseEntity.status(response.getBody().getStatusCode()).body(response.getBody());
//
//		} catch (FeignException e) {
//			log.error("FeignException while updating SubService | subServiceId={}", subServiceId, e);
//
//			return buildErrorResponse(ExtractFeignMessage.clearMessage(e), HttpStatus.INTERNAL_SERVER_ERROR.value());
//		}
//	}
//
//	@Override
//	public ResponseEntity<ResponseStructure<SubServicesDto>> getSubServiceByServiceId(String hospitalId,
//			String subServiceId) {
//		log.info("Fetching SubService | hospitalId={}, subServiceId={}",
//				hospitalId, subServiceId);
//		try {
//			ResponseEntity<ResponseStructure<SubServicesDto>> response = feignClient
//					.getSubServiceByServiceId(hospitalId, subServiceId);
//			log.info("SubService fetched successfully | subServiceId={}", subServiceId);
//
//			return ResponseEntity.status(HttpStatus.OK).body(response.getBody());
//
//		} catch (FeignException e) {
//			log.error("FeignException while fetching SubService | subServiceId={}", subServiceId, e);
//
//			return buildErrorResponse(ExtractFeignMessage.clearMessage(e), HttpStatus.INTERNAL_SERVER_ERROR.value());
//		}
//	}
//
//	@Override
//	public ResponseEntity<ResponseStructure<List<SubServicesDto>>> getSubServiceByHospitalId(String hospitalId) {
//		log.info("Fetching SubServices by hospitalId={}", hospitalId);
//
//		try {
//			ResponseEntity<ResponseStructure<List<SubServicesDto>>> response = feignClient
//					.getSubServiceByHospitalId(hospitalId); // ✅ FIXED here
//			log.info("SubServices fetched successfully | hospitalId={}", hospitalId);
//
//			return ResponseEntity.status(HttpStatus.OK).body(response.getBody());
//
//		} catch (FeignException e) {
//			log.error("FeignException while fetching SubServices by hospitalId={}", hospitalId, e);
//
//			return buildErrorResponseList(ExtractFeignMessage.clearMessage(e),
//					HttpStatus.INTERNAL_SERVER_ERROR.value());
//		}
//	}
//
//	@Override
//	public ResponseEntity<ResponseStructure<List<SubServicesDto>>> getAllSubServices() {
//		log.info("Fetching all SubServices");
//
//		try {
//			ResponseEntity<ResponseStructure<List<SubServicesDto>>> response = feignClient.getAllSubServices();
//			log.info("All SubServices fetched successfully");
//
//			return ResponseEntity.status(response.getBody().getStatusCode()).body(response.getBody());
//
//		} catch (FeignException e) {
//			log.error("FeignException while fetching all SubServices", e);
//
//			return buildErrorResponseList(ExtractFeignMessage.clearMessage(e),
//					HttpStatus.INTERNAL_SERVER_ERROR.value());
//		}
//	}
//
//	// === Helper methods ===
//
//	private ResponseEntity<ResponseStructure<SubServicesDto>> buildErrorResponse(String message, int statusCode) {
//		
//		ResponseStructure<SubServicesDto> errorResponse = ResponseStructure.<SubServicesDto>builder().data(null)
//				.message(extractCleanMessage(message)).httpStatus(HttpStatus.valueOf(statusCode)).statusCode(statusCode)
//				.build();
//
//		return ResponseEntity.status(statusCode).body(errorResponse);
//	}
//
//	private ResponseEntity<ResponseStructure<List<SubServicesDto>>> buildErrorResponseList(String message,
//			int statusCode) {
//		ResponseStructure<List<SubServicesDto>> errorResponse = ResponseStructure.<List<SubServicesDto>>builder()
//				.data(null) // <-- changed from null to empty list
//				.message(extractCleanMessage(message)).httpStatus(HttpStatus.valueOf(statusCode)).statusCode(statusCode)
//				.build();
//		return ResponseEntity.status(statusCode).body(errorResponse);
//	}
//
//	private String extractCleanMessage(String rawMessage) {
//		// Try to extract the "message" value from JSON string if included
//		try {
//			int msgStart = rawMessage.indexOf("\"message\":\"");
//			if (msgStart != -1) {
//				int start = msgStart + 10;
//				int end = rawMessage.indexOf("\"", start);
//				return rawMessage.substring(start, end);
//			}
//		} catch (Exception ignored) {
//		}
//		return rawMessage;
//	}
//
//	// ------------------------------------Amount calculation using cosultation type
//	// (1= FOC,2=paid)-----------------------
//	@Override
//	public ResponseEntity<ResponseStructure<SubServicesDto>> getSubServiceCostByConsultationType(String hospitalId,
//			String subServiceId, String subServiceName, int consultationType) {
//		log.info("Fetching SubService cost | hospitalId={}, subServiceId={}, consultationType={}",
//				hospitalId, subServiceId, consultationType);
//
//		try {
//			ResponseEntity<ResponseStructure<SubServicesDto>> response = feignClient
//					.getSubServiceCostByConsultationType(hospitalId, subServiceId, subServiceName, consultationType);
//			log.info("SubService cost fetched successfully | subServiceId={}", subServiceId);
//
//			return ResponseEntity.status(response.getBody().getStatusCode()).body(response.getBody());
//
//		} catch (FeignException e) {
//			log.error("FeignException while fetching SubService cost | subServiceId={}", subServiceId, e);
//			return buildErrorResponse(ExtractFeignMessage.clearMessage(e), HttpStatus.INTERNAL_SERVER_ERROR.value());
//		}
//	}
//
//}