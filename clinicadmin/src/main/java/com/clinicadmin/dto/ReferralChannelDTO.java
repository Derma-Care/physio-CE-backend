package com.clinicadmin.dto;

import lombok.Data;

@Data
public class ReferralChannelDTO {

    private String channel;
//    private String referredByName;
    private Long patientsReferred;
    private Double revenueGenerated;
}
