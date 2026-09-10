package com.clinicadmin.dto;

import lombok.Data;

/**
 * Incoming filter for GET /revenue-analytics.
 *
 * filterType mapping (as agreed with frontend):
 *   1 = Today
 *   2 = This Week
 *   3 = This Month
 *   4 = This Year
 *   5 = Overall (all-time, no date restriction)
 *
 * If startDate AND endDate are both supplied (format: yyyy-MM-dd), they take
 * priority over filterType and a custom range is used instead.
 */
@Data
public class RevenueFilterRequestDTO {

    private String clinicId;
    private String branchId;

    private Integer filterType; // 1,2,3,4,5 - see class javadoc

    private String startDate; // yyyy-MM-dd, optional, used for custom range
    private String endDate;   // yyyy-MM-dd, optional, used for custom range
}