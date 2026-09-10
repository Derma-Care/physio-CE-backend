package com.clinicadmin.dto;

import java.time.LocalDate;

public class PatientAnalyticsRequest {

    private String filter;      // "day" | "week" | "month" | "year" | "custom" — informational,
                                 // actual routing uses the {filterType} path variable (1-4);
                                 // "custom" is implied when startDate/endDate are present
    private String search;      // optional, filters age group rows e.g. "19-35"
    private LocalDate startDate; // required only when filter = "custom"
    private LocalDate endDate;   // required only when filter = "custom"

    public String getFilter() { return filter; }
    public void setFilter(String filter) { this.filter = filter; }

    public String getSearch() { return search; }
    public void setSearch(String search) { this.search = search; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
}
