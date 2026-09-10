package com.clinicadmin.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.clinicadmin.entity.PatientConsentForm;

public interface PatientConsentFormRepository extends MongoRepository<PatientConsentForm, String> {

}
