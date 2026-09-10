package com.clinicadmin.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorResponse {

    private String doctorId;
    private String clinicId;
    private String clinicName;
    private String branchId;
    private String branchName;
    private String doctorName;
    private String doctorMobileNumber;
    private String doctorProfile;
    private List<DoctorBranches> branches;
}
