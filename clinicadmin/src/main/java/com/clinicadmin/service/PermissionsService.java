package com.clinicadmin.service;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.clinicadmin.dto.PermissionsDTO;
import com.clinicadmin.dto.ResponseStructure;

public interface PermissionsService {

    // ✅ Get permissions for a user (by clinicId, branchId, and userId)
    ResponseStructure<PermissionsDTO> getPermissionsByClinicBranchAndUser(String clinicId, String branchId, String userId);

    // ✅ Update permissions for a user (update directly in their role entity)
    ResponseEntity<ResponseStructure<PermissionsDTO>> updatePermissionsById(String userId, PermissionsDTO dto);

	ResponseStructure<List<PermissionsDTO>> getPermissionsByClinicId(String clinicId);
    ResponseStructure<List<PermissionsDTO>> getPermissionsByClinicAndBranch(String clinicId, String branchId); 
    ResponseStructure<PermissionsDTO> getPermissionsByUserId(String userId);

    ResponseStructure<List<PermissionsDTO>> getPermissionsByBranchId(String branchId);

	ResponseEntity<Map<String, List<String>>> getDefaultAdminPermissions();


}
