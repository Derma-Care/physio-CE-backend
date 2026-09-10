package com.clinicadmin.dto;


import lombok.Data;

@Data
public class PhysiotherapyRecordDTO {

    private String clinicId;
    private String branchId;
    private String patientId;
    private String bookingId;
    private String therapistRecordId;
    private String startDate;
}
