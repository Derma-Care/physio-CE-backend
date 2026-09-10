package com.clinicadmin.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Document(collection = "Customer_Credentials")
public class CustomerCredentials {
	@Id
	private String Id;
	private String userName;
	private String password;
	private String hospitalId;
	private String branchId;
	private String hospitalName;
	

}
