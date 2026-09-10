package com.clinicadmin.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.clinicadmin.entity.TreatmentSchedule;

public interface TreatmentScheduleRepository extends MongoRepository<TreatmentSchedule, String> {
	Optional<TreatmentSchedule> findByClinicIdAndBranchIdAndBookingIdAndPatientId(
			String clinicId, String branchId, String bookingId, String patientId);

	Optional<TreatmentSchedule> findBySoapNoteId(String soapNoteId);

	//Optional<TreatmentSchedule> findByClinicIdAndBranchId(String clinicId, String branchId);

   List<TreatmentSchedule> findByClinicIdAndBranchId(String clinicId, String branchId);


    @Query(value = "{ 'clinicId' : ?0, 'branchId' : ?1, 'sittings.sittingsId' : ?2 }")
	Optional<TreatmentSchedule> findByClinicIdAndBranchIdAndSittingsId(
	        String clinicId,
	        String branchId,
	        String sittingsId);


    TreatmentSchedule findByBookingId(String bookingId);

    
    List<TreatmentSchedule> findByClinicIdAndBranchIdAndSessionStartDateBetween(String clinicId, String branchId, String string, String string1);

    List<TreatmentSchedule> findByClinicIdAndBranchIdAndPatientId(String clinicId, String branchId, String patientId);
	List<TreatmentSchedule> findAllByClinicIdAndBranchId(String clinicId, String branchId);


    List<TreatmentSchedule> findByClinicIdAndBranchIdAndDoctorIdAndPatientId(String clinicId, String branchId, String doctorId, String patientId);

    List<TreatmentSchedule> findByClinicIdAndBranchIdAndDoctorIdAndPatientIdAndBookingId(String clinicId, String branchId, String doctorId, String patientId,String bookingId);

    List<TreatmentSchedule> findByClinicIdAndBranchIdAndDoctorId(String clinicId, String branchId, String doctorId);

    List<TreatmentSchedule> findByClinicIdAndBranchIdAndPatientIdAndBookingId(String clinicId, String branchId, String patientId, String bookingId);

    TreatmentSchedule findByClinicIdAndBranchIdAndBookingId(String clinicId, String branchId, String bookingId);

    Optional<TreatmentSchedule> findByClinicIdAndBranchIdAndSittings_SittingsId(
            String clinicId,
            String branchId,
            String sittingsId
    );
}