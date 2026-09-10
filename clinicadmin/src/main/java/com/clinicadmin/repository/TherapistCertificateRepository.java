package com.clinicadmin.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.clinicadmin.dto.TherapistCertificateDTO;
import com.clinicadmin.entity.TherapistCertificate;

@Repository
public interface TherapistCertificateRepository
        extends MongoRepository<TherapistCertificate, String> {

    List<TherapistCertificate>findByClinicIdAndBranchId(String clinicId,String branchId);

    List<TherapistCertificate> findByClinicIdAndBranchIdAndTherapistId(
            String clinicId,
            String branchId,
            String therapistId);
}