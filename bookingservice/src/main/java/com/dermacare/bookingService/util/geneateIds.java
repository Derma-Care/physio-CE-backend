package com.dermacare.bookingService.util;

import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class geneateIds{
	
@Autowired
private SequenceGeneratorService sequenceGenerator;

public String generateBookingId(String clinicId, String branchId) {

    String year = String.valueOf(LocalDate.now().getYear());

    // Key for sequence (per clinic + branch + year)
    String sequenceKey = clinicId + "-" + branchId + "-" + year;

    long sequence = sequenceGenerator.getNextSequence(sequenceKey);

    // Format: 0001, 0002...
    String formattedSeq = String.format("%04d", sequence);

    return clinicId + "-" + branchId + "-" + year + "-" + formattedSeq;
}
}