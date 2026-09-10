package com.clinicadmin.entity;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "package-Management")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class PackageManagement {

    @Id
    
    private String id;
    
    private String packageId;
    
    private String clinicId;
    
    private String branchId;
    private String discountAmount;
    private String packageAmount;
    private String packageName;
    private String finalAmount;
    private List<String> programIds;

    private int discountPercentage;

    private String startOfferDate;

    private String endOfferDate;

    private String offerType;
    
    private List<TherophyProgramEntity>programs;
}

