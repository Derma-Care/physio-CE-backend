package com.clinicadmin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clinicadmin.dto.Response;
import com.clinicadmin.service.impl.RatingCalculationService;

@RestController
@RequestMapping("/clinic-admin")
//@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class RatingsController {

	@Autowired
	private RatingCalculationService ratingCalculationService;

	@GetMapping("/averageRatings/{branchId}/{doctorId}")
	public ResponseEntity<Response> getAverageRatings(@PathVariable String branchId, @PathVariable String doctorId) {

		Response response = ratingCalculationService.calculateAverageRating(branchId, doctorId);
		if (response != null) {
			return ResponseEntity.status(response.getStatus()).body(response);
		}
		return null;
	}
	
	@GetMapping("/getAverageRatingsByDoctorId/{doctorId}")
	public ResponseEntity<Response> getAverageRatingsByDoctorId( @PathVariable String doctorId) {

		Response response = ratingCalculationService.calculateAverageRatingByDoctorId(doctorId);
		if (response != null) {
			return ResponseEntity.status(response.getStatus()).body(response);
		}
		return null;
	}
}
