package com.clinicadmin.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clinicadmin.dto.PermissionsDTO;
import com.clinicadmin.dto.ResponseStructure;
import com.clinicadmin.service.PermissionsService;

@RestController
@RequestMapping("/clinic-admin")
public class PermissionsController {

    @Autowired
    private PermissionsService permissionsService;

    // ✅ 1️⃣ Get Permissions for a specific user
    @GetMapping("/getPermissionsByClinicIdBranchIdUserId/{clinicId}/{branchId}/{userId}")
    public ResponseEntity<ResponseStructure<PermissionsDTO>> getPermissions(
            @PathVariable String clinicId,
            @PathVariable String branchId,
            @PathVariable String userId) {

        ResponseStructure<PermissionsDTO> response =
                permissionsService.getPermissionsByClinicBranchAndUser(clinicId, branchId, userId);

        return ResponseEntity.status(response.getHttpStatus()).body(response);
    }



        @PutMapping("/updatePermissionsByUserId/{userId}")
        public ResponseEntity<ResponseStructure<PermissionsDTO>> updatePermissions(
                @PathVariable String userId,
                @RequestBody PermissionsDTO dto) {
            // dto must contain clinicId and branchId
            return permissionsService.updatePermissionsById(userId, dto);
        }
    

    // ✅ 3️⃣ Get All Permissions by Clinic ID
    @GetMapping("/getPermissionsByClinicId/{clinicId}")
    public ResponseEntity<ResponseStructure<List<PermissionsDTO>>> getPermissionsByClinicId(
            @PathVariable String clinicId) {

        ResponseStructure<List<PermissionsDTO>> response =
                permissionsService.getPermissionsByClinicId(clinicId);

        return ResponseEntity.status(response.getHttpStatus()).body(response);
    }

    // ✅ 4️⃣ Get All Permissions by Clinic ID and Branch ID
    @GetMapping("/getPermissionsByClinicIdAndBranchId/{clinicId}/{branchId}")
    public ResponseEntity<ResponseStructure<List<PermissionsDTO>>> getPermissionsByClinicIdAndBranchId(
            @PathVariable String clinicId,
            @PathVariable String branchId) {

        ResponseStructure<List<PermissionsDTO>> response =
                permissionsService.getPermissionsByClinicAndBranch(clinicId, branchId);

        return ResponseEntity.status(response.getHttpStatus()).body(response);
    }
 // ✅ 5️⃣ Get Permissions by User ID only
    @GetMapping("/getPermissionsByUserId/{userId}")
    public ResponseEntity<ResponseStructure<PermissionsDTO>> getPermissionsByUserId(
            @PathVariable String userId) {

        ResponseStructure<PermissionsDTO> response =
                permissionsService.getPermissionsByUserId(userId);

        return ResponseEntity.status(response.getHttpStatus()).body(response);
    }

    // ✅ 6️⃣ Get All Permissions by Branch ID only
    @GetMapping("/getPermissionsByBranchId/{branchId}")
    public ResponseEntity<ResponseStructure<List<PermissionsDTO>>> getPermissionsByBranchId(
            @PathVariable String branchId) {

        ResponseStructure<List<PermissionsDTO>> response =
                permissionsService.getPermissionsByBranchId(branchId);

        return ResponseEntity.status(response.getHttpStatus()).body(response);
    }
    
    // ✅ Get Default Admin Permissions from Admin Service
    @GetMapping("/getDefaultAdminPermissions")
    public ResponseEntity<Map<String, List<String>>> getDefaultAdminPermissions() {
        return permissionsService.getDefaultAdminPermissions();
    }

}
