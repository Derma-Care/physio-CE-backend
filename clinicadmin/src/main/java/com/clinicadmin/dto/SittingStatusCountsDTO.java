package com.clinicadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Breakdown of sitting statuses. Reused at three levels:
 *  - overall (across every package/sitting in the clinic+branch)
 *  - per package/sitting-type group
 *  - per patient within a group
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SittingStatusCountsDTO {

    private Integer totalSittings;
    private Integer completed;
    private Integer inProgress;
    private Integer notStarted;
    private Integer cancelled;
    private Integer rescheduled;

    // Sittings still owed to the patient: notStarted + inProgress + rescheduled
    // (i.e. everything except completed and cancelled).
    private Integer remainingSittings;
}