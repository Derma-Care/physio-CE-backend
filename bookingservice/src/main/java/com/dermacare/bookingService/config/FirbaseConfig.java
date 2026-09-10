package com.dermacare.bookingService.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import com.google.cloud.storage.StorageOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;

@Configuration
public class FirbaseConfig {
	
	@Value("${app.firebase-configuration-file}")
	private String firebaseConfigPath;
	
	private Storage storage;
    private final String bucketName = "dermacare-17f59.appspot.com";

    @PostConstruct
    public void init() throws IOException {
        // Load Firebase service account JSON from resources
        ClassPathResource resource = new ClassPathResource(firebaseConfigPath);
        InputStream is = resource.getInputStream();

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(is))
                .setStorageBucket(bucketName)
                .build();

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options);
        }

        storage = StorageOptions.getDefaultInstance().getService();
      ///  System.out.println("Firebase initialized successfully!");
    }
	
    
    public String uploadBase64Image(String base64Image) {
        try {
            // Remove Base64 prefix if present
            if (base64Image.contains(",")) {
                base64Image = base64Image.split(",")[1];
            }

            byte[] decodedBytes = Base64.getDecoder().decode(base64Image);

            String uploadPath = "uploads/Dermacare.png";

            BlobId blobId = BlobId.of(bucketName, uploadPath);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType("image/png") // or "image/jpeg"
                    .build();

            storage.create(blobInfo, decodedBytes);

            return "https://storage.googleapis.com/" + bucketName + "/" + uploadPath;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("File upload failed", e);
        }}
	
}
