package com.clinicadmin.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.clinicadmin.entity.TherapyService;

public interface TherapyServiceRepository extends MongoRepository<TherapyService, String> {

    List<TherapyService> findByClinicIdAndBranchId(String clinicId, String branchId);

    Optional<TherapyService> findByIdAndClinicIdAndBranchId(String id, String clinicId, String branchId);
    Optional<TherapyService> findByClinicId(String clinicId);
  

}