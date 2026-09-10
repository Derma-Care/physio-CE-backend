package com.clinicadmin.service;

import com.clinicadmin.dto.PhysiLiteSittingDTO;
import com.clinicadmin.dto.Response;

public interface PhysioLiteSittingService {

	Response createSitting(PhysiLiteSittingDTO dto);

	Response updateSitting(String id, String clinicId, String branchId, PhysiLiteSittingDTO dto);

	Response deleteSitting(String id, String clinicId, String branchId);

	Response getSittingById(String id, String clinicId, String branchId);

	Response getAllSittingsByClinicIdAndBranchId(String clinicId, String branchId);

	Response getAllSittingsByClinicId(String clinicId);

	Response getSittingBySittingId(String sittingId, String clinicId, String branchId);

	Response deleteSittingBySittingId(String sittingId, String clinicId, String branchId);
}