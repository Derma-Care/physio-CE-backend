package com.clinicadmin.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DoctorSummary {

    private String doctorname;
    private String doctorId;
    private String date;
    private String time;           // e.g. "08:30 AM"
    private Integer sittingNumber; // numeric sitting number
    private String sittingStatus;  // "Not Started" | "In-progress" | "Completed"
    private String packageType;    // String package type
    private List<PhysioMaxTreatedDoctor> treatedBy;

}
