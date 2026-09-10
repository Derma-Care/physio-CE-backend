package com.clinicadmin.dto;

import lombok.Data;

@Data
public class RecoverySupportDTO {
    private String id;
    private String clinicId;
    private String name;
    private String description;
    private String image;
    private String category;
}