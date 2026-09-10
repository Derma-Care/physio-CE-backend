package com.clinicadmin.dto;

import lombok.Data;

@Data
public class TherapistRecordRequest {

    private String clinicId;
    private String branchId;
    private String patientId;
    private String bookingId;
    private String therapistId;
    private String therapistRecordId;

}