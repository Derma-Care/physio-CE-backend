package com.clinicadmin.utils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;

import com.clinicadmin.dto.SecurityStaffDTO;
import com.clinicadmin.entity.SecurityStaff;

public class SecurityStaffMapper {

    // Always encode string to Base64
    public static String encode(String input) {
        if (input == null) return null;
        return Base64.getEncoder().encodeToString(input.getBytes(StandardCharsets.UTF_8));
    }

    // Always decode Base64 to string
    public static String decode(String base64) {
        if (base64 == null) return null;
        return new String(Base64.getDecoder().decode(base64), StandardCharsets.UTF_8);
    }

    // DTO → Entity (encode before saving)
    public static SecurityStaff toEntity(SecurityStaffDTO dto) {
        if (dto == null) return null;

        SecurityStaff staff = new SecurityStaff();

        staff.setSecurityStaffId(dto.getSecurityStaffId());
        staff.setClinicId(dto.getClinicId());
        staff.setHospitalName(dto.getHospitalName());
        staff.setBranchId(dto.getBranchId());
        staff.setRole(dto.getRole());
        staff.setBranchName(dto.getBranchName());
        staff.setPermissions(dto.getPermissions());
        staff.setFullName(dto.getFullName());
        staff.setDateOfBirth(dto.getDateOfBirth());
        staff.setGender(dto.getGender());
        staff.setContactNumber(dto.getContactNumber());
        staff.setGovermentId(dto.getGovermentId());
        staff.setDateOfJoining(dto.getDateOfJoining());
        staff.setDepartment(dto.getDepartment());
        staff.setAddress(dto.getAddress());
        staff.setBankAccountDetails(dto.getBankAccountDetails());
        staff.setShiftTimingsOrAvailability(dto.getShiftTimingsOrAvailability());

        // Always encode files/images
        staff.setPoliceVerification(encode(dto.getPoliceVerification()));
        staff.setPoliceVerificationCertificate(encode(dto.getPoliceVerificationCertificate()));
        staff.setMedicalFitnessCertificate(encode(dto.getMedicalFitnessCertificate()));
        staff.setProfilePicture(encode(dto.getProfilePicture()));

        staff.setEmailId(dto.getEmailId());
        staff.setTraningOrGuardLicense(dto.getTraningOrGuardLicense());
        staff.setPreviousEmployeeHistory(dto.getPreviousEmployeeHistory());
        staff.setCreatedBy(dto.getCreatedBy());
        staff.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Kolkata")).toString());

        return staff;
    }

    // Entity → DTO (decode before sending to frontend)
    public static SecurityStaffDTO toDTO(SecurityStaff staff) {
        if (staff == null) return null;

        SecurityStaffDTO dto = new SecurityStaffDTO();

        dto.setSecurityStaffId(staff.getSecurityStaffId());
        dto.setClinicId(staff.getClinicId());
        dto.setHospitalName(staff.getHospitalName());
        dto.setBranchId(staff.getBranchId());
        dto.setBranchName(staff.getBranchName());
        dto.setRole(staff.getRole());
        dto.setFullName(staff.getFullName());
        dto.setDateOfBirth(staff.getDateOfBirth());
        dto.setGender(staff.getGender());
        dto.setContactNumber(staff.getContactNumber());
        dto.setGovermentId(staff.getGovermentId());
        dto.setDateOfJoining(staff.getDateOfJoining());
        dto.setDepartment(staff.getDepartment());
        dto.setShiftTimingsOrAvailability(staff.getShiftTimingsOrAvailability());
        dto.setAddress(staff.getAddress());
        dto.setBankAccountDetails(staff.getBankAccountDetails());

        // Always decode files/images
        dto.setPoliceVerification(decode(staff.getPoliceVerification()));
        dto.setPoliceVerificationCertificate(decode(staff.getPoliceVerificationCertificate()));
        dto.setMedicalFitnessCertificate(decode(staff.getMedicalFitnessCertificate()));
        dto.setProfilePicture(decode(staff.getProfilePicture()));

        dto.setEmailId(staff.getEmailId());
        dto.setTraningOrGuardLicense(staff.getTraningOrGuardLicense());
        dto.setPreviousEmployeeHistory(staff.getPreviousEmployeeHistory());
        dto.setPermissions(staff.getPermissions());
        dto.setCreatedBy(staff.getCreatedBy());
        dto.setCreatedAt(staff.getCreatedAt());
        dto.setUpdatedDate(staff.getUpdatedDate());

        return dto;
    }
}
