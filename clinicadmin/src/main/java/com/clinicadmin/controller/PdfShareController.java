package com.clinicadmin.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.clinicadmin.dto.PdfShareResponseDTO;
import com.clinicadmin.service.PdfShareService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/pdf")
@RequiredArgsConstructor
public class PdfShareController {

    private final PdfShareService pdfShareService;

    // Generate share link
    @GetMapping("/generate")
    public ResponseEntity<PdfShareResponseDTO> generateShareLink(
            @RequestParam String fileKey) {

        return ResponseEntity.ok(pdfShareService.generateShareUrl(fileKey));
    }

    // Open PDF
    @GetMapping("/{shortCode}")
    public ResponseEntity<byte[]> openPdf(
            @PathVariable String shortCode) {

        byte[] pdfBytes = pdfShareService.getPdf(shortCode);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/pdf"))
                .contentLength(pdfBytes.length)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"document.pdf\"")
                .body(pdfBytes);
    }
}