package com.clinicadmin.utils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;

import com.clinicadmin.dto.WardBoyDTO;
import com.clinicadmin.entity.WardBoy;

public class WardBoyMapper {

    /**
     * Encode raw String to Base64.
     */
    public static String encodeToBase64(String input) {
        if (input == null || input.isEmpty()) return null;
        return Base64.getEncoder().encodeToString(input.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Decode Base64 string to original String.
     */
    public static String decodeFromBase64(String base64) {
        if (base64 == null || base64.isEmpty()) return null;
        return new String(Base64.getDecoder().decode(base64), StandardCharsets.UTF_8);
    }

    // ----------------- DTO → Entity -----------------
    public static WardBoy toEntity(WardBoyDTO dto) {
        if (dto == null) return null;

        WardBoy wardBoy = new WardBoy();
        wardBoy.setWardBoyId(dto.getWardBoyId());
        wardBoy.setClinicId(dto.getClinicId());
        wardBoy.setHospitalName(dto.getHospitalName());
        wardBoy.setBranchId(dto.getBranchId());
        wardBoy.setBranchName(dto.getBranchName());
        wardBoy.setRole(dto.getRole());

        wardBoy.setFullName(dto.getFullName());
        wardBoy.setDateOfBirth(dto.getDateOfBirth());
        wardBoy.setContactNumber(dto.getContactNumber());
        wardBoy.setGovernmentId(dto.getGovernmentId());
        wardBoy.setDateOfJoining(dto.getDateOfJoining());
        wardBoy.setDepartment(dto.getDepartment());
        wardBoy.setBankAccountDetails(dto.getBankAccountDetails());
        wardBoy.setAddress(dto.getAddress());
        wardBoy.setGender(dto.getGender());
        wardBoy.setWorkExprience(dto.getWorkExprience());
        wardBoy.setShiftTimingsOrAvailability(dto.getShiftTimingsOrAvailability());
        wardBoy.setEmergencyContact(dto.getEmergencyContact());

        // Encode fields before saving to DB
        wardBoy.setProfilePicture(encodeToBase64(dto.getProfilePicture()));
        wardBoy.setPoliceVerificationCertificate(encodeToBase64(dto.getPoliceVerificationCertificate()));
        wardBoy.setMedicalFitnessCertificate(encodeToBase64(dto.getMedicalFitnessCertificate()));
        wardBoy.setBasicHealthFirstAidTrainingCertificate(
                encodeToBase64(dto.getBasicHealthFirstAidTrainingCertificate()));
        wardBoy.setPoliceVerification(encodeToBase64(dto.getPoliceVerification()));

        wardBoy.setEmailId(dto.getEmailId());
        wardBoy.setPreviousEmploymentHistory(dto.getPreviousEmploymentHistory());
        wardBoy.setPermissions(dto.getPermissions());
        wardBoy.setCreatedBy(dto.getCreatedBy());
        wardBoy.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Kolkata")).toString());

        return wardBoy;
    }

    // ----------------- Entity → DTO -----------------
    public static WardBoyDTO toDTO(WardBoy entity) {
        if (entity == null) return null;

        WardBoyDTO dto = new WardBoyDTO();
        dto.setWardBoyId(entity.getWardBoyId());
        dto.setFullName(entity.getFullName());
        dto.setGender(entity.getGender());
        dto.setClinicId(entity.getClinicId());
        dto.setHospitalName(entity.getHospitalName());
        dto.setBranchId(entity.getBranchId());
        dto.setBranchName(entity.getBranchName());
        dto.setRole(entity.getRole());
        dto.setDateOfBirth(entity.getDateOfBirth());
        dto.setContactNumber(entity.getContactNumber());
        dto.setGovernmentId(entity.getGovernmentId());
        dto.setDateOfJoining(entity.getDateOfJoining());
        dto.setDepartment(entity.getDepartment());
        dto.setAddress(entity.getAddress());
        dto.setEmergencyContact(entity.getEmergencyContact());
        dto.setShiftTimingsOrAvailability(entity.getShiftTimingsOrAvailability());
        dto.setWorkExprience(entity.getWorkExprience());
        dto.setBankAccountDetails(entity.getBankAccountDetails());

        // Decode Base64 fields for frontend/DTO usage
        dto.setProfilePicture(decodeFromBase64(entity.getProfilePicture()));
        dto.setPoliceVerificationCertificate(decodeFromBase64(entity.getPoliceVerificationCertificate()));
        dto.setMedicalFitnessCertificate(decodeFromBase64(entity.getMedicalFitnessCertificate()));
        dto.setBasicHealthFirstAidTrainingCertificate(
                decodeFromBase64(entity.getBasicHealthFirstAidTrainingCertificate()));
        dto.setPoliceVerification(decodeFromBase64(entity.getPoliceVerification()));

        dto.setEmailId(entity.getEmailId());
        dto.setPreviousEmploymentHistory(entity.getPreviousEmploymentHistory());
        dto.setPermissions(entity.getPermissions());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedDate(entity.getUpdatedDate());

        return dto;
    }
}
