package com.clinicadmin.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.clinicadmin.entity.PdfShareLink;

@Repository
public interface PdfShareLinkRepository extends MongoRepository<PdfShareLink, String> {

    Optional<PdfShareLink> findByShortCode(String shortCode);

    Optional<PdfShareLink> findByFileKey(String fileKey);

}