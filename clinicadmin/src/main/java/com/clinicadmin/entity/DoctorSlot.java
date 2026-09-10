package com.clinicadmin.entity;

import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import com.clinicadmin.dto.DoctorAvailableSlotDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "doctor_slots")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorSlot {

    @Id
    private String id;

    private String doctorId;
    private String hospitalId;
    private String branchId;
    private String branchName;
    private String date; // yyyy-MM-dds
    private int slotInterval;        // ex: 10, 20, 30


    private List<DoctorAvailableSlotDTO> availableSlots;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Indexed(name = "slotExpiryIndex", expireAfter = "02d")
    private LocalDateTime createdAt = LocalDateTime.now();
}
