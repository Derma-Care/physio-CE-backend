package com.clinicadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConsultationTypeDTO {
	private int inClinic; //1
	private int videoOrOnline;//2
	private int serviceAndTreatments;//3
	

}