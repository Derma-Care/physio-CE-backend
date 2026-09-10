package com.clinicadmin.service;

import com.clinicadmin.dto.FollowOptionDTO;
import com.clinicadmin.dto.Response;

public interface FollowOptionService {

    Response create(FollowOptionDTO dto);

    Response getAll();

    Response getById(String id);

    Response update(String id, FollowOptionDTO dto);

    Response delete(String id);
}
