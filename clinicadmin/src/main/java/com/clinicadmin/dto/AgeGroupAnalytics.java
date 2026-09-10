package com.clinicadmin.dto;

public class AgeGroupAnalytics {
    private Long id;
    private String ageGroup;
    private Integer male;
    private Integer female;
    private Integer total;
    private Integer growthTrend;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getAgeGroup() { return ageGroup; }
    public void setAgeGroup(String ageGroup) { this.ageGroup = ageGroup; }
    public Integer getMale() { return male; }
    public void setMale(Integer male) { this.male = male; }
    public Integer getFemale() { return female; }
    public void setFemale(Integer female) { this.female = female; }
    public Integer getTotal() { return total; }
    public void setTotal(Integer total) { this.total = total; }
    public Integer getGrowthTrend() { return growthTrend; }
    public void setGrowthTrend(Integer growthTrend) { this.growthTrend = growthTrend; }
}
