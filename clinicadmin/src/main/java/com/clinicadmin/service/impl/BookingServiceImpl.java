package com.clinicadmin.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.clinicadmin.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.clinicadmin.entity.QuestionsByPartEntity;
import com.clinicadmin.entity.QuestionsEntity;
import com.clinicadmin.feignclient.BookingFeign;
import com.clinicadmin.feignclient.CustomerServiceFeignClient;
import com.clinicadmin.service.BookingService;
import com.clinicadmin.service.DoctorService;
import com.clinicadmin.utils.ExtractFeignMessage;
import com.fasterxml.jackson.core.JsonProcessingException;

import feign.FeignException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Service
public class BookingServiceImpl implements BookingService {
	@Autowired
	BookingFeign bookingFeign;

	@Autowired
	DoctorService doctorService;	
	
	@Autowired	
	DoctorServiceImpl doctorServiceImpl;
	
	@Autowired
	private CustomerServiceFeignClient customerServiceFeignClient;
	
	// Add this field inside BookingServiceImpl class
	@Autowired
	private SimpMessagingTemplate messagingTemplate;



	@Override
	public Response getAllBookedServicesDetailsByBranchId(String branchId) {
		Response response = new Response();
		try {
			ResponseEntity<ResponseStructure<List<BookingResponse>>> res = bookingFeign
					.getAllBookedServicesByBranchId(branchId);

			if (res == null || !res.hasBody() || res.getBody().getData() == null || res.getBody().getData().isEmpty()) {
				response.setStatus(200);
				response.setMessage("Bookings Not Found");
				response.setSuccess(true);
				response.setData(Collections.emptyList());
				return response;
			}

			// ✅ Convert ResponseStructure to Response
			ResponseStructure<List<BookingResponse>> body = res.getBody();
			response.setStatus(body.getStatusCode());
			response.setMessage(body.getMessage());
			response.setSuccess(body.getHttpStatus().is2xxSuccessful());
			response.setData(body.getData());

			return response;

		} catch (FeignException e) {
			response.setStatus(e.status());
			response.setMessage(ExtractFeignMessage.clearMessage(e));
			response.setSuccess(false);
			response.setData(null);
			return response;
		}
	}

	@Override
	public ResponseEntity<ResponseStructure<List<Map<String,Object>>>> getBookingsByClinicIdWithBranchId(String clinicId,
			String branchId) {
		ResponseStructure<List<Map<String,Object>>> res = new ResponseStructure<>();
		try {
			return bookingFeign.getBookedServicesByClinicIdWithBranchId(clinicId, branchId);
		} catch (FeignException e) {
			res = new ResponseStructure<>(null, ExtractFeignMessage.clearMessage(e), HttpStatus.INTERNAL_SERVER_ERROR,
					e.status());
			return ResponseEntity.status(res.getStatusCode()).body(res);
		}
	}

	@Override
	public ResponseEntity<?> retrieveOneWeekAppointments(String clinicId, String branchId) {
		ResponseStructure<List<BookingResponse>> res = new ResponseStructure<>();
		try {
			return bookingFeign.retrieveOneWeekAppointments(clinicId, branchId);
		} catch (FeignException e) {
			res = new ResponseStructure<>(null, ExtractFeignMessage.clearMessage(e), null, e.status());
			return ResponseEntity.status(res.getStatusCode()).body(res);
		}
	}

	@Override
	public ResponseEntity<?> retrieveAppointnmentsByServiceDate(String clinicId, String branchId, String date) {
		ResponseStructure<List<BookingResponse>> res = new ResponseStructure<>();
		try {
			return bookingFeign.retrieveAppointnmentsByServiceDate(clinicId, branchId, date);
		} catch (FeignException e) {
			res = new ResponseStructure<>(null, ExtractFeignMessage.clearMessage(e), HttpStatus.INTERNAL_SERVER_ERROR,
					e.status());
			return ResponseEntity.status(res.getStatusCode()).body(res);
		}
	}

	@Override
	public ResponseEntity<?> updateAppointmentBasedOnBookingId(BookingResponse response) {
		ResponseStructure<List<BookingResponse>> res = new ResponseStructure<>();
		try {
			ResponseEntity<ResponseStructure<BookingResponse>> bookingResponse = bookingFeign.updateAppointmentBasedOnBookingId(response);
			if(bookingResponse.getBody().getData() != null) {
			if( response.getDoctorId() != null&&
						 response.getBranchId()!= null&&
						 response.getServiceDate()!= null&&
						 response.getServicetime()!= null) {
				 doctorServiceImpl.updateSlot(         
						 bookingResponse.getBody().getData().getDoctorId(),
						 bookingResponse.getBody().getData().getBranchId(),
						 bookingResponse.getBody().getData().getServiceDate(),
						 bookingResponse.getBody().getData().getServicetime());			
			  }} return bookingResponse;
			} catch (FeignException e) {
			res = new ResponseStructure<>(null, ExtractFeignMessage.clearMessage(e), HttpStatus.INTERNAL_SERVER_ERROR,
					e.status());
			return ResponseEntity.status(res.getStatusCode()).body(res);
		}
	}

	
	@Override
	public ResponseEntity<?> retrieveAppointnmentsByPatientId(String patientId) {
		ResponseStructure<List<BookingResponse>> res = new ResponseStructure<>();
		try {
			return bookingFeign.getBookingByPatientId(patientId);
		} catch (FeignException e) {
			res = new ResponseStructure<>(null, ExtractFeignMessage.clearMessage(e), HttpStatus.INTERNAL_SERVER_ERROR,
					e.status());
			return ResponseEntity.status(res.getStatusCode()).body(res);
		}

	}

