package com.AdminService.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClinicTimingDTO {

    /** Required. 12‑hour format, e.g. "07:00 AM". */
    private String openingTime;

    /**
     * Optional:
     *   • blank / null → insert ONE slot (openingTime → openingTime+1h)
     *   • not blank     → treat as END of a range and bulk‑generate slots
     */
    private String closingTime;
}
