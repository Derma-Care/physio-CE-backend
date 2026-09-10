package com.AdminService.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.AdminService.entity.BranchCredentials;

public interface BranchCredentialsRepository extends MongoRepository<BranchCredentials, String> {
   
	BranchCredentials findByUserName(String userName);

	BranchCredentials findByUserNameAndPassword(String userName, String password);

	List<BranchCredentials> findByBranchId(String branchId);

	void deleteByBranchId(String branchId);
	
	BranchCredentials findByMobilenumber(String mobilenumber);
	
	BranchCredentials findByMobilenumberAndRole(String mobilenumber, String role);

    BranchCredentials findByUserNameIgnoreCaseAndPasswordIgnoreCase(String userName, String password);
}
