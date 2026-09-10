package com.AdminService.service;



import com.AdminService.dto.PermissionDTO;
import com.AdminService.util.Response;


public interface PermissionService {

    Response create(PermissionDTO dto);

    Response update(String id, PermissionDTO dto);

    Response getById(String id);

    Response delete(String id);

	Response getAll();



	Response updateByPlanId(String id, String planId, PermissionDTO dto);

	

	Response deleteByPlanId(String id, String planId, PermissionDTO dto);

	Response getByPlanId(String id, String planId);

	Response getByPlanId(String planId);

	Response updateByPlanId(String planId, PermissionDTO dto);
}
