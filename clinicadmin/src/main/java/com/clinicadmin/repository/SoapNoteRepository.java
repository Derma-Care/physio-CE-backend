package com.clinicadmin.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.clinicadmin.entity.SoapNote;

public interface SoapNoteRepository extends MongoRepository<SoapNote, String> {
	List<SoapNote> findByPatientId(String patientId);

	List<SoapNote> findByDoctorId(String doctorId);

	List<SoapNote> findByClinicIdAndBranchId(String clinicId, String branchId);

	SoapNote findByBookingId(String bookingId);

	// -------------------- New: clinic/branch combined lookups --------------------
	List<SoapNote> findByClinicId(String clinicId);

	List<SoapNote> findByClinicIdAndBranchIdAndDoctorId(String clinicId, String branchId, String doctorId);

	List<SoapNote> findByClinicIdAndBranchIdAndPatientId(String clinicId, String branchId, String patientId);

	List<SoapNote> findByClinicIdAndBranchIdAndBookingId(String clinicId, String branchId, String bookingId);

	Optional<SoapNote> findByClinicIdAndBranchIdAndBookingIdAndPatientId(String clinicId, String branchId,
			String bookingId, String patientId);
	
}
