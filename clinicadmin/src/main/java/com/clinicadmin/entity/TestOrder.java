package com.clinicadmin.entity;


import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "test_orders")
public class TestOrder {

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

    private String testName;
    private String testNameId;

    private String vendorId;
    private String vendorName;
    private String vendorEmail;

    private String status;

    private Instant orderedAt;
}