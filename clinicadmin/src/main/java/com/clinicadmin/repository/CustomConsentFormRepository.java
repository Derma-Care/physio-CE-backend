package com.clinicadmin.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.clinicadmin.entity.CustomConsentForm;

@Repository
public interface CustomConsentFormRepository extends MongoRepository<CustomConsentForm, String> {
	// For generic consent form (only one expected)
    Optional<CustomConsentForm> findByHospitalIdAndConsentFormType(String hospitalId, String consentFormType);

    // For procedure consent forms (multiple possible)
    List<CustomConsentForm> findAllByHospitalIdAndConsentFormType(String hospitalId, String consentFormType);

	Optional<CustomConsentForm> findByHospitalIdAndSubServiceid(String hospitalId, String subServiceid);

	List<CustomConsentForm> findByHospitalId(String hospitalId);
}
