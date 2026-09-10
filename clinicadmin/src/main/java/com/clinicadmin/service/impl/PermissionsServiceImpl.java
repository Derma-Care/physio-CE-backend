package com.clinicadmin.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.clinicadmin.dto.PermissionsDTO;
import com.clinicadmin.dto.ResponseStructure;
import com.clinicadmin.entity.LabTechnicianEntity;
import com.clinicadmin.entity.Nurse;
import com.clinicadmin.entity.Pharmacist;
import com.clinicadmin.entity.SecurityStaff;
import com.clinicadmin.entity.WardBoy;
import com.clinicadmin.feignclient.AdminServiceClient;
import com.clinicadmin.repository.LabTechnicianRepository;
import com.clinicadmin.repository.NurseRepository;
import com.clinicadmin.repository.PharmacistRepository;
import com.clinicadmin.repository.SecurityStaffRepository;
import com.clinicadmin.repository.WardBoyRepository;
import com.clinicadmin.service.PermissionsService;

import feign.FeignException;

@Service
public class PermissionsServiceImpl implements PermissionsService {

    @Autowired
    private SecurityStaffRepository securityStaffRepository;

    @Autowired
    private WardBoyRepository wardBoyRepository;

    @Autowired
    private LabTechnicianRepository labTechnicianRepository;

    @Autowired
    private NurseRepository nurseRepository;

    @Autowired
    private PharmacistRepository pharmacistRepository;
    
    @Autowired
    private AdminServiceClient adminServiceClient;

    // ✅ Get permissions for specific user
    @Override
    public ResponseStructure<PermissionsDTO> getPermissionsByClinicBranchAndUser(String clinicId, String branchId, String userId) {

        // 🔹 SecurityStaff
        Optional<SecurityStaff> securityOpt = securityStaffRepository
                .findByClinicIdAndBranchIdAndSecurityStaffId(clinicId, branchId, userId);
        if (securityOpt.isPresent()) {
            SecurityStaff staff = securityOpt.get();
            PermissionsDTO dto = new PermissionsDTO(staff.getClinicId(), staff.getBranchId(),
                    staff.getSecurityStaffId(), "SecurityStaff", staff.getPermissions());
            return ResponseStructure.buildResponse(dto,
                    "Permissions fetched from Security Staff", HttpStatus.OK, HttpStatus.OK.value());
        }

        // 🔹 WardBoy
        Optional<WardBoy> wardOpt = wardBoyRepository
                .findByClinicIdAndBranchIdAndWardBoyId(clinicId, branchId, userId);
        if (wardOpt.isPresent()) {
            WardBoy ward = wardOpt.get();
            PermissionsDTO dto = new PermissionsDTO(ward.getClinicId(), ward.getBranchId(),
                    ward.getWardBoyId(), "WardBoy", ward.getPermissions());
            return ResponseStructure.buildResponse(dto,
                    "Permissions fetched from WardBoy", HttpStatus.OK, HttpStatus.OK.value());
        }

        // 🔹 LabTechnician
        Optional<LabTechnicianEntity> labOpt = labTechnicianRepository.findByClinicIdAndId(clinicId, userId);
        if (labOpt.isPresent()) {
            LabTechnicianEntity lab = labOpt.get();
            PermissionsDTO dto = new PermissionsDTO(lab.getClinicId(), lab.getBranchId(),
                    lab.getId(), "LabTechnician", lab.getPermissions());
            return ResponseStructure.buildResponse(dto,
                    "Permissions fetched from Lab Technician", HttpStatus.OK, HttpStatus.OK.value());
        }

        // 🔹 Nurse
        Optional<Nurse> nurseOpt = nurseRepository.findByHospitalIdAndNurseId(clinicId, userId);
        if (nurseOpt.isPresent()) {
            Nurse nurse = nurseOpt.get();
            PermissionsDTO dto = new PermissionsDTO(nurse.getHospitalId(), nurse.getBranchId(),
                    nurse.getNurseId(), "Nurse", nurse.getPermissions());
            return ResponseStructure.buildResponse(dto,
                    "Permissions fetched from Nurse", HttpStatus.OK, HttpStatus.OK.value());
        }

        // 🔹 Pharmacist
        Optional<Pharmacist> pharmOpt = pharmacistRepository.findByPharmacistId(userId);
        if (pharmOpt.isPresent()) {
            Pharmacist pharm = pharmOpt.get();
            PermissionsDTO dto = new PermissionsDTO(pharm.getHospitalId(), pharm.getBranchId(),
                    pharm.getPharmacistId(), "Pharmacist", pharm.getPermissions());
            return ResponseStructure.buildResponse(dto,
                    "Permissions fetched from Pharmacist", HttpStatus.OK, HttpStatus.OK.value());
        }

        // ❌ Not found
        return ResponseStructure.buildResponse(null,
                "No permissions found for user ID: " + userId,
                HttpStatus.NOT_FOUND,
                HttpStatus.NOT_FOUND.value());
    }

