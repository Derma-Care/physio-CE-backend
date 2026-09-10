package com.clinicadmin.entity;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Document(collection = "privacyPolicy")
@AllArgsConstructor
@NoArgsConstructor
public class PrivacyPolicy {

	private String id;
	private String clinicId;
	private String PrivacyPolicy;
}