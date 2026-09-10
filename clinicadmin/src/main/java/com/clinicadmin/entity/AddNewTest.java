package com.clinicadmin.entity;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "add_new_test")
public class AddNewTest {

    @Id
    private String id;      // Example: TST-8F3A91

//    private String testName;

    private String vendor;

    private String vendorEmailId;

    private String clinicId;

    private String branchId;

}