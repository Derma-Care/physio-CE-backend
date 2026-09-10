package com.clinicadmin.repository;

import com.clinicadmin.entity.PatientFeedback;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface PatientFeedbackRepository extends MongoRepository<PatientFeedback, String> {

	List<PatientFeedback> findByClinicIdAndBranchId(String clinicId, String branchId);

    List<PatientFeedback> findByClinicIdAndDoctorFeedbackTargetId(
            String clinicId,
            String targetId);
    
    List<PatientFeedback> findByClinicIdAndBranchIdAndPatientId(String clinicId, String branchId,String patientId);

	List<PatientFeedback> findByClinicIdAndBranchIdAndTherapistFeedbackTargetId(String clinicId, String branchId,
			String therapistId);




}