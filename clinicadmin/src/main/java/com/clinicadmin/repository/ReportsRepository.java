package com.clinicadmin.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.clinicadmin.entity.ReportsList;

@Repository
public interface ReportsRepository extends MongoRepository<ReportsList, String> {
	
	@Query("{ 'reportsList.bookingId': ?0 }")
	List<ReportsList> findByReportsListBookingId(String bookingId);
	
	List<ReportsList> findByCustomerId(String customerId);

	List<ReportsList> findByReportsListPatientIdAndReportsListBookingId(String patientId, String bookingId);

}
