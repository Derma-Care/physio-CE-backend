package com.clinicadmin.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "counters")
public class Counter {
    @Id
    private String id;  // e.g., "hospital1_customer", "hospital1_branch1_patient"
    private long seq;
}
