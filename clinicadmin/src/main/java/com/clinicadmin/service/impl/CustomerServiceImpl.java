package com.clinicadmin.service.impl;

import org.springframework.http.ResponseEntity;

import com.clinicadmin.dto.Response;
import com.clinicadmin.feignclient.CustomerServiceFeignClient;
import com.clinicadmin.service.CustomerService;

public class CustomerServiceImpl implements CustomerService {

	CustomerServiceFeignClient customerServiceFeignClient;
	
    public Response fetchSubServiceDetails(String subServiceId) {
    	Response response = new Response();
        try {
            ResponseEntity<Object> subServiceResponse = customerServiceFeignClient.getSubServiceInfoBySubServiceId(subServiceId);
            response.setSuccess(true);
            response.setData(subServiceResponse.getBody());
            response.setMessage("Fetched subservice info successfully");
            response.setStatus(200);
        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("Error fetching subservice info: " + e.getMessage());
            response.setStatus(500);
        }
        return response;
    }
	
	@Override
	public Response deleteBookedService(String id) {
		
		return null;
	}

}
