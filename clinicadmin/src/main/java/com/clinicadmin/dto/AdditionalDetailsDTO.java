package com.clinicadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public  class AdditionalDetailsDTO {

    private String billingStaff;
    private String notes;
    private String internalComments;
}