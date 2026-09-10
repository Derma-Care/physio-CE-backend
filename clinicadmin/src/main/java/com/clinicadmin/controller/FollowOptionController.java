package com.clinicadmin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clinicadmin.dto.FollowOptionDTO;
import com.clinicadmin.dto.Response;
import com.clinicadmin.service.FollowOptionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/clinic-admin")
//@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})

public class FollowOptionController {
	@Autowired
	private FollowOptionService service;

	@PostMapping("/follow-options/create")
	public Response create(@RequestBody FollowOptionDTO dto) {
		return service.create(dto);
	}

	@GetMapping("/follow-options/getAll")
	public Response getAll() {
		return service.getAll();
	}

	@GetMapping("follow-options/getbyFollowUpId/{id}")
	public Response getById(@PathVariable String id) {
		return service.getById(id);
	}

	@PutMapping("follow-options/update/{id}")
	public Response update(@PathVariable String id, @RequestBody FollowOptionDTO dto) {
		return service.update(id, dto);
	}

	@DeleteMapping("follow-options/delete/{id}")
	public Response delete(@PathVariable String id) {
		return service.delete(id);
	}
}
