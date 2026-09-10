package com.clinicadmin.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.clinicadmin.entity.Billing;

@Repository
public interface BillingRepository extends MongoRepository<Billing, String> {

    List<Billing> findByClinicIdAndBranchId(
            String clinicId,
            String branchId);

    Optional<Billing> findByBillingIdAndClinicIdAndBranchId(
            String billingId,
            String clinicId,
            String branchId);

	List<Billing> findByClinicId(String clinicId);
}