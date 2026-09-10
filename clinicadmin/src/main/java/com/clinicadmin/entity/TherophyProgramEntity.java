package com.clinicadmin.entity;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "therophyProgram")
@JsonInclude(JsonInclude.Include.NON_NULL)

public class TherophyProgramEntity {
	
	@Id
	private String id;
	private String programName;
	private List<String> therophyIds;
	private String clinicId;
	private String branchId;

}
