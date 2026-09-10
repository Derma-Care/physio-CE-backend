package com.clinicadmin.entity;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionsEntity {

	private long questionId;
	private String question;
	private String type;
	private List<String> options;

}
