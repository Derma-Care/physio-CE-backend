package com.AdminService.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class QuetionsAndAnswerForAddClinicDTO {
    private String id;
    private List<QuestionAnswerDTO> questionsAndAnswers;
}