		// BOOKING MANAGEMENT
		@Override
		public Response bookService(BookingResponse req) throws JsonProcessingException {
			Response response = new Response();
			try {
                ResponseEntity<Response> res = bookingFeign.bookService(req);
                Object bookingResponse = res.getBody();
				if (bookingResponse != null) {
					response.setData(bookingResponse);
					response.setMessage("follow up appointment found");
					response.setSuccess(true);
					response.setStatus(res.getBody().getStatus());
					try {
						messagingTemplate.convertAndSend(
								"/topic/bookings",
								response
						);
					} catch (Exception e) {}
				} else {				
					response.setMessage("follow up appointment not found");
					response.setSuccess(false);
					response.setStatus(res.getStatusCode().value());
				}
			} catch (FeignException e) {
				response.setStatus(e.status());
				response.setMessage( ExtractFeignMessage.clearMessage(e));
				response.setSuccess(false);
			}
			return response;
		}
	

@Override
public ResponseEntity<?> getInprogressBookingsByPatientId(String patientId) {
    ResponseStructure<List<BookingResponse>> res = new ResponseStructure<>();
    try {
        return bookingFeign.getInprogressAppointmentsByPatientId(patientId);
    } catch (FeignException e) {
        res = new ResponseStructure<>(null, ExtractFeignMessage.clearMessage(e), HttpStatus.INTERNAL_SERVER_ERROR, e.status());
        return ResponseEntity.status(res.getStatusCode()).body(res);
    }
}

@Override
public ResponseEntity<?> getInprogressBookingsByPatientIdAndClinicId(String patientId, String clinicId) {
    ResponseStructure<List<BookingResponse>> res = new ResponseStructure<>();
    try {
        return bookingFeign.getInprogressAppointmentsByPatientIdAndClinicId(patientId, clinicId);
    } catch (FeignException e) {
        res = new ResponseStructure<>(
                null,
                ExtractFeignMessage.clearMessage(e),
                HttpStatus.INTERNAL_SERVER_ERROR,
                e.status()
        );
        return ResponseEntity.status(res.getStatusCode()).body(res);
    }
}


@Override
public ResponseEntity<?> getReprts(String clinicId,
		String branchId,
		Integer number,
	    String startDate,
		String endDate) {
    ResponseStructure<List<BookingResponse>> res = new ResponseStructure<>();
    try {
        return bookingFeign.getReport(clinicId, branchId, number, startDate, endDate);
    } catch (FeignException e) {
        res = new ResponseStructure<>(
                null,
                ExtractFeignMessage.clearMessage(e),
                HttpStatus.INTERNAL_SERVER_ERROR,
                e.status()
        );
        return ResponseEntity.status(res.getStatusCode()).body(res);
    }
}