    // ✅ Update permissions
    @Override
    public ResponseEntity<ResponseStructure<PermissionsDTO>> updatePermissionsById(String userId, PermissionsDTO dto) {

        // WardBoy
        Optional<WardBoy> wardOpt = wardBoyRepository
                .findByClinicIdAndBranchIdAndWardBoyId(dto.getClinicId(), dto.getBranchId(), userId);
        if (wardOpt.isPresent()) {
            WardBoy wardBoy = wardOpt.get();
            wardBoy.setPermissions(dto.getPermissions());
            wardBoyRepository.save(wardBoy);

            return ResponseEntity.ok(ResponseStructure.buildResponse(dto,
                    "Permissions updated successfully for WardBoy", HttpStatus.OK, HttpStatus.OK.value()));
        }

        // SecurityStaff
        Optional<SecurityStaff> staffOpt = securityStaffRepository
                .findByClinicIdAndBranchIdAndSecurityStaffId(dto.getClinicId(), dto.getBranchId(), userId);
        if (staffOpt.isPresent()) {
            SecurityStaff staff = staffOpt.get();
            staff.setPermissions(dto.getPermissions());
            securityStaffRepository.save(staff);

            return ResponseEntity.ok(ResponseStructure.buildResponse(dto,
                    "Permissions updated successfully for Security Staff", HttpStatus.OK, HttpStatus.OK.value()));
        }

        // LabTechnician
        Optional<LabTechnicianEntity> labOpt = labTechnicianRepository
                .findByClinicIdAndId(dto.getClinicId(), userId);
        if (labOpt.isPresent()) {
            LabTechnicianEntity lab = labOpt.get();
            lab.setPermissions(dto.getPermissions());
            labTechnicianRepository.save(lab);

            return ResponseEntity.ok(ResponseStructure.buildResponse(dto,
                    "Permissions updated successfully for Lab Technician", HttpStatus.OK, HttpStatus.OK.value()));
        }

        // Nurse
        Optional<Nurse> nurseOpt = nurseRepository.findByHospitalIdAndNurseId(dto.getClinicId(), userId);
        if (nurseOpt.isPresent()) {
            Nurse nurse = nurseOpt.get();
            nurse.setPermissions(dto.getPermissions());
            nurseRepository.save(nurse);

            return ResponseEntity.ok(ResponseStructure.buildResponse(dto,
                    "Permissions updated successfully for Nurse", HttpStatus.OK, HttpStatus.OK.value()));
        }

        // Pharmacist
        Optional<Pharmacist> pharmOpt = pharmacistRepository.findByPharmacistId(userId);
        if (pharmOpt.isPresent()) {
            Pharmacist pharm = pharmOpt.get();
            pharm.setPermissions(dto.getPermissions());
            pharmacistRepository.save(pharm);

            return ResponseEntity.ok(ResponseStructure.buildResponse(dto,
                    "Permissions updated successfully for Pharmacist", HttpStatus.OK, HttpStatus.OK.value()));
        }

        // Not Found
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseStructure.buildResponse(null,
                "User not found with ID: " + userId, HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.value()));
    }

    // ✅ Get all permissions by Clinic ID
    @Override
    public ResponseStructure<List<PermissionsDTO>> getPermissionsByClinicId(String clinicId) {
        List<PermissionsDTO> resultList = new ArrayList<>();

        // Security Staff
        for (SecurityStaff staff : securityStaffRepository.findByClinicId(clinicId)) {
            resultList.add(new PermissionsDTO(staff.getClinicId(), staff.getBranchId(),
                    staff.getSecurityStaffId(), "SecurityStaff", staff.getPermissions()));
        }

        // WardBoy
        for (WardBoy ward : wardBoyRepository.findByClinicId(clinicId)) {
            resultList.add(new PermissionsDTO(ward.getClinicId(), ward.getBranchId(),
                    ward.getWardBoyId(), "WardBoy", ward.getPermissions()));
        }

        // LabTechnician
        for (LabTechnicianEntity lab : labTechnicianRepository.findByClinicId(clinicId)) {
            resultList.add(new PermissionsDTO(lab.getClinicId(), lab.getBranchId(),
                    lab.getId(), "LabTechnician", lab.getPermissions()));
        }

        // Nurse
        for (Nurse nurse : nurseRepository.findByHospitalId(clinicId)) {
            resultList.add(new PermissionsDTO(nurse.getHospitalId(), nurse.getBranchId(),
                    nurse.getNurseId(), "Nurse", nurse.getPermissions()));
        }

        // Pharmacist
        for (Pharmacist pharm : pharmacistRepository.findByHospitalId(clinicId)) {
            resultList.add(new PermissionsDTO(pharm.getHospitalId(), pharm.getBranchId(),
                    pharm.getPharmacistId(), "Pharmacist", pharm.getPermissions()));
        }

        return ResponseStructure.buildResponse(resultList,
                "Permissions fetched successfully for Clinic ID: " + clinicId,
                HttpStatus.OK, HttpStatus.OK.value());
    }

    // ✅ Get all permissions by Clinic ID and Branch ID
    @Override
    public ResponseStructure<List<PermissionsDTO>> getPermissionsByClinicAndBranch(String clinicId, String branchId) {
        List<PermissionsDTO> resultList = new ArrayList<>();

        // Security Staff
        for (SecurityStaff staff : securityStaffRepository.findByClinicIdAndBranchId(clinicId, branchId)) {
            resultList.add(new PermissionsDTO(staff.getClinicId(), staff.getBranchId(),
                    staff.getSecurityStaffId(), "SecurityStaff", staff.getPermissions()));
        }

        // WardBoy
        for (WardBoy ward : wardBoyRepository.findByClinicIdAndBranchId(clinicId, branchId)) {
            resultList.add(new PermissionsDTO(ward.getClinicId(), ward.getBranchId(),
                    ward.getWardBoyId(), "WardBoy", ward.getPermissions()));
        }

        // LabTechnician
        for (LabTechnicianEntity lab : labTechnicianRepository.findByClinicIdAndBranchId(clinicId, branchId)) {
            resultList.add(new PermissionsDTO(lab.getClinicId(), lab.getBranchId(),
                    lab.getId(), "LabTechnician", lab.getPermissions()));
        }

        // Nurse
        for (Nurse nurse : nurseRepository.findByHospitalIdAndBranchId(clinicId, branchId)) {
            resultList.add(new PermissionsDTO(nurse.getHospitalId(), nurse.getBranchId(),
                    nurse.getNurseId(), "Nurse", nurse.getPermissions()));
        }

        // Pharmacist
        for (Pharmacist pharm : pharmacistRepository.findByHospitalIdAndBranchId(clinicId, branchId)) {
            resultList.add(new PermissionsDTO(pharm.getHospitalId(), pharm.getBranchId(),
                    pharm.getPharmacistId(), "Pharmacist", pharm.getPermissions()));
        }

        if (resultList.isEmpty()) {
            return ResponseStructure.buildResponse(resultList,
                    "No permissions found for Clinic ID: " + clinicId + " and Branch ID: " + branchId,
                    HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.value());
        }

        return ResponseStructure.buildResponse(resultList,
                "Permissions fetched successfully for Clinic ID: " + clinicId + " and Branch ID: " + branchId,
                HttpStatus.OK, HttpStatus.OK.value());
    }

    @Override
    public ResponseStructure<PermissionsDTO> getPermissionsByUserId(String userId) {
        return ResponseStructure.buildResponse(null,
                "Feature not implemented yet: getPermissionsByUserId", HttpStatus.NOT_IMPLEMENTED, 501);
    }

    @Override
    public ResponseStructure<List<PermissionsDTO>> getPermissionsByBranchId(String branchId) {
        return ResponseStructure.buildResponse(null,
                "Feature not implemented yet: getPermissionsByBranchId", HttpStatus.NOT_IMPLEMENTED, 501);
    }
    
    @Override
    public ResponseEntity<Map<String, List<String>>> getDefaultAdminPermissions() {
        try {
            // Call Admin Service using Feign
            return adminServiceClient.getDefaultAdminPermissions();

        } catch (FeignException e) {
            // If AdminService throws an error, capture it here
            throw new RuntimeException("Failed to fetch default admin permissions from Admin Service. " +
                    "Status: " + e.status() + ", Message: " + e.contentUTF8());
        }
    }
}
