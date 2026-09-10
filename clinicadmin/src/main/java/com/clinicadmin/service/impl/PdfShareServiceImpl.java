package com.clinicadmin.service.impl;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.clinicadmin.dto.PdfShareResponseDTO;
import com.clinicadmin.entity.PdfShareLink;
import com.clinicadmin.repository.PdfShareLinkRepository;
import com.clinicadmin.service.PdfShareService;
import com.clinicadmin.service.S3Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PdfShareServiceImpl implements PdfShareService {

    private final PdfShareLinkRepository repository;
    private final S3Service s3Service;

    @Value("${app.base-url}")
    private String baseUrl;

    @Override
    public PdfShareResponseDTO generateShareUrl(String fileKey) {

        DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern("dd-MM-yyyy HH:mm:ss")
                .withZone(ZoneId.of("Asia/Kolkata"));

        Optional<PdfShareLink> existing = repository.findByFileKey(fileKey);

        if (existing.isPresent()
                && existing.get().getExpiryTime().isAfter(Instant.now())) {

            PdfShareLink link = existing.get();

            return PdfShareResponseDTO.builder()
                    .shortCode(link.getShortCode())
                    .shareUrl(baseUrl + "/pdf/" + link.getShortCode())
                    .fileKey(link.getFileKey())
                    .createdAt(formatter.format(link.getCreatedAt()))
                    .expiryTime(formatter.format(link.getExpiryTime()))
                    .build();
        }

        String shortCode = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 10);

        Instant now = Instant.now();

        PdfShareLink link = PdfShareLink.builder()
                .shortCode(shortCode)
                .fileKey(fileKey)
                .createdAt(now)
                .expiryTime(now.plus(3, ChronoUnit.DAYS))
                .build();

        repository.save(link);

        return PdfShareResponseDTO.builder()
                .shortCode(shortCode)
                .shareUrl(baseUrl + "/pdf/" + shortCode)
                .fileKey(fileKey)
                .createdAt(formatter.format(link.getCreatedAt()))
                .expiryTime(formatter.format(link.getExpiryTime()))
                .build();
    }

    @Override
    public byte[] getPdf(String shortCode) {

        PdfShareLink link = repository.findByShortCode(shortCode)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Invalid PDF share link."));

        if (Instant.now().isAfter(link.getExpiryTime())) {
            throw new ResponseStatusException(
                    HttpStatus.GONE,
                    "The PDF share link you are trying to access has expired. Please contact the sender to request a new share link.");
        }

        return s3Service.downloadFile(link.getFileKey());
        
    }
    
    
    @Override
    public String getFileKey(String shortCode) {

    	PdfShareLink link = repository.findByShortCode(shortCode)
    	        .orElseThrow(() -> new ResponseStatusException(
    	                HttpStatus.NOT_FOUND,
    	                "Invalid PDF share link."));
    	if (Instant.now().isAfter(link.getExpiryTime())) {
    	    throw new ResponseStatusException(
    	            HttpStatus.GONE,
    	            "The PDF share link you are trying to access has expired. Please contact the sender to request a new share link.");
    	}

        return link.getFileKey();
    }
}