    @Override
    public ResponseEntity<?> getCustomBookings(String clinicId,
                                               String branchId,String date) {
        Response response = new Response();
        try {
            return bookingFeign.getCustomDateBookings(clinicId, branchId,date);
        } catch (FeignException e) {
            response.setStatus(e.status());
            response.setMessage( ExtractFeignMessage.clearMessage(e));
            response.setSuccess(false);
            return ResponseEntity.status(response.getStatus()).body(response);
        }
    }

@Override
public ResponseEntity<?> getInProgressBookingsByIds(String patientId,
		String bookingId) {
	Response response = new Response();
    try {
        return bookingFeign.getInProgressAppointmentByPatientIdAndBookingId(patientId, bookingId);
    } catch (FeignException e) {
    	response.setStatus(e.status());
		response.setMessage( ExtractFeignMessage.clearMessage(e));
		response.setSuccess(false);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}


@Override
public ResponseEntity<?> getReportsByPatientId(String patientId) {
	Response response = new Response();
    try {
        return bookingFeign.getReportsByPatientId(patientId);
    } catch (FeignException e) {
    	response.setStatus(e.status());
		response.setMessage(ExtractFeignMessage.clearMessage(e));
		response.setSuccess(false);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}

@Override
public ResponseEntity<?> getUpcomingBookings(String clinicId,
		String branchId,int option) {
	Response response = new Response();
    try {
        return bookingFeign.getUpcomingBookings(clinicId, branchId, option);
    } catch (FeignException e) {
    	response.setStatus(e.status());
		response.setMessage( ExtractFeignMessage.clearMessage(e));
		response.setSuccess(false);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}

@Override
public ResponseEntity<?> getBookingsByDate(String clinicId,
		String branchId, String date) {
	Response response = new Response();
    try {
        return bookingFeign.getPhysioBookingBasedOnDate(clinicId, branchId, date);
    } catch (FeignException e) {
    	response.setStatus(e.status());
		response.setMessage( ExtractFeignMessage.clearMessage(e));
		response.setSuccess(false);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}


@Override
public ResponseEntity<?> getBookingsByDateRange(String clinicId,
		String branchId,String start, String end) {
	Response response = new Response();
    try {
        return bookingFeign.getPhysioBookingsByCustomeRange(clinicId, branchId, start, end);
    } catch (FeignException e) {
    	response.setStatus(e.status());
		response.setMessage( ExtractFeignMessage.clearMessage(e));
		response.setSuccess(false);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}


@Override
public ResponseEntity<?> getBookedServiceById(String bookingId) {
	Response response = new Response();
    try {
        return bookingFeign.getBookedService(bookingId);
    } catch (FeignException e) {
    	response.setStatus(e.status());
		response.setMessage( ExtractFeignMessage.clearMessage(e));
		response.setSuccess(false);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}


@Override
public ResponseEntity<?> getBookingById(String bookingId){
	Response response = new Response();
    try {
        return bookingFeign.getBookedService(bookingId);
    } catch (FeignException e) {
    	response.setStatus(e.status());
		response.setMessage( ExtractFeignMessage.clearMessage(e));
		response.setSuccess(false);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}


@Override
public ResponseEntity<?> getTodayBookingsByClinicIdAndBranchId(String clinicId,String branchId){
	Response response = new Response();
    try {
        return bookingFeign.getTodayBookings(clinicId, branchId);
    } catch (FeignException e) {
    	response.setStatus(e.status());
		response.setMessage( ExtractFeignMessage.clearMessage(e));
		response.setSuccess(false);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}

@Override
public ResponseEntity<?> getFilteredBookingsByStatus(String clinicId,String branchId){
	Response response = new Response();
    try {
        return bookingFeign.getFilteredBookingsByStatus(clinicId, branchId);
    } catch (FeignException e) {
    	response.setStatus(e.status());
		response.setMessage( ExtractFeignMessage.clearMessage(e));
		response.setSuccess(false);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}

@Override
public ResponseEntity<?> physioAppointment(BookingRequset req) {
    ResponseEntity<Response> res = null;
    Response response = new Response();
    try {
    	 if(req.getTheraphyAnswers()!= null) {
 	        
	        	if (req.getTheraphyAnswers() != null && !req.getTheraphyAnswers().isEmpty()) {

	        	    Map<String, List<TheraphyAnswersDTO>> map = req.getTheraphyAnswers();

	        	    for (Map.Entry<String, List<TheraphyAnswersDTO>> entry : map.entrySet()) {

	        	        String key = entry.getKey(); // e.g., "back"
	        	        List<TheraphyAnswersDTO> answersList = entry.getValue();

	        	        // 🔍 Fetch DB data based on key
	        	        QuestionsByPartEntity entity = null;
	        	        try {
	        	        entity = customerServiceFeignClient.getByKey(key).getBody();
	        	        }catch(Exception e) {}
	        	        if (entity == null || entity.getQuestionsByPart() == null) {
	        	            continue;
	        	        }	        	       
	        	        List<QuestionsEntity> questionsList = entity.getQuestionsByPart().get(key);

	        	        if (questionsList == null || questionsList.isEmpty()  ) {
	        	            continue;
	        	        }

	        	        // 🔁 Match questionId and set question
	        	        for (TheraphyAnswersDTO dto : answersList) {

	        	            for (QuestionsEntity q : questionsList) {

	        	                if (q.getQuestionId() == dto.getQuestionId()) {
	        	                    dto.setQuestion(q.getQuestion());
	        	                    break; // stop once matched
	        	                }
	        	            }
	        	        }
	        	    }
	        	}
	        res = bookingFeign.bookPhysioAppointment(req);
	        }else {
    	    res = bookingFeign.bookPhysioAppointment(req);}
    	//System.out.println(res);
    			try {
    				//System.out.println("WebSocket notification triggered: /topic/clinic-admin/bookings");

        			messagingTemplate.convertAndSend(
        					  "/topic/clinic-admin/bookings",
        					res.getBody().getData()
        			);
        		} catch (Exception e) {}
    		
    	return res;
      } catch (FeignException e) {
    	    response.setStatus(e.status());
			response.setMessage(ExtractFeignMessage.clearMessage(e));
			response.setSuccess(false);
			//response.setData(Collections.emptyList());
        return ResponseEntity.status(response.getStatus()).body(response);}
}


@Override
public ResponseEntity<ResponseStructure<BookingResponse>> deleteBookedService(String id){
	return bookingFeign.deleteBookedService(id);
	
}


}
