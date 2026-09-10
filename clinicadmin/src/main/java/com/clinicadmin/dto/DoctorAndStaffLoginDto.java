
package com.clinicadmin.dto;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DoctorAndStaffLoginDto {
	
	private String userName;
	private String password;
	private String role;
	private String deviceId;
	private String staffId;
	private String staffName;
	private String hospitalName;
	private String hospitalId;
	private String branchId;
	private String branchName;
	private Map<String, List<String>> permissions;


	public void setDoctorMobileNumber(String doctorMobileNumber) {
		this.userName = userName != null ? userName.trim() : null;
	}

	public void setPassword(String password) {
		this.password = password != null ? password.trim() : null;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId != null ? deviceId.trim() : null;
	}

	public void setStaffId(String staffId) {
		this.staffId = staffId != null ? staffId.trim() : null;
	}

	public void setStaffName(String staffName) {
		this.staffName = staffName != null ? staffName.trim() : null;
	}

	public void setHospitalId(String hospitalId) {
		this.hospitalId = hospitalId != null ? hospitalId.trim() : null;
	}
}
