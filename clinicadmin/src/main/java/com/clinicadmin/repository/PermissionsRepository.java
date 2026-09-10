package com.clinicadmin.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.clinicadmin.entity.Permissions;

@Repository
public interface PermissionsRepository extends MongoRepository<Permissions, String> {

    // ✅ 1️⃣ Get all permissions for a clinic
    List<Permissions> findByClinicId(String clinicId);

    // ✅ 2️⃣ Get all permissions for a specific branch under a clinic
    List<Permissions> findByClinicIdAndBranchId(String clinicId, String branchId);

    // ✅ 3️⃣ Get permissions for a specific user in a clinic and branch
    Optional<Permissions> findByClinicIdAndBranchIdAndUserId(String clinicId, String branchId, String userId);

    // ✅ 4️⃣ Get permissions for a specific role type (like Nurse, WardBoy, LabTech, etc.)
    List<Permissions> findByClinicIdAndBranchIdAndRole(String clinicId, String branchId, String role);

    // ✅ 5️⃣ Optional: Get by clinic, branch, role, and user — for unique record checks
    Optional<Permissions> findByClinicIdAndBranchIdAndRoleAndUserId(String clinicId, String branchId, String role, String userId);
}
