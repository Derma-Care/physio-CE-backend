package com.clinicadmin.dto;

import java.util.List;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestDetailsDTO {
	 private List<String> selectedTests;
	    private String testReason;
}