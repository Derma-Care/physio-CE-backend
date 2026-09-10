//package com.dermacare.bookingService.service.Impl;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.mongodb.core.FindAndModifyOptions;
//import org.springframework.data.mongodb.core.MongoOperations;
//import org.springframework.data.mongodb.core.query.Criteria;
//import org.springframework.data.mongodb.core.query.Query;
//import org.springframework.data.mongodb.core.query.Update;
//import org.springframework.stereotype.Service;
//
//import com.dermacare.bookingService.entity.CustomizedDatabaseSequence;
//
//@Service
//public class SequenceGeneratorService {
//
//    @Autowired
//    private MongoOperations mongoOperations;
//
//    public long getNextSequence() {
//        Query query = new Query(Criteria.where("_id").is("booking_Id"));
//        Update update = new Update().inc("seq", 1);
//        FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true).upsert(true);
//        CustomizedDatabaseSequence counter = mongoOperations.findAndModify(query, update, options, CustomizedDatabaseSequence.class);
//        return counter != null ? counter.getSeq() : 1;
//    }
//}

