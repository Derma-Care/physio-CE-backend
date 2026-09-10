package com.clinicadmin.entity;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "pdf_share_links")
public class PdfShareLink {

    @Id
    private String id;

    @Indexed(unique = true)
    private String shortCode;

    private String fileKey;

    private Instant createdAt;
    @Indexed(expireAfter = "0s")
    private Instant expiryTime;

}