package com.clinicadmin.service;

import java.util.List;

import com.clinicadmin.dto.ResponseStructure;
import com.clinicadmin.dto.SecurityStaffDTO;
import com.clinicadmin.entity.SecurityStaff;

public interface SecurityStaffService {
    ResponseStructure<SecurityStaffDTO> addSecurityStaff(SecurityStaffDTO dto);
    ResponseStructure<SecurityStaffDTO> getSecurityStaffById(String staffId);
    ResponseStructure<List<SecurityStaffDTO>> getAllByClinicId(String clinicId);
    ResponseStructure<String> deleteSecurityStaff(String staffId);
//	ResponseStructure<SecurityStaff> updateSecurityStaff(SecurityStaffDTO dto);
	ResponseStructure<SecurityStaff> updateSecurityStaff(SecurityStaff staffRequest);
	ResponseStructure<List<SecurityStaffDTO>> getSecurityStaffByClinicIdAndBranchId(String clinicId, String branchId);

}