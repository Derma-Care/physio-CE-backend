package com.clinicadmin.entity;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.clinicadmin.utils.ObjectIdSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "LabTests")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LabTest {
    @Id
    @JsonSerialize(using = ObjectIdSerializer.class)
    private ObjectId id;
    private String testName;
    private String hospitalId;
    private String description;
    private String purpose;
   
}
