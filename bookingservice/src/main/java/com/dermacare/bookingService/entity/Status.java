package com.dermacare.bookingService.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Status {
	
	private LocalDateTime DATE_TIME;
	private String status;

}
