package com.AdminService.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.AdminService.dto.StoreUpdatedQAInClinicsDTO;
import com.AdminService.service.StoreUpdatedQAInClinicsService;
import com.AdminService.util.Response;

@RestController
@RequestMapping("/admin")
//@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class StoreUpdatedQAInClinicsController {
	@Autowired
	StoreUpdatedQAInClinicsService storeUpdatedQAInClinicsService;
	
	@PostMapping("clinicQA/postQuestionsAndAnswer")
	public ResponseEntity<Response> saveQuestions(@RequestBody StoreUpdatedQAInClinicsDTO dto){
		Response response = storeUpdatedQAInClinicsService.saveQaAndAnswers(dto);
		return ResponseEntity.status(response.getStatus()).body(response);	
		
	}
	
	@PutMapping("clinicQA/updateQuestionsAndAnswer/{id}")
	public ResponseEntity<Response> updateQA(@PathVariable String id, @RequestBody StoreUpdatedQAInClinicsDTO dto){
		Response response = storeUpdatedQAInClinicsService.updateQaAndAnswers(id, dto);
		return ResponseEntity.status(response.getStatus()).body(response);	
		
	}
	
	 // ✅ Get Q&A by Id
    @GetMapping("clinicQA/getQuestionsAndAnswerById/{id}")
    public ResponseEntity<Response> getById(@PathVariable String id) {
        Response response = storeUpdatedQAInClinicsService.getById(id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    // ✅ Get All Q&A
    @GetMapping("getAllQuestionsAndAnswer")
    public ResponseEntity<Response> getAll() {
        Response response = storeUpdatedQAInClinicsService.getAll();
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    // ✅ Delete Q&A by Id
    @DeleteMapping("deleteQuestionsAndAnswer/{id}")
    public ResponseEntity<Response> deleteById(@PathVariable String id) {
        Response response = storeUpdatedQAInClinicsService.deleteById(id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

}
