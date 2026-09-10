package com.clinicadmin.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.clinicadmin.dto.CustomerRatingDomain;
import com.clinicadmin.dto.RatingCategoryStats;
import com.clinicadmin.dto.RatingsDTO;
import com.clinicadmin.dto.Response;
import com.clinicadmin.feignclient.CustomerServiceFeignClient;
import com.clinicadmin.utils.ExtractFeignMessage;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import feign.FeignException;

@Service
public class RatingCalculationService {

    @Autowired
    private CustomerServiceFeignClient customerServiceFeignClient;

    public Response calculateAverageRating(String branchId, String doctorId) {
        Response response = new Response();

        try {
            ResponseEntity<Response> responseEntity =
                    customerServiceFeignClient.getRatingInfo(branchId, doctorId);

            Object ratings = responseEntity.getBody().getData();
            List<CustomerRatingDomain> allRatings = new ArrayList<>();
            ObjectMapper mapper = new ObjectMapper();

            // Convert to List
            if (ratings instanceof List) {
                allRatings = mapper.convertValue(ratings, new TypeReference<List<CustomerRatingDomain>>() {});
            } else if (ratings != null) {
                CustomerRatingDomain single = mapper.convertValue(ratings, CustomerRatingDomain.class);
                allRatings.add(single);
            }

            // Check if data exists at all
            if (allRatings.isEmpty()) {
                response.setSuccess(true);
                response.setStatus(200);
                response.setMessage("No ratings found");
                return response;
            }

            // Validate hospitalId and doctorId separately
            boolean hospitalExists = allRatings.stream()
                    .anyMatch(r -> r.getBranchId().equals(branchId));
            boolean doctorExists = allRatings.stream()
                    .anyMatch(r -> r.getDoctorId().equals(doctorId));

            if (!hospitalExists && !doctorExists) {
                response.setSuccess(false);
                response.setStatus(404);
                response.setMessage("Invalid hospitalId and doctorId.");
                return response;
            } else if (!hospitalExists) {
                response.setSuccess(false);
                response.setStatus(404);
                response.setMessage("Invalid hospitalId.");
                return response;
            } else if (!doctorExists) {
                response.setSuccess(false);
                response.setStatus(404);
                response.setMessage("Invalid doctorId.");
                return response;
            }

            List<CustomerRatingDomain> matchedRatings = allRatings.stream()
                    .filter(r -> r.getBranchId().equals(branchId) && r.getDoctorId().equals(doctorId))
                    .toList();

            if (matchedRatings.isEmpty()) {
                response.setSuccess(true);
                response.setStatus(200);
                response.setMessage("No matching ratings found for the given hospitalId and doctorId.");
                return response;
            }

            double totalDoctorRating = matchedRatings.stream()
                    .mapToDouble(CustomerRatingDomain::getDoctorRating).sum();
            double totalHospitalRating = matchedRatings.stream()
                    .mapToDouble(CustomerRatingDomain::getBranchRating).sum();
            double avgDoctorRating = totalDoctorRating / matchedRatings.size();
            double avgHospitalRating = totalHospitalRating / matchedRatings.size();

            long total = matchedRatings.size();

            // Doctor Rating Classification
            long excellent = matchedRatings.stream().filter(r -> r.getDoctorRating() >= 4.5).count();
            long good = matchedRatings.stream().filter(r -> r.getDoctorRating() >= 3.5 && r.getDoctorRating() < 4.5).count();
            long average = matchedRatings.stream().filter(r -> r.getDoctorRating() >= 2.5 && r.getDoctorRating() < 3.5).count();
            long belowAverage = matchedRatings.stream().filter(r -> r.getDoctorRating() < 2.5).count();

            List<RatingCategoryStats> categoryStats = new ArrayList<>();
            categoryStats.add(new RatingCategoryStats("Excellent", excellent, (excellent * 100.0) / total));
            categoryStats.add(new RatingCategoryStats("Good", good, (good * 100.0) / total));
            categoryStats.add(new RatingCategoryStats("Average", average, (average * 100.0) / total));
            categoryStats.add(new RatingCategoryStats("Below Average", belowAverage, (belowAverage * 100.0) / total));

            // Set data in DTO
            RatingsDTO data = new RatingsDTO();
            data.setDoctorId(doctorId);
            data.setBranchId(branchId);
            data.setOverallDoctorRating(avgDoctorRating);
            data.setOverallBranchRating(avgHospitalRating);
            data.setCount(total);
            data.setComments(matchedRatings);
            data.setRatingCategoryStats(categoryStats); // Include categorized stats

            response.setSuccess(true);
            response.setData(data);
            response.setMessage("Ratings fetched successfully.");
            response.setStatus(200);
            return response;

        } catch (FeignException e) {
            response.setSuccess(false);
            response.setMessage(ExtractFeignMessage.clearMessage(e));
            response.setStatus(e.status());
            return response;
		}
	}


    
    public Response calculateAverageRatingByDoctorId(String doctorId) {
        Response response = new Response();

        try {
            // Call Feign client to get ratings from customer service
            ResponseEntity<Response> responseEntity =
                    customerServiceFeignClient.getRatingInfoByDoctorId(doctorId);

            Object ratings = responseEntity.getBody().getData();
            List<CustomerRatingDomain> allRatings = new ArrayList<>();
            ObjectMapper mapper = new ObjectMapper();

            // Convert response into a List<CustomerRatingDomain>
            if (ratings instanceof List) {
                allRatings = mapper.convertValue(ratings, new TypeReference<List<CustomerRatingDomain>>() {});
            } else if (ratings != null) {
                CustomerRatingDomain single = mapper.convertValue(ratings, CustomerRatingDomain.class);
                allRatings.add(single);
            }

            // Check if there are ratings for this doctor
            List<CustomerRatingDomain> matchedRatings = allRatings.stream()
                    .filter(r -> r.getDoctorId().equals(doctorId))
                    .toList();

            if (matchedRatings.isEmpty()) {
                response.setSuccess(true);
                response.setStatus(200);
                response.setMessage("No Ratings Available.");
                return response;
            }

            // Compute averages
            double totalDoctorRating = matchedRatings.stream()
                    .mapToDouble(CustomerRatingDomain::getDoctorRating)
                    .sum();
            double totalBranchRating = matchedRatings.stream()
                    .mapToDouble(CustomerRatingDomain::getBranchRating)
                    .sum();

            double avgDoctorRating = totalDoctorRating / matchedRatings.size();
            double avgBranchRating = totalBranchRating / matchedRatings.size();

            long total = matchedRatings.size();

            // Doctor Rating Classification
            long excellent = matchedRatings.stream().filter(r -> r.getDoctorRating() >= 4.5).count();
            long good = matchedRatings.stream().filter(r -> r.getDoctorRating() >= 3.5 && r.getDoctorRating() < 4.5).count();
            long average = matchedRatings.stream().filter(r -> r.getDoctorRating() >= 2.5 && r.getDoctorRating() < 3.5).count();
            long belowAverage = matchedRatings.stream().filter(r -> r.getDoctorRating() < 2.5).count();

            List<RatingCategoryStats> categoryStats = new ArrayList<>();
            categoryStats.add(new RatingCategoryStats("Excellent", excellent, (excellent * 100.0) / total));
            categoryStats.add(new RatingCategoryStats("Good", good, (good * 100.0) / total));
            categoryStats.add(new RatingCategoryStats("Average", average, (average * 100.0) / total));
            categoryStats.add(new RatingCategoryStats("Below Average", belowAverage, (belowAverage * 100.0) / total));

            // Prepare DTO
            RatingsDTO data = new RatingsDTO();
            data.setDoctorId(doctorId);
            data.setOverallDoctorRating(avgDoctorRating);
            data.setOverallBranchRating(avgBranchRating);
            data.setCount(total);
            data.setComments(matchedRatings);
            data.setRatingCategoryStats(categoryStats);

            // Build response
            response.setSuccess(true);
            response.setData(data);
            response.setMessage("Ratings fetched successfully.");
            response.setStatus(200);
            return response;

        } catch (FeignException e) {
            response.setSuccess(false);
            response.setMessage(ExtractFeignMessage.clearMessage(e));
            response.setStatus(e.status());
            return response;
        }
    }



}
