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
public class JourneyToPatient {

    private String sittingsId;

    private String status;

    private OffsetDateTime startedAt;

    private Double startedLatitude;

    private Double startedLongitude;

    private String startedAddressUrl;

    private String reachedAddressUrl;

    private String reachedAddress;

    private String  startedAddress;

    private Boolean isJourneyToPatientStarted;

    private Boolean isJourneyToPatientEnded;

    private OffsetDateTime reachedAt;

    private Double reachedLatitude;

    private Double reachedLongitude;

    private Long durationMinutes;

    private Double distanceKm;
}
