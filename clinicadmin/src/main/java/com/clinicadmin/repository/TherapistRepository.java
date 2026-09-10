package com.clinicadmin.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.clinicadmin.entity.Therapist;

@Repository
public interface TherapistRepository extends MongoRepository<Therapist, String> {

    Optional<Therapist> findByTherapistId(String therapistId);

    void deleteByTherapistId(String therapistId);

    boolean existsByContactNumber(String contactNumber);

    Optional<Therapist> findByUserName(String userName);

    List<Therapist> findByClinicIdAndBranchId(String clinicId, String branchId);

    List<Therapist> findByClinicId(String clinicId);

	Optional<Therapist> findByClinicIdAndBranchIdAndTherapistId(String clinicId, String branchId, String therapistId);
}