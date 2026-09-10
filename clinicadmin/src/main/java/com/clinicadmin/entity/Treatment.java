package com.clinicadmin.entity;

import org.bson.types.ObjectId;
import lombok.Data;

@Data
public class Treatment {
    private ObjectId id;
    private String treatmentName;
    private String hospitalId;
}
