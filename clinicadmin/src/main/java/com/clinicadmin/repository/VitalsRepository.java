package com.clinicadmin.repository;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.clinicadmin.entity.Vitals;
@Repository
public interface VitalsRepository extends MongoRepository<Vitals, ObjectId>{
	
	Optional<Vitals> findByPatientId(String patientId);

	void deleteByBookingIdAndPatientId(String bookingId, String patientId);

	List<Vitals> findByBookingIdAndPatientId(String bookingId, String patientId);

	Optional<Vitals> findByBookingId(String bookingId);

}