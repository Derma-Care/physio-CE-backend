package com.clinicadmin.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import com.clinicadmin.entity.ImageForNotification;

@Repository
public interface ImageForNotificationRepo extends MongoRepository<ImageForNotification, String> {

	ImageForNotification findByImageName(String imageName);
}
