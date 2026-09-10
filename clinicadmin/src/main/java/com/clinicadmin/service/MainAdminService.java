package com.clinicadmin.service;

import com.clinicadmin.dto.CategoryMediaCarouselDTO;

public interface MainAdminService {

	Iterable<CategoryMediaCarouselDTO> fetchAllMedia();

}
