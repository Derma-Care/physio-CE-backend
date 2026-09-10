package com.clinicadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeResponse {

    private String customerId;
    private String patientId;
    private String patientName;
    private String mobileNumber;
    private String doctorMobileNumber;
    private String clinicId;
    private String branchId;
    private String gender;
    private String age;
    private String address;
    private String bookingId;
    private Sittings sitting;
    private HomeVisitTrackingRequest homeVisitTrackingRequest;
    private int size;
}
