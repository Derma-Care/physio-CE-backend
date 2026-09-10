package com.clinicadmin.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.clinicadmin.entity.TherapistAssignment;

@Repository
public interface TherapistAssignmentRepository
        extends MongoRepository<TherapistAssignment, String> {

    Optional<TherapistAssignment> findByTherapistRecordId(
            String therapistRecordId);
}