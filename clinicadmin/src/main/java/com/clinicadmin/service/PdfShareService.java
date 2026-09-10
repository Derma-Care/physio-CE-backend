package com.clinicadmin.service;

import com.clinicadmin.dto.PdfShareResponseDTO;

public interface PdfShareService {

    PdfShareResponseDTO generateShareUrl(String fileKey);

    byte[] getPdf(String shortCode);

    String getFileKey(String shortCode);
}