package com.clinicadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TheraphyAnswersDTO {
	
	private long questionId;
	private String question;
	private String answer;

}
