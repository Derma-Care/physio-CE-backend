package com.clinicadmin.utils;

import com.clinicadmin.dto.ReferredDoctorDTO;
import com.clinicadmin.entity.ReferredDoctor;

public class ReferredDoctorMapper {

    public static ReferredDoctor dtoToEntity(ReferredDoctorDTO dto) {
        ReferredDoctor doctor = new ReferredDoctor();
        doctor.setId(dto.getId());
        doctor.setFullName(dto.getFullName());
        doctor.setClinicId(dto.getClinicId());
        doctor.setBranchId(dto.getBranchId());
        doctor.setReferralId(dto.getReferralId());
        doctor.setGender(dto.getGender());
        doctor.setDateOfBirth(dto.getDateOfBirth());
        doctor.setGovernmentId(dto.getGovernmentId());
        doctor.setMedicalRegistrationNumber(dto.getMedicalRegistrationNumber());
        doctor.setSpecialization(dto.getSpecialization());
        doctor.setYearsOfExperience(dto.getYearsOfExperience());
        doctor.setCurrentHospitalName(dto.getCurrentHospitalName());
        doctor.setDepartment(dto.getDepartment());
        doctor.setMobileNumber(dto.getMobileNumber());
        doctor.setEmail(dto.getEmail());
        doctor.setAddress(dto.getAddress());
//        doctor.setFirstReferralDate(dto.getFirstReferralDate());
//        doctor.setTotalReferrals(dto.getTotalReferrals());
//        doctor.setReferredPatients(dto.getReferredPatients());
//        doctor.setPreferredCommunicationMethod(dto.getPreferredCommunicationMethod());
        doctor.setBankAccountNumber(dto.getBankAccountNumber());
        doctor.setStatus(dto.getStatus());
        return doctor;
    }

    public static ReferredDoctorDTO entityToDto(ReferredDoctor doctor) {
        ReferredDoctorDTO dto = new ReferredDoctorDTO();
        dto.setId(doctor.getId());
        dto.setClinicId(doctor.getClinicId());
        dto.setBranchId(doctor.getBranchId());
        dto.setReferralId(doctor.getReferralId());
        dto.setFullName(doctor.getFullName());
        dto.setGender(doctor.getGender());
        dto.setDateOfBirth(doctor.getDateOfBirth());
        dto.setGovernmentId(doctor.getGovernmentId());
        dto.setMedicalRegistrationNumber(doctor.getMedicalRegistrationNumber());
        dto.setSpecialization(doctor.getSpecialization());
        dto.setYearsOfExperience(doctor.getYearsOfExperience());
        dto.setCurrentHospitalName(doctor.getCurrentHospitalName());
        dto.setDepartment(doctor.getDepartment());
        dto.setMobileNumber(doctor.getMobileNumber());
        dto.setEmail(doctor.getEmail());
        dto.setAddress(doctor.getAddress());
//        dto.setFirstReferralDate(doctor.getFirstReferralDate());
//        dto.setTotalReferrals(doctor.getTotalReferrals());
//        dto.setReferredPatients(doctor.getReferredPatients());
//        dto.setPreferredCommunicationMethod(doctor.getPreferredCommunicationMethod());
        dto.setBankAccountNumber(doctor.getBankAccountNumber());
        dto.setStatus(doctor.getStatus());
        return dto;
    }
}