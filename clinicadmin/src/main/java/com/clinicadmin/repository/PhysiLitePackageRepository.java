package com.clinicadmin.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.clinicadmin.entity.PhysiLitePackage;

@Repository
public interface PhysiLitePackageRepository extends MongoRepository<PhysiLitePackage, String> {

	List<PhysiLitePackage> findByClinicId(String clinicId);

	List<PhysiLitePackage> findByClinicIdAndBranchId(String clinicId, String branchId);

	Optional<PhysiLitePackage> findByIdAndClinicIdAndBranchId(String id, String clinicId, String branchId);

	boolean existsByPackageId(String packageId);

	Optional<PhysiLitePackage> findByPackageIdAndClinicIdAndBranchId(String packageId, String clinicId, String branchId);

}
