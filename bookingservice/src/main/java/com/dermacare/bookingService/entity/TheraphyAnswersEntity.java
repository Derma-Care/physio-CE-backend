package com.dermacare.bookingService.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TheraphyAnswersEntity{
	
	private long questionId;
	private String question;
	private String answer;
	private List<String> options;

}
