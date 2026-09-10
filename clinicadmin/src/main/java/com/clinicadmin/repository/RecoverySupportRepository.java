package com.clinicadmin.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.clinicadmin.entity.RecoverySupport;

public interface RecoverySupportRepository extends MongoRepository<RecoverySupport, String> {

    List<RecoverySupport> findByClinicId(String clinicId);

	Optional<RecoverySupport> findByClinicIdAndId(String clinicId, String id);

}