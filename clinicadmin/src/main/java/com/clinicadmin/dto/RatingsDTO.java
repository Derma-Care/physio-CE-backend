package com.clinicadmin.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RatingsDTO {
	private String doctorId;;
	private String branchId;
	private double overallDoctorRating;
	private double overallBranchRating;
	private long count;
	private List<CustomerRatingDomain> comments;
	private List<RatingCategoryStats> ratingCategoryStats;

}
