package com.clinicadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BankAccountDetails {

	private String accountHolderName;
	private String accountNumber;
	private String bankName;
	private String branchName;
	private String ifscCode;
	private String panCardNumber;
}
