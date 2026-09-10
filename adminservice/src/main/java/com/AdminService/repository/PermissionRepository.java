package com.AdminService.repository;

import com.AdminService.entity.Permission;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PermissionRepository extends MongoRepository<Permission, String> {

	Optional<Permission> findTopByOrderByIdAsc();

    

}