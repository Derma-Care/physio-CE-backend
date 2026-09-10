package com.clinicadmin.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PdfShareResponseDTO {

    private String shortCode;

    private String shareUrl;

    private String fileKey;

    private String createdAt;
    
    private String expiryTime;
}