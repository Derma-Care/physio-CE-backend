package com.AdminService.service;

import com.AdminService.dto.QuetionsAndAnswerForAddClinicDTO;
import com.AdminService.util.Response;

public interface QuetionsAndAnswerForAddClinicService {

	Response saveQuetions(QuetionsAndAnswerForAddClinicDTO dto);

	Response updateQuetions(QuetionsAndAnswerForAddClinicDTO dto);

	Response getQuetions();



}