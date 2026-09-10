package com.clinicadmin.dto;



import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Analytics for a single package (or single-sitting service) - identified by
 * packageId. Covers both multi-sitting packages and single-sitting services,
 * since both are stored the same way on TreatmentSchedule (packageId /
 * packageName / packageType / numberOfSittings=1 for a single sitting).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PackageAnalyticsDTO {

    private String packageId;
    private String packageName;
    private String packageType;

    private Boolean singleSitting; // true when this group's plans are 1-sitting plans

    private Integer totalPatients;  // distinct patients who have taken this package/sitting
    private Integer totalBookings;  // total treatment-schedule records (a patient can rebook)

    private SittingStatusCountsDTO statusCounts; // aggregated across all patients in this group

    private List<PackagePatientDetailDTO> patients;
}
