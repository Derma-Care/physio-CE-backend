package com.clinicadmin.entity;

import java.time.LocalDateTime;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection="Vitals")
public class Vitals {
	@Id
	private ObjectId id;
	private String patientId;
	private String patientName;
	private String height;
	private double weight;
	private String bloodPressure;
	private String temperature;
	private String bmi;
	private String bookingId;
	private LocalDateTime  date;

}