package com.AdminService.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.AdminService.entity.ClinicTiming;

public interface ClinicTimingRepository
        extends MongoRepository<ClinicTiming, String> {

    List<ClinicTiming> findAllByOrderByStartHourAsc();
}