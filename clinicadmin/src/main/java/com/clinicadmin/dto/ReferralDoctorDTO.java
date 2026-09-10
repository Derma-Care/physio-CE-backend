package com.clinicadmin.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReferralDoctorDTO {

	private String referredDoctorId;

	private String referredDoctorName;

	private Integer patientCount;

	private Double revenue;

	private List<ReferredPatientDTO> patients;

}
