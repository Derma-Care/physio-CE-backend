package com.clinicadmin.entity;

import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.clinicadmin.dto.BankAccountDetails;
import com.clinicadmin.dto.DoctorBranches;
//import com.clinicadmin.dto.DoctorCategoryDTO;
//import com.clinicadmin.dto.DoctorServicesDTO;
//import com.clinicadmin.dto.DoctorSubServiceDTO;
import com.clinicadmin.utils.ObjectIdSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "doctors")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Doctors {
	@Id
	@JsonSerialize(using = ObjectIdSerializer.class)
	private ObjectId id;
	private String doctorId;
	private String role;
	private String deviceId;
	private String hospitalId;
	private String hospitalName;
	private String branchId;
	private String doctorEmail;
	private String doctorPicture;
	private String doctorLicence;
	private String doctorMobileNumber;
	private String doctorName;
//	private List<DoctorCategoryDTO> category;
//	private List<DoctorServicesDTO> service;
//	private List<DoctorSubServiceDTO> subServices;
	private String specialization;
	private String gender;
	private String experience;
	private String qualification;
	private String availableDays;
	private String availableTimes;
	private String profileDescription;
	private DoctorFee doctorFees;
	private List<String> focusAreas;
	private List<String> languages;
	private List<String> highlights;
	private Boolean doctorAvailabilityStatus;
	private double doctorAverageRating;
	private boolean recommendation;
	private String doctorSignature;
	private boolean associatedWithIADVC;
	private String associationsOrMemberships;
	private List<DoctorBranches> branches;
//	private ConsultationType Consultation;
	private Map<String, List<String>> permissions;
	private String dateofJoining;
	private String emergencyContact;
	private String aadharID;
	private String dateofBirth;
	private String createdBy;

	private String createdAt;

	private String updatedDate;

	private String Status;
	private BankAccountDetails bankAccountDetails;

	private String fcmTokenUpdatedAt;
	private String providerType;


}
