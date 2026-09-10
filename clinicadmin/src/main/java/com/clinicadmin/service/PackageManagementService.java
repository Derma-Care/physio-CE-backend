package com.clinicadmin.service;

import com.clinicadmin.dto.PackageManagementDTO;
import com.clinicadmin.dto.Response;

public interface PackageManagementService {

    Response createPackage(PackageManagementDTO dto);
    
    Response getByClinicAndBranch(String clinicId, String branchId);

    Response getByClinicBranchAndPackageId(String clinicId, String branchId, String packageId);

    Response updatePackage(String packageId, PackageManagementDTO dto);

    Response deletePackage(String packageId);

	Response getPackageWithPrograms(String packageId, String clinicId, String branchId);
	Response getByClinicId(String clinicId);
	}