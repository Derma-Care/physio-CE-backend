package com.clinicadmin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clinicadmin.dto.CategoryMediaCarouselDTO;
import com.clinicadmin.service.MainAdminService;

@RestController
@RequestMapping("/clinic-admin")
//@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class MainAdminServiceController {

	@Autowired
	MainAdminService mainAdminService;

	@GetMapping("/categoryAdvertisement/getAll")
	public ResponseEntity<Iterable<CategoryMediaCarouselDTO>> getAllMediaFromAdmin() {
		Iterable<CategoryMediaCarouselDTO> media = mainAdminService.fetchAllMedia();

		if (media == null || !media.iterator().hasNext()) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.ok(media);
	}

}
