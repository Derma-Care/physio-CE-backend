package com.clinicadmin.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.clinicadmin.entity.PhysioLiteSitting;

@Repository
public interface PhysioLiteSittingRepository extends MongoRepository<PhysioLiteSitting, String> {

	Optional<PhysioLiteSitting> findBySittingIdAndClinicIdAndBranchId(String sittingId, String clinicId,
			String branchId);

	boolean existsBySittingId(String sittingId);

	Optional<PhysioLiteSitting> findByIdAndClinicIdAndBranchId(String id, String clinicId, String branchId);

	List<PhysioLiteSitting> findByClinicId(String clinicId);

	List<PhysioLiteSitting> findByClinicIdAndBranchId(String clinicId, String branchId);
}
