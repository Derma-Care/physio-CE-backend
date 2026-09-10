//package com.clinicadmin.service.impl;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Service;
//
//import com.clinicadmin.dto.Response;
//import com.clinicadmin.feignclient.DoctorServiceFeign;
//import com.clinicadmin.service.DoctorSaveDetailsFeignImplService;
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//@Service
//public class DoctorSaveDetailsFeignImplServiceImpl implements DoctorSaveDetailsFeignImplService {
//	@Autowired
//	DoctorServiceFeign doctorServiceFeign;
//
//	@Autowired
//	private ObjectMapper objectMapper;
//
//	@Override
//	public Response getVisitHistoryByPatientId(String patientId) {
//		Response res = new Response();
//
//		try {
//			// SUCCESS RESPONSE
//			ResponseEntity<Response> response = doctorServiceFeign.getVisitHistoryByPatientId(patientId);
//
//			return response.getBody();
//
//		} catch (feign.FeignException ex) {
//
//			try {
//				// Convert Feign error JSON body to Response object
//				Response errorResponse = objectMapper.readValue(ex.contentUTF8(), Response.class);
//
//				return errorResponse;
//
//			} catch (Exception e) {
//				// Fallback if parsing fails
//				res.setSuccess(false);
//				res.setMessage("Doctor Service Error: " + ex.getMessage());
//				res.setStatus(ex.status());
//				res.setData(null);
//				return res;
//			}
//		}
//	}
//}
