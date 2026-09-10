package com.clinicadmin.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.clinicadmin.dto.PrivacyPolicyDTO;
import com.clinicadmin.entity.PrivacyPolicy;

public interface  PrivacyPolicyRepository extends MongoRepository<PrivacyPolicy,String>{

	List<PrivacyPolicyDTO> findByClinicId(String clinicId);

}