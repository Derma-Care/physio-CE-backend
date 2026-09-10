package com.clinicadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddNewTestDTO {

    private String id;

//    private String testName;

    private String vendor;

    private String vendorEmailId;

    private String clinicId;

    private String branchId;

}