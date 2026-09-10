package com.clinicadmin.service;

import java.util.List;

import com.clinicadmin.dto.LabTechnicanRestPassword;
import com.clinicadmin.dto.LabTechnicianLogin;
import com.clinicadmin.dto.LabTechnicianRequestDTO;
import com.clinicadmin.dto.OnBoardResponse;
import com.clinicadmin.dto.ResponseStructure;

public interface LabTechnicianService {

    ResponseStructure<LabTechnicianRequestDTO> createLabTechnician(LabTechnicianRequestDTO dto);

    ResponseStructure<LabTechnicianRequestDTO> getLabTechnicianById(String id);

    ResponseStructure<List<LabTechnicianRequestDTO>> getAllLabTechnicians();

    ResponseStructure<LabTechnicianRequestDTO> updateLabTechnician(String id, LabTechnicianRequestDTO dto);

    ResponseStructure<String> deleteLabTechnician(String id);

    // ✅ Login method
//    OnBoardResponse login(LabTechnicianLogin loginRequest);

    // ✅ Reset Password method
//    ResponseStructure<String> resetPassword(String username, LabTechnicanRestPassword resetRequest);

	ResponseStructure<LabTechnicianRequestDTO> getLabTechnicianByClinicAndId(String clinicId, String technicianId);

	ResponseStructure<List<LabTechnicianRequestDTO>> getLabTechniciansByClinic(String clinicId);

	ResponseStructure<List<LabTechnicianRequestDTO>> getLabTechniciansByHospitalIdAndBranchId(String clinicId,
			String branchId);
	

}