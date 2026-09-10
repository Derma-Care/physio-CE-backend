package com.clinicadmin.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.clinicadmin.entity.Payment;
import com.clinicadmin.entity.TreatmentSchedule;

@Repository
public interface PaymentRepository extends MongoRepository<Payment, String> {




	List<Payment> findByClinicIdAndBranchId(String clinicId, String branchId);

	boolean existsByBillingId(String billingId);

	Optional<Payment> findByBillingId(String billingId);
	Optional<Payment> findByBookingId(String bookinId);

	Optional<Payment> findByClinicIdAndBranchIdAndBillingId(String clinicId, String branchId, String billingId);




	List<Payment> findByClinicIdAndBranchIdAndPatientIdAndBookingId(String clinicId, String branchId,
	        String patientId, String bookingId);

	boolean existsByTransactionHistory_ReceiptNumber(String receiptNumber);

	Optional<Payment> findByBookingIdIgnoreCase(String id);

	
}