package com.AdminService.feign;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.AdminService.dto.BookingResponse;
import com.AdminService.util.Response;
import com.AdminService.util.ResponseStructure;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@FeignClient(value = "bookingservice")
@CircuitBreaker(name = "circuitBreaker", fallbackMethod = "bookServiceFallBack")
@CrossOrigin
public interface BookingFeign {
	
	@GetMapping("/api/v1/getAllBookedServices")
	public ResponseEntity<ResponseStructure<List<BookingResponse>>> getAllBookedService();


}