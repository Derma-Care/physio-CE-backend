package com.clinicadmin.entity;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "soap_notes")
public class SoapNote {

	@Id
	private String id;

	private String patientId;
	private String patientName;
	private String mobileNumber;
	private String clinicId;
	private String branchId;
	private String bookingId;
	private String doctorId;
	private String doctorName;
	private String complaints;
	private String packageType;
	private String packageId;
	private String packageName;
	private Integer numberOfSittings;
	private Double packagePrice;
	private String slot;
	private String noteText;
	private String sessionStartDate;

	// S3 keys only — never store presigned URLs in DB, they expire
	private String attachmentFileKey;
	private String audioNoteKey;

	private Instant createdAt;
	private Instant updatedAt;
	// NEW: true once editing should be locked — payment made or a real
	// sitting (2+) booked on the linked treatment schedule. Set by
	// PaymentServiceImpl whenever it recomputes the payment-side flag.
	// updateSoapNote() checks this and rejects edits once it's true.
	private boolean editingDisabled ;
}