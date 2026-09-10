package com.clinicadmin.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.clinicadmin.entity.FollowOption;

public interface FollowOptionRepository extends MongoRepository<FollowOption, String> {
}
