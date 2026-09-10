package com.clinicadmin.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClinicAdminDeviceTokenEntity {
	
	private String id;
	private String username;
	private String clinicAdminWebFcmToken;

}
