package com.clinicadmin.repository;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.clinicadmin.entity.Nurse;

@Repository
public interface NurseRepository extends MongoRepository<Nurse, ObjectId> {
	boolean existsByNurseContactNumber(String nurseContactNumber);

	boolean existsByNurseId(String nurseId);

	Optional<Nurse> findByHospitalIdAndNurseId(String hospitalId, String nurseId);
	

	Optional<Nurse> findByNurseId(String nurseId);

	List<Nurse> findByHospitalId(String hospitalId);

	void deleteByHospitalIdAndNurseId(String hospitalId, String nurseId);

	List<Nurse> findByHospitalIdAndBranchId(String hospitalId, String branchId);

//	Optional<Nurse> findByUserName(String userName);
}
