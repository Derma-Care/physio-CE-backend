package com.clinicadmin.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;

import com.clinicadmin.dto.LabTechnicianRequestDTO;
import com.clinicadmin.entity.LabTechnicianEntity;

public class LabTechnicianMapper {

    /**
     * Encode raw bytes to Base64 string.
     */
    public static String encodeBytesToBase64(byte[] bytes) {
        if (bytes == null || bytes.length == 0)
            return null;
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * Decode Base64 string to raw bytes.
     */
    public static byte[] decodeBase64ToBytes(String base64) {
        if (base64 == null || base64.isBlank())
            return null;
        return Base64.getDecoder().decode(base64);
    }

    /**
     * DTO → Entity
     */
    public static LabTechnicianEntity toEntity(LabTechnicianRequestDTO dto) {
        if (dto == null) return null;

        LabTechnicianEntity entity = new LabTechnicianEntity();

        entity.setId(dto.getId());
        entity.setClinicId(dto.getClinicId());
        entity.setHospitalName(dto.getHospitalName());
        entity.setBranchId(dto.getBranchId());
        entity.setFullName(dto.getFullName());
        entity.setGender(dto.getGender());
        entity.setDateOfBirth(dto.getDateOfBirth());
        entity.setContactNumber(dto.getContactNumber());
        entity.setGovernmentId(dto.getGovernmentId());
        entity.setQualificationOrCertifications(dto.getQualificationOrCertifications());
        entity.setDateOfJoining(dto.getDateOfJoining());
        entity.setDepartmentOrAssignedLab(dto.getDepartmentOrAssignedLab());
        entity.setYearOfExperience(dto.getYearOfExperience());
        entity.setSpecialization(dto.getSpecialization());
        entity.setShiftTimingsOrAvailability(dto.getShiftTimingsOrAvailability());
        entity.setAddress(dto.getAddress());
        entity.setEmergencyContact(dto.getEmergencyContact());
        entity.setBankAccountDetails(dto.getBankAccountDetails());

        // Store Base64 strings directly
        entity.setMedicalFitnessCertificate(dto.getMedicalFitnessCertificate());
        entity.setLabLicenseOrRegistration(dto.getLabLicenseOrRegistration());
        entity.setProfilePicture(dto.getProfilePicture());

        entity.setEmailId(dto.getEmailId());
        entity.setRole(dto.getRole());
        entity.setVaccinationStatus(dto.getVaccinationStatus());
        entity.setPreviousEmploymentHistory(dto.getPreviousEmploymentHistory());
        entity.setPermissions(dto.getPermissions());
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Kolkata")).toString());

        return entity;
    }

    /**
     * Entity → DTO
     */
    public static LabTechnicianRequestDTO toDTO(LabTechnicianEntity entity) {
        if (entity == null) return null;

        LabTechnicianRequestDTO dto = new LabTechnicianRequestDTO();

        dto.setId(entity.getId());
        dto.setClinicId(entity.getClinicId());
        dto.setHospitalName(entity.getHospitalName());
        dto.setBranchId(entity.getBranchId());
        dto.setFullName(entity.getFullName());
        dto.setGender(entity.getGender());
        dto.setDateOfBirth(entity.getDateOfBirth());
        dto.setContactNumber(entity.getContactNumber());
        dto.setGovernmentId(entity.getGovernmentId());
        dto.setQualificationOrCertifications(entity.getQualificationOrCertifications());
        dto.setDateOfJoining(entity.getDateOfJoining());
        dto.setDepartmentOrAssignedLab(entity.getDepartmentOrAssignedLab());
        dto.setYearOfExperience(entity.getYearOfExperience());
        dto.setSpecialization(entity.getSpecialization());
        dto.setShiftTimingsOrAvailability(entity.getShiftTimingsOrAvailability());
        dto.setAddress(entity.getAddress());
        dto.setEmergencyContact(entity.getEmergencyContact());
        dto.setBankAccountDetails(entity.getBankAccountDetails());

        // Return Base64 strings directly
        dto.setMedicalFitnessCertificate(entity.getMedicalFitnessCertificate());
        dto.setLabLicenseOrRegistration(entity.getLabLicenseOrRegistration());
        dto.setProfilePicture(entity.getProfilePicture());

        dto.setEmailId(entity.getEmailId());
        dto.setRole(entity.getRole());
        dto.setVaccinationStatus(entity.getVaccinationStatus());
        dto.setPreviousEmploymentHistory(entity.getPreviousEmploymentHistory());
        dto.setPermissions(entity.getPermissions());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedDate(entity.getUpdatedDate());

        return dto;
    }
}
