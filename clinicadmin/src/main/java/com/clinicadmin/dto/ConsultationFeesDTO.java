package com.clinicadmin.dto;

import java.time.LocalDateTime;
import org.springframework.data.annotation.CreatedDate;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConsultationFeesDTO {
	
	private double consulationFee;
	private String DATE_TIME;
	

}
