package com.clinicadmin.repository;


import java.time.Instant;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.clinicadmin.entity.TestOrder;

@Repository
public interface TestOrderRepository extends MongoRepository<TestOrder, String> {

    List<TestOrder> findByClinicIdAndBranchId(String clinicId, String branchId);

	TestOrder findByClinicIdAndBranchIdAndId(String clinicId, String branchId, String id);

	List<TestOrder> findByClinicIdAndBranchIdAndPatientIdAndBookingId(
	        String clinicId,
	        String branchId,
	        String patientId,
	        String bookingId);

	List<TestOrder> findByClinicIdAndBranchIdAndTestNameId(String clinicId, String branchId, String testNameId);

	List<TestOrder> findByClinicIdAndBranchIdAndOrderedAtBetween(
            String clinicId,
            String branchId,
            Instant startDate,
            Instant endDate);

    List<TestOrder> findByClinicIdAndBranchIdAndTestNameIdAndOrderedAtBetween(
            String clinicId,
            String branchId,
            String testNameId,
            Instant startDate,
            Instant endDate);

	List<TestOrder> findByClinicIdAndBranchIdAndVendorIdAndOrderedAtBetween(String clinicId, String branchId,
			String vendorId, Instant instant, Instant instant2);

	List<TestOrder> findByClinicIdAndBranchIdAndVendorIdAndTestNameId(String clinicId, String branchId, String vendorId,
			String testNameId);

	List<TestOrder> findByClinicIdAndBranchIdAndVendorId(String clinicId, String branchId, String vendorId);
}