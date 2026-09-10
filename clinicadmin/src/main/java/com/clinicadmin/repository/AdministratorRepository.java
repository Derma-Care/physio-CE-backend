package com.clinicadmin.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.clinicadmin.dto.AdministratorDTO;
import com.clinicadmin.entity.Administrator;

@Repository
public interface AdministratorRepository extends MongoRepository<Administrator, String> {

   
    List<Administrator> findByClinicId(String clinicId);

    Optional<AdministratorDTO> findByEmailId(String email);

    boolean existsByEmailId(String email);

	boolean existsByContactNumber(String contactNumber);
	
	Optional<Administrator> findByClinicIdAndAdminId(String clinicId, String id);

	List<Administrator> findByClinicIdAndBranchId(String clinicId, String branchId);

	Optional<Administrator> findByClinicIdAndBranchIdAndAdminId(String clinicId, String branchId, String adminId);
}
