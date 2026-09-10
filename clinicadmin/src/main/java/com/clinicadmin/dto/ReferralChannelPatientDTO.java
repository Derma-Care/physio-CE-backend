package com.clinicadmin.dto;

import lombok.Data;

@Data
public class ReferralChannelPatientDTO {

    private String patientName;
    private String bookingId;

    private String serviceName;
    private String serviceType;

    private String serviceDate;
    private String serviceTime;
   
    private String contactNumber;

    private String status;
    private String dateOfVisit;
    private String referredByPerson;

    private Double totalCost;
    private Double paidAmount;
    private Double pendingAmount;
}