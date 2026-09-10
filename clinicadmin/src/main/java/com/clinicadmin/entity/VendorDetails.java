package com.clinicadmin.entity;

import lombok.Data;

@Data
public class VendorDetails {

    private String vendorName;
    private String contactPerson;
    private String phone;
    private String email;
    private String address;
    private String supportContractDetails;
}