package com.clinicadmin.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.clinicadmin.entity.WardBoy;

@Repository
public interface WardBoyRepository extends MongoRepository<WardBoy, String> {



	Optional<WardBoy> findByClinicIdAndBranchIdAndWardBoyId(String clinicId, String branchId, String userId);

	List<WardBoy> findByClinicId(String clinicId);

	Optional<WardBoy> findByContactNumber(String contactNumber);

	Optional<WardBoy> findAllByClinicId(String clinicId);

	Optional<WardBoy> findByWardBoyIdAndClinicId(String wardBoyId, String clinicId);

	Optional<WardBoy> findAllByClinicIdAndBranchId(String clinicId, String branchId);

	List<WardBoy> findByClinicIdAndBranchId(String clinicId, String branchId);

	Map<String, List<String>> findByBranchId(String branchId);

}