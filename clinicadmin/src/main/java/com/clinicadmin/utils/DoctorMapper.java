package com.clinicadmin.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;

import org.bson.types.ObjectId;

import com.clinicadmin.dto.BankAccountDetails;
import com.clinicadmin.dto.DoctorFeeDTO;
import com.clinicadmin.dto.DoctorsDTO;
import com.clinicadmin.entity.DoctorFee;
import com.clinicadmin.entity.Doctors;
import com.clinicadmin.service.S3Service;

public class DoctorMapper {

	public static Doctors mapDoctorDTOtoDoctorEntity(DoctorsDTO dto) {
		Doctors doctor = new Doctors();

		if (dto.getId() != null) {
			doctor.setId(new ObjectId(dto.getId()));
		}

		if (dto.getDoctorPicture() != null && !dto.getDoctorPicture().isBlank()) {
		    doctor.setDoctorPicture(dto.getDoctorPicture()); // S3 key stored as-is
		}

		doctor.setDoctorId(dto.getDoctorId());
		doctor.setHospitalId(dto.getHospitalId());
		doctor.setHospitalName(dto.getHospitalName());
		doctor.setBranchId(dto.getBranchId());
		doctor.setPermissions(dto.getPermissions());
		doctor.setProviderType(dto.getProviderType());
		doctor.setRole(dto.getRole());
		doctor.setDoctorAverageRating(dto.getDoctorAverageRating());
		doctor.setDoctorEmail(dto.getDoctorEmail());
		doctor.setDoctorLicence(dto.getDoctorLicence());
		doctor.setDoctorMobileNumber(dto.getDoctorMobileNumber());
		doctor.setDoctorName(dto.getDoctorName());
		doctor.setSpecialization(dto.getSpecialization());
		doctor.setGender(dto.getGender());
		doctor.setExperience(dto.getExperience());
		doctor.setQualification(dto.getQualification());
		doctor.setAvailableDays(dto.getAvailableDays());
		doctor.setAvailableTimes(dto.getAvailableTimes());
		doctor.setProfileDescription(dto.getProfileDescription());
		doctor.setFocusAreas(dto.getFocusAreas());
		doctor.setLanguages(dto.getLanguages());
		doctor.setHighlights(dto.getHighlights());
		doctor.setDoctorAvailabilityStatus(true);
		doctor.setRecommendation(dto.isRecommendation());
		doctor.setAadharID(dto.getAadharID());
		doctor.setDateofJoining(dto.getDateofJoining());
		doctor.setDateofBirth(dto.getDateofBirth());
		doctor.setEmergencyContact(dto.getEmergencyContact());
		doctor.setCreatedBy(dto.getCreatedBy());
		doctor.setCreatedBy(LocalDateTime.now(ZoneId.of("Asia/Kolkata")).toString());

		if (dto.getDoctorSignature() != null && !dto.getDoctorSignature().isBlank()) {
		    doctor.setDoctorSignature(dto.getDoctorSignature()); // S3 key stored as-is
		}

		doctor.setAssociatedWithIADVC(dto.isAssociatedWithIADVC());
		doctor.setAssociationsOrMemberships(dto.getAssociationsOrMemberships());
		doctor.setBranches(dto.getBranches());
		doctor.setPermissions(dto.getPermissions());

		if (dto.getDoctorFees() != null) {
			doctor.setDoctorFees(mapDoctorFeeDTOtoEntity(dto.getDoctorFees()));
		}
//		if (dto.getConsultation() != null) {
//			ConsultationType consultation = new ConsultationType();
//			consultation.setServiceAndTreatments(dto.getConsultation().getServiceAndTreatments());
//			consultation.setInClinic(dto.getConsultation().getInClinic());
//			consultation.setVideoOrOnline(dto.getConsultation().getVideoOrOnline());
//			doctor.setConsultation(consultation);
//		}
		if (dto.getBankAccountDetails() != null) {

		    BankAccountDetails bankDetails = new BankAccountDetails();

		    bankDetails.setAccountHolderName(
		            dto.getBankAccountDetails().getAccountHolderName());

		    bankDetails.setAccountNumber(
		            dto.getBankAccountDetails().getAccountNumber());

		    bankDetails.setBankName(
		            dto.getBankAccountDetails().getBankName());

		    bankDetails.setBranchName(
		            dto.getBankAccountDetails().getBranchName());

		    bankDetails.setIfscCode(
		            dto.getBankAccountDetails().getIfscCode());	
		    
		    bankDetails.setPanCardNumber(dto.getBankAccountDetails().getPanCardNumber());
		    
		    doctor.setBankAccountDetails(bankDetails);
		}
		return doctor;
	}

