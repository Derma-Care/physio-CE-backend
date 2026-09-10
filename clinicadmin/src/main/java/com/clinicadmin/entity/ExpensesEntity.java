package com.clinicadmin.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import com.clinicadmin.dto.ExpensesDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "Expenses")
public class ExpensesEntity {
	
	@Id
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
