package com.clinicadmin.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestionHeading {
	private String heading;
	private List<ConsentFormQuestionAndAnswers> questionsAndAnswers;
}
