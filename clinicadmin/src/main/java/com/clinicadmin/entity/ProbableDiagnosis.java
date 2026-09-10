//package com.clinicadmin.entity;
//
//import org.bson.types.ObjectId;
//import org.springframework.data.annotation.Id;
//import org.springframework.data.mongodb.core.mapping.Document;
//
//import com.clinicadmin.utils.ObjectIdSerializer;
//import com.fasterxml.jackson.databind.annotation.JsonSerialize;
//
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//@Document(collection = "Diseases")
//@Data
//@AllArgsConstructor
//@NoArgsConstructor
//
//public class ProbableDiagnosis {
//	@Id
//	@JsonSerialize(using = ObjectIdSerializer.class)
//	private ObjectId id;
//	private String diseaseName;
//	private String hospitalId;
//	private String probableSymptoms;
//	private String notes;
//	
//	
//
//}
