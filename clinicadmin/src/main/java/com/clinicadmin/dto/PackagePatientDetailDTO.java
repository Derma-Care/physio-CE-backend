package com.clinicadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One patient's row in a package/sitting-type's drill-down patient list.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PackagePatientDetailDTO {

    private String scheduleId;   // TreatmentSchedule.id
    private String patientId;
    private String patientName;
    private String mobileNumber;
    private String bookingId;
    private String doctorName;

    private SittingStatusCountsDTO statusCounts;

    private String lastSittingDate;   // most recent sitting date found on this schedule, "dd/MM/yyyy" or "yyyy-MM-dd" as stored
}
