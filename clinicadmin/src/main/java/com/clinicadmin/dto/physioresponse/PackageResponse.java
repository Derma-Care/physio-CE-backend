package com.clinicadmin.dto.physioresponse;

import java.util.List;
import lombok.Data;

@Data
public class PackageResponse {
    private String packageId;
    private String packageName;
    private Double totalPackagePrice;
    private String paymentStatus;
    private List<ProgramResponse> programs;
}