package com.clinicadmin.entity;

import org.springframework.data.mongodb.core.mapping.Document;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Document(collection = "NotificationImage")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ImageForNotification {

	private String id;
	private String imageName;
	private byte[] image;
}
