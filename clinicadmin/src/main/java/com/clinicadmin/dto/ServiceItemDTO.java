package com.clinicadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public  class ServiceItemDTO {

    private String serviceId;
    private String serviceName;

    private Integer qty;

    private Double unitPrice;
    private Double discountPercent;
    private Double taxPercent;
}