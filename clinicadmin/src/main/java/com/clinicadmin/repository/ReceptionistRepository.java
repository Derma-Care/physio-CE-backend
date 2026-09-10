package com.clinicadmin.repository;

import java.util.List; // ✅ Added import
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.clinicadmin.entity.ReceptionistEntity;

public interface ReceptionistRepository extends MongoRepository<ReceptionistEntity, String> {

	boolean existsByContactNumber(String contactNumber);

//    Optional<ReceptionistEntity> findByUserName(String userName);

//    Optional<ReceptionistEntity> findByUserNameAndPassword(String userName, String password);

	Optional<ReceptionistEntity> findByContactNumber(String contactNumber);

	List<ReceptionistEntity> findByClinicId(String clinicId); // ✅ Now works fine

	Optional<ReceptionistEntity> findByClinicIdAndId(String clinicId, String receptionistId);

	List<ReceptionistEntity> findByClinicIdAndBranchId(String clinicId, String branchId);

	Optional<ReceptionistEntity> findByClinicIdAndBranchIdAndRoleIgnoreCase(
	        String clinicId,
	        String branchId,
	        String role);


}