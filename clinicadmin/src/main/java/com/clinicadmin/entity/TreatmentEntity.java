package com.clinicadmin.entity;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class TreatmentEntity {

    private OffsetDateTime startedAt;

    private String status;

    private Double startedLatitude;

    private Double startedLongitude;

    private OffsetDateTime endedAt;

    private Double endedLatitude;

    private Double endedLongitude;

    private String startedAddressUrl;

    private String reachedAddressUrl;

    private String reachedAddress;

    private String  startedAddress;
    private Long durationMinutes;

    private Boolean isTreatmentStarted;

    private Boolean isTreatmentEnded;
}
