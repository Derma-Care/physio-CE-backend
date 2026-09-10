package com.dermacare.bookingService.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BranchDTO {

    private String clinicId;
    private String hospitalName;
    private String branchId;
    private String branchName;
    private String address;
    private String contactNumber; // WhatsApp
    private String email;
    private String latitude;
    private String longitude;
    private String location;
}