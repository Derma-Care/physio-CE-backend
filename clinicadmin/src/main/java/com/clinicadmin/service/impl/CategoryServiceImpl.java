//package com.clinicadmin.service.impl;
//
//
//import java.util.List;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Service;
//
//
//import com.clinicadmin.dto.CategoryDto;
//import com.clinicadmin.dto.Response;
//import com.clinicadmin.dto.ResponseStructure;
//import com.clinicadmin.feignclient.ServiceFeignClient;
//import com.clinicadmin.service.CategoryService;
//
//import feign.FeignException;
//
//@Service
//public class CategoryServiceImpl implements CategoryService {
//
//	@Autowired
//	private ServiceFeignClient serviceFeignClient;
//
//	
//	@Override
//	public Response getCategoryById(String categoryId) {
//	    Response response = new Response();
//	    try {
//	        ResponseEntity<ResponseStructure<CategoryDto>> res = serviceFeignClient.getCategoryById(categoryId);
//	        if (res.hasBody() && res.getBody() != null) {
//	            ResponseStructure<CategoryDto> rs = res.getBody();
//	            response.setData(rs.getData());
//	            response.setStatus(rs.getStatusCode());
//	            response.setMessage(rs.getMessage());
//	            response.setSuccess(true);
//	        } else {
//	        	ResponseStructure<CategoryDto> rs = res.getBody();
//	            response.setStatus(rs.getStatusCode());
//	            response.setMessage(rs.getMessage());
//	            response.setSuccess(false);
//	        }
//	    } catch (FeignException e) {
//	        response.setStatus(e.status());
//	        response.setMessage(e.getMessage());
//	        response.setSuccess(false);
//	    }
//	    return response;
//	}
//
//
//	@Override
//	public Response getAllCategory() {
//		Response response= new Response();
//		try {
//			ResponseEntity<ResponseStructure<List<CategoryDto>>> res=serviceFeignClient.getAllCategory();
//			if(res.hasBody() && res.getBody()!=null) {
//				ResponseStructure <List <CategoryDto>> rsBody =res.getBody();
//				response.setSuccess(true);
//				response.setData(rsBody.getData());
//				response.setMessage(rsBody.getMessage());
//				response.setStatus(rsBody.getStatusCode());
//				
//			}else {
//				ResponseStructure <List <CategoryDto>> rsBody =res.getBody();
//				response.setSuccess(false);
//				response.setMessage(rsBody.getMessage());
//				response.setStatus(rsBody.getStatusCode());
//			}
//		}
//		catch (FeignException e) {
//	        response.setStatus(e.status());
//	        response.setMessage(e.getMessage());
//	        response.setSuccess(false);
//		}
//		return response;
//	}
//
//}
