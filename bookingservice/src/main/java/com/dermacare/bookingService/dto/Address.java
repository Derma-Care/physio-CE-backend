package com.dermacare.bookingService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Address {

	private String houseNo; // e.g. Flat No, House No.
	private String street; // e.g. Street Name / Road
	private String landmark; // e.g. Near temple, Opposite mall
	private String city; // e.g. Hyderabad
	private String state; // e.g. Telangana
	private String country; // e.g. India
	private String postalCode; // e.g. 500001
}