//package com.clinicadmin.service.impl;
//
//import java.util.List;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Service;
//
//import com.clinicadmin.dto.Response;
//import com.clinicadmin.dto.ResponseStructure;
//import com.clinicadmin.dto.ServicesDto;
//import com.clinicadmin.feignclient.ServiceFeignClient;
//import com.clinicadmin.service.ServicesService;
//import com.clinicadmin.utils.ExtractFeignMessage;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import feign.FeignException;
//
//@Service
//public class ServicesServiceImpl implements ServicesService{
//	private static final Logger log = LoggerFactory.getLogger(ServicesServiceImpl.class);
//	@Autowired
//	ServiceFeignClient serviceFeignClient;
//	
//	@Override
//	public Response getServiceById(String categoryId) {
//	    Response response = new Response();
//	    log.info("Fetching services by categoryId={}", categoryId);
//	    try {
//	        ResponseEntity<ResponseStructure<List<ServicesDto>>> res = serviceFeignClient.getServiceById(categoryId);
//	        if (res.hasBody() && res.getBody() != null) {
//	        	log.info("Services fetched successfully | categoryId={}", categoryId);
//	        	ResponseStructure<List<ServicesDto>> rsBody = res.getBody();
//	            response.setSuccess(true);
//	            response.setData(rsBody.getData());
//	            response.setMessage(rsBody.getMessage());
//	            response.setStatus(200);
//	        }
//	    } catch (FeignException e) {
//	    	log.error("Feign error while fetching services by categoryId={}",
//					categoryId, e);
//	        response.setSuccess(false);
//	        response.setMessage(ExtractFeignMessage.clearMessage(e));
//	        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
////	        e.printStackTrace();
//	    }
//	    return response;
//	}
//
//
//	@Override
//	public Response getServiceByServiceId(String serviceId) {
//		log.info("Fetching service by serviceId={}", serviceId);
//
//		Response response = new Response();
//	    try {
//	    	ResponseEntity<ResponseStructure<ServicesDto>> res = serviceFeignClient.getServiceByServiceId(serviceId);
//	        if (res.hasBody() && res.getBody() != null) {
//				log.info("Service fetched successfully | serviceId={}", serviceId);
//
//	            ResponseStructure<ServicesDto> rsBody = res.getBody();
//	            response.setSuccess(true);
//	            response.setData(rsBody.getData());
//	            response.setMessage(rsBody.getMessage());
//	            response.setStatus(200);
//	        }
//	    } catch (FeignException e) {
//	    	log.error("Feign error while fetching service by serviceId={}",
//					serviceId, e);
//	        response.setSuccess(false);
//	        response.setMessage(ExtractFeignMessage.clearMessage(e));
//	        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
////	        e.printStackTrace();
//	    }
//	    return response;
//	}
//
//	@Override
//	public Response getAllServices() {
//		log.info("Fetching all services");
//
//		Response response = new Response();
//	    try {
//	        ResponseEntity<ResponseStructure<List<ServicesDto>>> res = serviceFeignClient.getAllServices();
//	        if (res.hasBody() && res.getBody() != null) {
//				log.info("All services fetched successfully");
//
//	        	ResponseStructure<List<ServicesDto>> rsBody = res.getBody();
//	            response.setSuccess(true);
//	            response.setData(rsBody.getData());
//	            response.setMessage(rsBody.getMessage());
//	            response.setStatus(200);	
//	        }
//	    } catch (FeignException e) {
//			log.error("Feign error while fetching all services", e);
//
//	    	response.setSuccess(false);
//	        response.setMessage(ExtractFeignMessage.clearMessage(e));
//	        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
////	        e.printStackTrace();
//	    }
//	    return response;
//	}
//
//}
