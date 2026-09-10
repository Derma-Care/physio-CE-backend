package com.clinicadmin.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.clinicadmin.entity.Test;

public interface TestRepository extends MongoRepository<Test, String> {

    Optional<Test> findByTestId(String testId);

    boolean existsByTestNameAndClinicIdAndBranchId(
            String testName,
            String clinicId,
            String branchId);

    Optional<Test> findByClinicIdAndBranchIdAndTestId(
            String clinicId,
            String branchId,
            String testId);

    List<Test> findByClinicIdAndBranchId(
            String clinicId,
            String branchId);
}