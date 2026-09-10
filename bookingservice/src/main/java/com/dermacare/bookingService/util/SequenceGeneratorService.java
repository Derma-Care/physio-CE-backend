package com.dermacare.bookingService.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dermacare.bookingService.entity.DatabaseSequence;

import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.core.FindAndModifyOptions;

@Service
public class SequenceGeneratorService {

    @Autowired
    private MongoOperations mongoOperations;

    public long getNextSequence(String key) {

        Query query = new Query(Criteria.where("_id").is(key));

        Update update = new Update().inc("seq", 1);

        FindAndModifyOptions options = new FindAndModifyOptions()
                .returnNew(true)
                .upsert(true);

        DatabaseSequence counter = mongoOperations.findAndModify(
                query, update, options, DatabaseSequence.class
        );

        return counter != null ? counter.getSeq() : 1;
    }
}