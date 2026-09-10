package com.clinicadmin.service;

import java.util.List;

import com.clinicadmin.dto.ResponseStructure;
import com.clinicadmin.dto.WardBoyDTO;

public interface WardBoyService {
    
    ResponseStructure<WardBoyDTO> addWardBoy(WardBoyDTO dto);
    
    ResponseStructure<WardBoyDTO> getWardBoyById(String id);
    
    ResponseStructure<List<WardBoyDTO>> getAllWardBoys();
    
    ResponseStructure<WardBoyDTO> updateWardBoy(String id, WardBoyDTO dto);
    
    ResponseStructure<Void> deleteWardBoy(String id);

	ResponseStructure<List<WardBoyDTO>> getWardBoysByClinicId(String clinicId);

	ResponseStructure<WardBoyDTO> getWardBoyByIdAndClinicId(String wardBoyId, String clinicId);

	ResponseStructure<List<WardBoyDTO>> getWardBoysByClinicIdAndBranchId(String clinicId, String branchId);

}