//package com.clinicadmin.service.impl;
//
//import com.clinicadmin.dto.AreaDTO;
//import com.clinicadmin.dto.Response;
//import com.clinicadmin.feignclient.PharmacyManagementFeignClient;
//import com.clinicadmin.service.AreaService;
//import feign.FeignException;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//@Service
//@RequiredArgsConstructor
//public class AreaServiceImpl implements AreaService {
//	private static final Logger log = LoggerFactory.getLogger(AreaServiceImpl.class);
//
//	private final PharmacyManagementFeignClient pharmacyClient;
//	private final ObjectMapper objectMapper = new ObjectMapper();
//
//	@Override
//	public Response saveArea(AreaDTO dto) {
//		log.info("Save Area request recieved");
//		try {
//			log.info("Area saved successfully ");
//			return pharmacyClient.saveArea(dto);
//
//		} catch (FeignException ex) {
//			log.error("Feign error while saving area | status={}", ex.status(), ex);
//			return extractPharmacyError(ex);
//		}
//	}
//
//	@Override
//	public Response updateArea(String id, AreaDTO dto) {
//		log.info("Update Area request | areaId={}", id);
//		try {
//			log.info("Area updated successfully | areaId={}", id);
//			return pharmacyClient.updateArea(id, dto);
//		} catch (FeignException ex) {
//			log.error("Feign error while updating area | areaId={}, status={}", id, ex.status(), ex);
//
//			return extractPharmacyError(ex);
//		}
//	}
//
//	@Override
//	public Response getAreaById(String id) {
//		log.info("Get Area by ID request | areaId={}", id);
//		try {
//			log.info("Area fetched successfully | areaId={}", id);
//			return pharmacyClient.getAreaById(id);
//		} catch (FeignException ex) {
//			log.error("Feign error while fetching area | areaId={}, status={}", id, ex.status(), ex);
//			return extractPharmacyError(ex);
//		}
//	}
//
//	@Override
//	public Response getAllAreas() {
//		
//		try {
//			  Response response = pharmacyClient.getAllAreas();
//	            log.info("All areas fetched successfully");
//	            return response;
//		} catch (FeignException ex) {
//			log.error("Feign error while fetching all areas | status={}",
//                    ex.status(), ex);
//			return extractPharmacyError(ex);
//		}
//	}
//
//	@Override
//	public Response deleteArea(String id) {
//		log.info("Delete Area request | areaId={}", id);
//		try {
//			Response response = pharmacyClient.deleteArea(id);
//            log.info("Area deleted successfully | areaId={}", id);
//            return response;
//		} catch (FeignException ex) {
//			 log.error("Feign error while deleting area | areaId={}, status={}",
//	                    id, ex.status(), ex);
//			return extractPharmacyError(ex);
//		}
//	}
//
//	@Override
//	public Response getAreasByCity(String cityId) {
//		log.info("Get Areas by City request | cityId={}", cityId);
//		try {
//			  Response response = pharmacyClient.getAreasByCity(cityId);
//	            log.info("Areas fetched successfully | cityId={}", cityId);
//	            return response;
//		} catch (FeignException ex) {
//			 log.error("Feign error while fetching areas by city | cityId={}, status={}",
//	                    cityId, ex.status(), ex);
//			return extractPharmacyError(ex);
//		}
//	}
//
//	// ⭐ MOST IMPORTANT — Extract SAME message returned by pharmacy service
//	private Response extractPharmacyError(FeignException ex) {
//        log.warn("Extracting pharmacy service error | status={}", ex.status());
//
//		try {
//			return objectMapper.readValue(ex.contentUTF8(), Response.class);
//		} catch (Exception parseException) {
//            log.error("Failed to parse pharmacy error response", parseException);
//
//			// fallback if pharmacy didn’t return JSON
//			Response fallback = new Response();
//			fallback.setStatus(500);
//			fallback.setMessage("Unexpected error: " + ex.getMessage());
//			fallback.setData(null);
//			return fallback;
//		}
//	}
//}
