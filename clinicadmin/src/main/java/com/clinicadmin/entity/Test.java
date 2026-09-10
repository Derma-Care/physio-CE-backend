package com.clinicadmin.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "tests")
public class Test {

    @Id
    private String id;

    private String testId;
    private String testName;
    private String clinicId;
    private String branchId;
}