	public static DoctorsDTO mapDoctorEntityToDoctorDTO(Doctors doctor) {
		DoctorsDTO dto = new DoctorsDTO();

		if (doctor.getId() != null) {
			dto.setId(doctor.getId().toHexString());
		}

		dto.setDoctorId(doctor.getDoctorId());
		dto.setHospitalId(doctor.getHospitalId());
		dto.setBranchId(doctor.getBranchId());
		dto.setHospitalName(doctor.getHospitalName());
		dto.setPermissions(dto.getPermissions());
		dto.setRole(doctor.getRole());
		dto.setProviderType(doctor.getProviderType());
		if (doctor.getDoctorPicture() != null && !doctor.getDoctorPicture().isBlank()) {
		    dto.setDoctorPicture(doctor.getDoctorPicture()); // S3 key passed through as-is
		}

		dto.setDoctorLicence(doctor.getDoctorLicence());
		dto.setDoctorAverageRating(doctor.getDoctorAverageRating());
		dto.setDeviceId(doctor.getDeviceId());
		dto.setDoctorMobileNumber(doctor.getDoctorMobileNumber());
		dto.setDoctorName(doctor.getDoctorName());
		dto.setDoctorEmail(doctor.getDoctorEmail());
//		dto.setCategory(doctor.getCategory());
//		dto.setService(doctor.getService());
//		dto.setSubServices(doctor.getSubServices());
		dto.setSpecialization(doctor.getSpecialization());
		dto.setGender(doctor.getGender());
		dto.setExperience(doctor.getExperience());
		dto.setQualification(doctor.getQualification());
		dto.setAvailableDays(doctor.getAvailableDays());
		dto.setAvailableTimes(doctor.getAvailableTimes());
		dto.setProfileDescription(doctor.getProfileDescription());
		dto.setFocusAreas(doctor.getFocusAreas());
		dto.setLanguages(doctor.getLanguages());
		dto.setHighlights(doctor.getHighlights());
		dto.setDoctorAvailabilityStatus(doctor.getDoctorAvailabilityStatus());
		dto.setRecommendation(doctor.isRecommendation());
		dto.setAadharID(doctor.getAadharID());
		dto.setDateofBirth(doctor.getDateofBirth());
		dto.setDateofJoining(doctor.getDateofJoining());
		dto.setEmergencyContact(doctor.getEmergencyContact());


		if (doctor.getDoctorSignature() != null && !doctor.getDoctorSignature().isBlank()) {
		    dto.setDoctorSignature(doctor.getDoctorSignature()); // S3 key passed through as-is
		}

		dto.setAssociatedWithIADVC(doctor.isAssociatedWithIADVC());
		dto.setAssociationsOrMemberships(doctor.getAssociationsOrMemberships());
		dto.setBranches(doctor.getBranches());
		dto.setPermissions(doctor.getPermissions());
		dto.setCreatedAt(doctor.getCreatedAt());
		dto.setCreatedBy(doctor.getCreatedBy());
		dto.setUpdatedDate(doctor.getUpdatedDate());

		if (doctor.getDoctorFees() != null) {
			dto.setDoctorFees(mapDoctorFeeEntityToDTO(doctor.getDoctorFees()));
		}
//		if (doctor.getConsultation() != null) {
//			ConsultationTypeDTO consultationDTO = new ConsultationTypeDTO();
//			consultationDTO.setServiceAndTreatments(doctor.getConsultation().getServiceAndTreatments());
//			consultationDTO.setInClinic(doctor.getConsultation().getInClinic());
//			consultationDTO.setVideoOrOnline(doctor.getConsultation().getVideoOrOnline());
//			dto.setConsultation(consultationDTO);
//		}
		
		if (doctor.getBankAccountDetails() != null) {

		    BankAccountDetails bankDetails = new BankAccountDetails();

		    bankDetails.setAccountHolderName(
		            doctor.getBankAccountDetails().getAccountHolderName());

		    bankDetails.setAccountNumber(
		            doctor.getBankAccountDetails().getAccountNumber());

		    bankDetails.setBankName(
		            doctor.getBankAccountDetails().getBankName());

		    bankDetails.setBranchName(
		            doctor.getBankAccountDetails().getBranchName());

		    bankDetails.setIfscCode(
		            doctor.getBankAccountDetails().getIfscCode());
		    
		    bankDetails.setPanCardNumber(doctor.getBankAccountDetails().getPanCardNumber());
		           		    
		    dto.setBankAccountDetails(bankDetails);
		}

		return dto;
	}

	public static DoctorFee mapDoctorFeeDTOtoEntity(DoctorFeeDTO dto) {
		DoctorFee fee = new DoctorFee();
		fee.setInClinicFee(dto.getInClinicFee());
		fee.setVedioConsultationFee(dto.getVedioConsultationFee());
		return fee;
	}

	public static DoctorFeeDTO mapDoctorFeeEntityToDTO(DoctorFee fee) {
		DoctorFeeDTO dto = new DoctorFeeDTO();
		dto.setInClinicFee(fee.getInClinicFee());
		dto.setVedioConsultationFee(fee.getVedioConsultationFee());
		return dto;
	}
	
	// ── Overloaded: maps entity to DTO AND resolves S3 signed URLs ─
	public static DoctorsDTO mapDoctorEntityToDoctorDTO(Doctors doctor, S3Service s3Service) {
	    DoctorsDTO dto = mapDoctorEntityToDoctorDTO(doctor); // reuse existing mapper
	    if (dto.getDoctorPicture() != null && !dto.getDoctorPicture().isBlank())
	        dto.setDoctorPicture(s3Service.generateSignedUrl(dto.getDoctorPicture()));
	    if (dto.getDoctorSignature() != null && !dto.getDoctorSignature().isBlank())
	        dto.setDoctorSignature(s3Service.generateSignedUrl(dto.getDoctorSignature()));
	    return dto;
	}
}
