package com.clinicadmin.entity;

import java.time.Instant;
import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "home_visit_tracking")
@JsonIgnoreProperties(ignoreUnknown = true)
public class HomeVisitTracking {

    @Id
    private String id;

    private String sittingsId;

    private String assignmentStatus;

    private OffsetDateTime assignedAt;

    private JourneyToPatient journeyToPatient;

    private TreatmentEntity treatment;

    private JourneyToClinic journeyToClinic;

    private Completion completion;

    private Instant createdAt;

    private Instant updatedAt;
}
