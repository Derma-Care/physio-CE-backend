package com.clinicadmin.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.clinicadmin.entity.Therapist;
import com.clinicadmin.entity.TherapistRecord;

public interface TherapistRecordRepository extends MongoRepository<TherapistRecord, String> {

    Optional<TherapistRecord> findByClinicIdAndBranchIdAndTherapistRecordId(
            String clinicId, String branchId, String therapistRecordId);
    Optional<TherapistRecord> findByClinicIdAndBranchIdAndTherapistRecordIdAndSessionId(
            String clinicId,
            String branchId,
            String therapistRecordId,
            String sessionId
    );
  List<TherapistRecord> findAllByPatientIdAndBookingId(String patientId, String bookingId);
List<TherapistRecord> findAllByPatientIdAndBookingIdAndTherapistRecordId(String patientId, String bookingId,
		String therapistRecordId);
Optional<TherapistRecord> findByClinicIdAndBranchIdAndBookingIdAndPatientIdAndSessionId(String clinicId,
		String branchId, String bookingId, String patientId, String sessionId);
List<TherapistRecord> findByTherapistIdAndCompletedDate(String therapistId, String date);
List<TherapistRecord> findByTherapistIdAndCompletedDateStartingWith(String therapistId, String month);
void deleteBySessionId(String sessionId);
Optional<TherapistRecord> findByClinicIdAndBranchIdAndPatientIdAndBookingIdAndTherapistIdAndTherapistRecordId(
		String clinicId, String branchId, String patientId, String bookingId, String therapistId,
		String therapistRecordId);
List<TherapistRecord> findByClinicIdAndBranchIdAndTherapistId(String clinicId, String branchId, String therapistId);	
}