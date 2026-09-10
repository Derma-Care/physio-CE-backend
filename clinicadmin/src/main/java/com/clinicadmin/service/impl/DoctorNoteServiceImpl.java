//package com.clinicadmin.service.impl;
//
//import java.util.List;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Service;
//
//import com.clinicadmin.dto.DoctorNotesDTO;
//import com.clinicadmin.dto.Response;
//import com.clinicadmin.feignclient.DoctorServiceFeign;
//import com.clinicadmin.service.DoctorNoteService;import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//@Service
//public class DoctorNoteServiceImpl implements DoctorNoteService {
//
//	@Autowired
//	DoctorServiceFeign doctorServiceFeign;
//
//	@Override
//	public Response getAllNotes() {
//
//		ResponseEntity<Response> notes = doctorServiceFeign.getAllNotes();
//		Object response=notes.getBody().getData();
//		List<DoctorNotesDTO> getNotes=new ObjectMapper().convertValue(response, new TypeReference<List<DoctorNotesDTO>>(){});
//		Response res = new Response();
//		res.setSuccess(notes.getBody().isSuccess());
//		res.setData(getNotes);
//		res.setStatus(notes.getBody().getStatus());
//		res.setMessage(notes.getBody().getMessage());
//		return res;
//	}
//
//}
