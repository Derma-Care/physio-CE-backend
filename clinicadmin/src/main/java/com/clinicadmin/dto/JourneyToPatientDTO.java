package com.clinicadmin.dto;


import java.time.OffsetDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JourneyToPatientDTO {

    private String sittingsId;

    private String status;

    private OffsetDateTime startedAt;

    private Double startedLatitude;

    private Double startedLongitude;

    private Boolean isJourneyToPatientStarted;

    private Boolean isJourneyToPatientEnded;

    private OffsetDateTime reachedAt;

    private Double reachedLatitude;

    private Double reachedLongitude;

    private String startedAddressUrl;

    private String reachedAddressUrl;

    private String reachedAddress;

    private String  startedAddress;

    private Long durationMinutes;

    private Double distanceKm;
}