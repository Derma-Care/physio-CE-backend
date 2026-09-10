package com.AdminService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestionAnswerDTO {
    private String question;
    private boolean answer; // Only Yes or No
}