package com.clinicadmin.service;

import com.clinicadmin.dto.PhysiLitePackageDTO;
import com.clinicadmin.dto.Response;

public interface PhysiLitePackageService {

	Response createPackage(String clinicId, String branchId, PhysiLitePackageDTO dto);

	Response updatePackage(String id, String clinicId, String branchId, PhysiLitePackageDTO dto);

	Response deletePackage(String id, String clinicId, String branchId);

	Response getPackageById(String id, String clinicId, String branchId);

	Response getAllPackagesByClinicIdAndBranchId(String clinicId, String branchId);

	Response getAllPackagesByClinicId(String clinicId);
	Response deletePackageByPackageId(String packageId, String clinicId, String branchId);
}