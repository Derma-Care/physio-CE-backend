package com.dermacare.bookingService.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerOnbordingDTO {
    private String id;

    private String mobileNumber;

   
    private String email;

   
    private String fullName;

    
    private String gender;

    
    private String dateOfBirth;

    private String age;


    private Address address;

  
    private String hospitalId;

   
    private String hospitalName;

    
    private String branchId;

    private String customerId;
    private String patientId;
    private String deviceId;
    private String referralCode;
    private String referredBy;
    private String userName;
    private String password;
}
