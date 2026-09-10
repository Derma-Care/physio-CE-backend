package com.clinicadmin.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.clinicadmin.entity.FeedbackDetails;

public interface FeedbackDetailsRepository
        extends MongoRepository<FeedbackDetails, String> {

	List<FeedbackDetails> findByClinicIdAndBranchId(String clinicId, String branchId);

	List<FeedbackDetails> findByClinicIdAndBranchIdAndTherapistId(String clinicId, String branchId, String therapistId);

	Optional<FeedbackDetails> findByBookingId(String bookingId);

//	List<FeedbackDetails> findByClinicIdAndDoctorId(String clinicId, String doctorId);

}
