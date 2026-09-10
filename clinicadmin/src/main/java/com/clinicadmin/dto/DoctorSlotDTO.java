package com.clinicadmin.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorSlotDTO {
    private String doctorId;
    private String hospitalId;
    private String branchId;
    private String date;             // yyyy-MM-dd
    private String branchName;

    private int slotInterval;        // ex: 10, 20, 30
    private String openingTime;      // ex: "07:00 AM"
    private String closingTime;      // ex: "08:00 PM"

    // Frontend will send selected slots from generated list
    private List<DoctorAvailableSlotDTO> availableSlots;

}
