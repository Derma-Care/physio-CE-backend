package com.AdminService.entity;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Document(collection = "ClinsQA")
public class StoreUpdatedQAInClinics {
	@Id
    private String id;
    private List<QuestionAnswer> questionsAndAnswers;
    private int count;
    private int score;
    private boolean submitedQA;
}

