package com.clinicadmin.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.clinicadmin.entity.LabTechnicianEntity;

public interface LabTechnicianRepository extends MongoRepository<LabTechnicianEntity, String> {

	// ✅ Find by contact number (unique, also acts as username)
	Optional<LabTechnicianEntity> findByContactNumber(String contactNumber);

//	// ✅ Find by username (can be contactNumber or separate userName field)
//	Optional<LabTechnicianEntity> findByUserName(String userName);
//
//	// ✅ Login check (username + password)
//	Optional<LabTechnicianEntity> findByUserNameAndPassword(String userName, String password);

	// ✅ Use @Query to correctly map the field for department
	@Query("{ 'departmentOrAssignedLab' : ?0 }")
	List<LabTechnicianEntity> findByDepartmentOrAssignedLab(String departmentOrAssignedLab);

	// ✅ Find by specialization
	List<LabTechnicianEntity> findBySpecialization(String specialization);

	// ✅ Check uniqueness for contact number
	boolean existsByContactNumber(String contactNumber);

	// ⚠️ Not recommended in real apps, but useful for random password generation
//	boolean existsByPassword(String password);

	Optional<LabTechnicianEntity> findByClinicIdAndId(String clinicId, String technicianId);

	List<LabTechnicianEntity> findByClinicId(String clinicId);

	List<LabTechnicianEntity> findByClinicIdAndBranchId(String clinicId, String branchId);

}