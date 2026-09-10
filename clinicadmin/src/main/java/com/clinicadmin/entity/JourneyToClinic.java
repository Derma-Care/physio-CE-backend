package com.clinicadmin.entity;

import java.time.OffsetDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JourneyToClinic {

    private OffsetDateTime startedAt;

    private String status;

    private Double startedLatitude;

    private Double startedLongitude;

    private OffsetDateTime reachedClinicAt;

    private Double reachedClinicLatitude;

    private Double reachedClinicLongitude;

    private String startedAddressUrl;

    private String reachedAddressUrl;

    private String reachedAddress;

    private String  startedAddress;

    private Long durationMinutes;

    private Double distanceKm;

    private Boolean isJourneyToClinicStarted;

    private Boolean isJourneyToClinicEnded;
}