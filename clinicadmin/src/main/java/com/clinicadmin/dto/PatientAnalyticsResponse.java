package com.clinicadmin.dto;

import java.util.List;

public class PatientAnalyticsResponse {
    private Summary summary;
    private List<TrendData> newPatientsTrend;
    private List<AgeGroupAnalytics> ageGroupAnalytics;

    public Summary getSummary() { return summary; }
    public void setSummary(Summary summary) { this.summary = summary; }
    public List<TrendData> getNewPatientsTrend() { return newPatientsTrend; }
    public void setNewPatientsTrend(List<TrendData> newPatientsTrend) { this.newPatientsTrend = newPatientsTrend; }
    public List<AgeGroupAnalytics> getAgeGroupAnalytics() { return ageGroupAnalytics; }
    public void setAgeGroupAnalytics(List<AgeGroupAnalytics> ageGroupAnalytics) { this.ageGroupAnalytics = ageGroupAnalytics; }
}