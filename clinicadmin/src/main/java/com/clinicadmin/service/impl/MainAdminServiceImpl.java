package com.clinicadmin.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.clinicadmin.dto.CategoryMediaCarouselDTO;
import com.clinicadmin.feignclient.AdminServiceClient;
import com.clinicadmin.service.MainAdminService;

@Service
public class MainAdminServiceImpl implements MainAdminService {

	@Autowired
	private AdminServiceClient adminServiceClient;

	@Override
	public Iterable<CategoryMediaCarouselDTO> fetchAllMedia() {
		ResponseEntity<Iterable<CategoryMediaCarouselDTO>> response = adminServiceClient.getAllMedia();
		return response.getBody();
	}
}
