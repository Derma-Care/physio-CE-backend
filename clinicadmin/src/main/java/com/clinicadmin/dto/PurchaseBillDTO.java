package com.clinicadmin.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PurchaseBillDTO {

	private String id;
	private String date;
	private String time;
	private String purchaseBillNo;

	@NotBlank(message = "Invoice No cannot be empty")
	private String invoiceNo;

	@NotBlank(message = "Supplier name required")
	private String supplierName;

	@NotBlank(message = "Invoice date required")
	private String invoiceDate;

	@NotBlank(message = "Receiving date required")
	private String receivingDate;

	private String taxType; // IGST / CGST-SGST

	@NotNull(message = "Items cannot be null")
	private List<PurchaseItemDTO> medicineDetails;

	private Double paidAmount; // change made
	private Double previousAdjustment; // change made
	private Double postDiscount; // change made

	private String paymentMode;
	private String billDueDate;
	private String creditDays;
	private String duePaidBillNo;
	private String department;
	private String financialYear;
	
}
