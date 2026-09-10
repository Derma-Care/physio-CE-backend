package com.clinicadmin.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.clinicadmin.entity.Package;

import java.util.List;

public interface PackageRepository extends MongoRepository<Package, String> {

    List<Package> findByClinicIdAndBranchId(String clinicId, String branchId);
}