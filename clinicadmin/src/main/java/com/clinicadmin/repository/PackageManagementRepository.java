package com.clinicadmin.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.clinicadmin.entity.PackageManagement;

public interface PackageManagementRepository extends MongoRepository<PackageManagement, String> {

    List<PackageManagement> findByClinicIdAndBranchId(String clinicId, String branchId);

    Optional<PackageManagement> findByClinicIdAndBranchIdAndPackageId(
            String clinicId, String branchId, String packageId);

    Optional<PackageManagement> findByPackageId(String packageId);
    List<PackageManagement> findByClinicId(String clinicId);
}