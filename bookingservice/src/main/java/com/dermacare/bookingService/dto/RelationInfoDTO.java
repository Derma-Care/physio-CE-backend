package com.dermacare.bookingService.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RelationInfoDTO {
	
	private String patientId;
	private String customerId;
	private String relation;
	private String fullname;
	private String mobileNumber;
	private String age;
	private String address;
	private String gender;

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RelationInfoDTO)) return false;
        RelationInfoDTO that = (RelationInfoDTO) o;
        return fullname != null && fullname.equalsIgnoreCase(that.fullname)
                && relation != null && relation.equalsIgnoreCase(that.relation)
                && mobileNumber != null && mobileNumber.equalsIgnoreCase(that.mobileNumber)
                && patientId != null && patientId.equalsIgnoreCase(that.patientId) ;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(fullname.toLowerCase(), relation.toLowerCase(), mobileNumber.toLowerCase(),
        		patientId.toLowerCase());
    }
}
