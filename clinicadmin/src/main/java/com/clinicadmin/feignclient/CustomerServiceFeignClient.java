package com.clinicadmin.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.clinicadmin.dto.Response;
import com.clinicadmin.entity.QuestionsByPartEntity;


@FeignClient(name = "customerservice")
public interface CustomerServiceFeignClient {

//    @GetMapping("/api/customer/getRatingInfo/{hospitalId}/{doctorId}")
//    public ResponseEntity<Response> getRatingInfo(@PathVariable String hospitalId, @PathVariable String doctorId);

	@DeleteMapping("/api/customer/deleteService/{id}")
	public ResponseEntity<Response> deleteBookedService(@PathVariable String id);

	@GetMapping("/api/customer/getRatingInfo/{branchlId}/{doctorId}")
	public ResponseEntity<Response> getRatingInfo(@PathVariable String branchlId, @PathVariable String doctorId);
	
	@GetMapping("/api/customer/getRatingInfoByDoctorId/{doctorId}")
	public ResponseEntity<Response> getRatingInfoByDoctorId(@PathVariable String doctorId);
	
	@GetMapping("/api/customer/getSubServiceInfo/{subServiceId}")
	   public ResponseEntity<Object> getSubServiceInfoBySubServiceId(@PathVariable String subServiceId);
	 @GetMapping("/api/customer/getByKey/{key}")
	   public ResponseEntity<QuestionsByPartEntity> getByKey(@PathVariable String key);
	
}