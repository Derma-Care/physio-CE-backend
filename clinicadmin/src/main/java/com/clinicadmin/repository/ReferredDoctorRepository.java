package com.clinicadmin.repository;



import org.springframework.data.mongodb.repository.MongoRepository;

import com.clinicadmin.dto.ReferredDoctorDTO;
import com.clinicadmin.entity.ReferredDoctor;

import java.util.List;
import java.util.Optional;

public interface ReferredDoctorRepository extends MongoRepository<ReferredDoctor, String> {
    Optional<ReferredDoctor> findByReferralId(String referralId);
    boolean existsByReferralId(String referralId);
	boolean existsByMobileNumber(String mobileNumber);
	List<ReferredDoctorDTO> findByClinicId(String clinicId);
}