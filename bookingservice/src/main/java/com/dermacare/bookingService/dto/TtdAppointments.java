package com.dermacare.bookingService.dto;

import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TtdAppointments {
	
	private List<List<BookingResponse>> appointments;

}
