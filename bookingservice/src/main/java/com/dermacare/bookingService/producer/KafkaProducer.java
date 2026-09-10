//package com.dermacare.bookingService.producer;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.kafka.support.KafkaHeaders;
//import org.springframework.messaging.Message;
//import org.springframework.messaging.support.MessageBuilder;
//import org.springframework.stereotype.Service;
//
//import com.dermacare.bookingService.entity.Booking;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//@Service
//public class KafkaProducer {
//
//	@Autowired
//	private KafkaTemplate<String, String> kafkaTemplate;
//
//	@Autowired
//	private ObjectMapper mapper;
//
//	public void publishBooking(Booking booking) throws JsonProcessingException {
//
//		String jsonBooking = mapper.writeValueAsString(booking);
//		Message<String> bookings = MessageBuilder.withPayload(jsonBooking)
//				.setHeader(KafkaHeaders.TOPIC, "bookedServices").build();
//		kafkaTemplate.send(bookings);
//
//	}
//
//}
