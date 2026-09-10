package com.clinicadmin.dto;

import lombok.Data;

@Data
public class DoctorReferralPatientDTO {

    private String patientName;
    private String bookingId;
    private String serviceName;
    private String serviceType;
    private String serviceTime;
    private String serviceDate;
    private String dateOfVisit;
    private String contactNumber;
    private String status;
    private Double totalCost;
    private Double paidAmount;
    private Double pendingAmount;

 
}