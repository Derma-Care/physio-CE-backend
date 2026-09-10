package com.clinicadmin.repository;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.clinicadmin.entity.Treatment;

@Repository
public interface TreatmentRepository extends MongoRepository<Treatment, ObjectId> {
	public Optional<Treatment> findByIdAndHospitalId(ObjectId id, String hospitalId);

	List<Treatment> findByHospitalId(String hospitalId);

	Optional<Treatment> findByHospitalIdAndTreatmentName(String hospitalId, String treatmentName);
}
