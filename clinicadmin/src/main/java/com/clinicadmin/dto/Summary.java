package com.clinicadmin.dto;

public class Summary {
    private Integer totalPatients;
    private Integer newPatients;
    private Integer activePatients;
    private Double growthRate;

    public Integer getTotalPatients() { return totalPatients; }
    public void setTotalPatients(Integer totalPatients) { this.totalPatients = totalPatients; }
    public Integer getNewPatients() { return newPatients; }
    public void setNewPatients(Integer newPatients) { this.newPatients = newPatients; }
    public Integer getActivePatients() { return activePatients; }
    public void setActivePatients(Integer activePatients) { this.activePatients = activePatients; }
    public Double getGrowthRate() { return growthRate; }
    public void setGrowthRate(Double growthRate) { this.growthRate = growthRate; }
}
