package com.AdminService.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One hourly slot (e.g. 09 AM → 10 AM).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "clinic_timings")
public class ClinicTiming {

    @Id
    private String id;

    /** 0–23, unique so every start hour exists only once. */
    @Indexed(unique = true)
    private Integer startHour;

    private String openingTime;   // "09:00 AM"
    private String closingTime;  // "10:00 AM"
}