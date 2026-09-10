package com.dermacare.bookingService.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;

@Document(collection = "counters")
@Data
public class DatabaseSequence {

    @Id
    private String id;   // e.g., APT-HYD-2026
    private long seq;

    // getters & setters
}