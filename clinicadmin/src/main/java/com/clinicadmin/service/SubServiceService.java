//package com.clinicadmin.service;
//
//import java.util.List;
//import org.springframework.http.ResponseEntity;
//import com.clinicadmin.dto.ResponseStructure;
//import com.clinicadmin.dto.SubServicesDto;
//
//public interface SubServiceService {
//
//	ResponseEntity<ResponseStructure<SubServicesDto>> addService(String subServiceId, SubServicesDto dto);
//
//	ResponseEntity<ResponseStructure<List<SubServicesDto>>> getSubServiceByIdCategory(String categoryId);
//
//	ResponseEntity<ResponseStructure<List<SubServicesDto>>> getSubServicesByServiceId(String serviceId);
//
//	ResponseEntity<ResponseStructure<SubServicesDto>> getSubServiceByServiceId(String subServiceId);
//
//	ResponseEntity<ResponseStructure<SubServicesDto>> deleteSubService(String hospitalId, String subServiceId);
//
//	ResponseEntity<ResponseStructure<SubServicesDto>> updateBySubServiceId(String hospitalId, String serviceId,
//			SubServicesDto domainServices);
//	ResponseEntity<ResponseStructure<SubServicesDto>> getSubServiceByServiceId( String hospitalId, String subServiceId);
//	ResponseEntity<ResponseStructure<List<SubServicesDto>>> getAllSubServices();
//
//	ResponseEntity<ResponseStructure<List<SubServicesDto>>> getSubServiceByHospitalId(String hospitalId);
//
//	
//	ResponseEntity<ResponseStructure<SubServicesDto>> getSubServiceCostByConsultationType(String hospitalId,
//			String subServiceId, String subServiceName, int consultationType);
//}