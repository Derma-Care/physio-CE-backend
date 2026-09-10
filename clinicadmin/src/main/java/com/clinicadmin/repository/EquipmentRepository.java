package com.clinicadmin.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.clinicadmin.entity.Equipment;

@Repository
public interface EquipmentRepository
        extends MongoRepository<Equipment, String> {

    List<Equipment> findByClinicIdAndBranchId(
            String clinicId,
            String branchId);
}


