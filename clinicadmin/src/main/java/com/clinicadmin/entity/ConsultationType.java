package com.clinicadmin.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConsultationType {
	
	private int serviceAndTreatments;
	private int inClinic;
	private int videoOrOnline;
	

}