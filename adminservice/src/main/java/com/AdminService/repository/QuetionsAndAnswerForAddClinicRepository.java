package com.AdminService.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.AdminService.entity.QuetionsAndAnswerForAddClinic;

@Repository
public interface QuetionsAndAnswerForAddClinicRepository extends MongoRepository<QuetionsAndAnswerForAddClinic, String> {

}