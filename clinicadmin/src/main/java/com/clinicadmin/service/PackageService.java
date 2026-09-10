package com.clinicadmin.service;

import com.clinicadmin.dto.PackageDTO;
import com.clinicadmin.dto.ResponseStructure;

import java.util.List;

public interface PackageService {

    
    ResponseStructure<PackageDTO> createPackage(PackageDTO dto);

    
    ResponseStructure<PackageDTO> getPackageById(String id);

    ResponseStructure<List<PackageDTO>> getAllPackages();

    ResponseStructure<List<PackageDTO>> getByClinicAndBranch(String clinicId, String branchId);

   
    ResponseStructure<PackageDTO> getByClinicBranchAndPackageId(
            String clinicId,
            String branchId,
            String packageId
    );

    ResponseStructure<PackageDTO> updatepackagebyid(String id, PackageDTO dto);

   
    ResponseStructure<String> deletepackagebyid(String id);
}