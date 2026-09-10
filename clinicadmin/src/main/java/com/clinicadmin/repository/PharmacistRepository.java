package com.clinicadmin.repository;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.clinicadmin.entity.Pharmacist;

public interface PharmacistRepository extends MongoRepository<Pharmacist, ObjectId> {
	boolean existsByContactNumber(String contactNumber);

	List<Pharmacist> findByDepartment(String department);

	Optional<Pharmacist> findByPharmacistId(String pharmacistId);

//    Optional<Pharmacist> findByUserName(String userName);
	void deleteByPharmacistId(String pharmacistId);

	List<Pharmacist> findByHospitalId(String hospitalId);

	@Query("{ 'hospitalId': ?0, 'branchId': ?1 }")
	List<Pharmacist> findByHospitalIdAndBranchId(String hospitalId, String branchId);



}