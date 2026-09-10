package com.clinicadmin.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.clinicadmin.entity.DoctorAndStaffLoginCredentials;

public interface DoctorLoginCredentialsRepository extends MongoRepository<DoctorAndStaffLoginCredentials, String> {

	Optional<DoctorAndStaffLoginCredentials> findByUsername(String username);

	Optional<DoctorAndStaffLoginCredentials> findByUsernameAndRole(String username, String role);

	DoctorAndStaffLoginCredentials findByMobilenumberAndRole(String mobilenumber, String role);

	boolean existsByUsername(String username); // fixed

	Optional<DoctorAndStaffLoginCredentials> findByStaffId(String staffId);

	List<DoctorAndStaffLoginCredentials> findByHospitalIdAndBranchId(String hospitalId, String branchId);

	void deleteByStaffId(String therapistId);

	Optional<DoctorAndStaffLoginCredentials> findByMobilenumber(String mobileNumber);

    DoctorAndStaffLoginCredentials findByMobilenumberAndRoleIgnoreCase(String mobileNumber, String normalizedRole);
}
