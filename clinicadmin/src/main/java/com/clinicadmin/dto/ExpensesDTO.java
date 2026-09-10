package com.clinicadmin.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpensesDTO {
	
	private String id;
	private String clinicId;
	private String branchId;
	private String expense;
	private String category;
	private double amount;
	private LocalDate date;
	private String paymentMode;
	private String notes;
	private String role;
	private String staffId;
	private LocalDateTime timestamp;
	private LocalDateTime updatedAt;
	
}
