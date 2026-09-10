package com.AdminService.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.AdminService.entity.ClinicCredentials;

@Repository
public interface ClinicCredentialsRepository extends MongoRepository<ClinicCredentials, String> {

	List<ClinicCredentials> findAllByUserName(String name);

	ClinicCredentials findByPassword(String password);

	ClinicCredentials findByUserNameAndPassword(String name, String password);

	void deleteByUserName(String name);

	ClinicCredentials findByUserName(String userName);

	ClinicCredentials findByMobilenumber(String mobilenumber);

	ClinicCredentials findByMobilenumberAndRole(String mobilenumber, String role);

	ClinicCredentials findByUserNameAndRole(String userName, String role);


	ClinicCredentials findByUserNameIgnoreCaseAndPasswordIgnoreCase(String userName, String password);
}
