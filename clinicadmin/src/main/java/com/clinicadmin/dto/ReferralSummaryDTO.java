package com.clinicadmin.dto;

import lombok.Data;

@Data
public class ReferralSummaryDTO {

    private Long totalReferrals;

    private Long doctorReferrals;

    private Double doctorReferralsPercentage;

    private Long otherChannelsReferrals;

    private Double otherChannelsReferralsPercentage;
    
    private long selfReferrals;
    
    private double selfReferralsPercentage;

    private TopReferringDoctorDTO topReferringDoctor;
}
