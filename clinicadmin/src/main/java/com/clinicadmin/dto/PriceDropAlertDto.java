package com.clinicadmin.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PriceDropAlertDto {

	private String clinicId;
	private String branchId;
	private String title;
	private String body;
	private String image;
	private boolean sendAll;
	private List<String> tokens;
	private String customerName;
	private String mobileNumber;

	
	public boolean getSendAll() {
		return sendAll;
	}
}
