package com.clinicadmin.repository;



import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.clinicadmin.entity.AddNewTest;



@Repository
public interface AddNewTestRepository extends MongoRepository<AddNewTest, String> {

    boolean existsById(String id);

    List<AddNewTest> findByClinicId(String clinicId);

    List<AddNewTest> findByBranchId(String branchId);

    List<AddNewTest> findByClinicIdAndBranchId(String clinicId, String branchId);

	AddNewTest findByClinicIdAndBranchIdAndId(String clinicId, String branchId, String id);

}