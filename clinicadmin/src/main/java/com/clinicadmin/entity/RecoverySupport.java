package com.clinicadmin.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "recovery_support")
public class RecoverySupport {

    @Id
    private String id;

    private String clinicId;
    private String name;
    private String description;
    private String image;
    private String category;
}
