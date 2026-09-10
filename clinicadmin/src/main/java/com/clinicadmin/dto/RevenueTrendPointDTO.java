package com.clinicadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A single bucket in the revenue trend chart (day, week, month, or year
 * depending on the granularity chosen for the active filter).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevenueTrendPointDTO {

    private String label;   // e.g. "2026-07-30", "2026-07", "Week 31" - display-ready bucket label
    private Double revenue; // amount collected (paid) in this bucket, from transaction history
    private Double discount; // discount applied on transactions in this bucket
}