package com.clinicadmin.entity;


import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class Completion {

    private String status;

    private OffsetDateTime completedAt;

    private String completedBy;

    private Boolean isCompleted;
}
