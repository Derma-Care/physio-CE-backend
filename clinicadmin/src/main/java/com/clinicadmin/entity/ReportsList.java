package com.clinicadmin.entity;

import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "reportsList")
public class ReportsList {
	@Id
	private String id;
	private String customerId;
	private String patientId;
	private List<Reports> reportsList;

}