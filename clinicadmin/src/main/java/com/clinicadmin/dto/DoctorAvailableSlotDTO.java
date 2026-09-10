package com.clinicadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorAvailableSlotDTO {
    private String slot;          // Time like 10:30AM, 11:00AM
    private boolean slotbooked;   // true if booked
    private boolean available;    // true if selectable by frontend
    private String reason;        // optional reason if unavailable
}
