package com.clinicadmin.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.clinicadmin.entity.HomeVisitTracking;

public interface HomeVisitTrackingRepository
        extends MongoRepository<HomeVisitTracking, String> {

    Optional<HomeVisitTracking> findBySittingsId(String sittingsId);

    boolean existsBySittingsId(String sittingsId);
}