package com.clinicadmin.config;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.beans.factory.annotation.Value;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import jakarta.annotation.PostConstruct;

@Configuration
public class FirebaseConfig {

    @Value("${app.firebase-configuration-file}")
    private String firebaseConfigurationFile;

    @PostConstruct
    public void initFirebase() throws IOException {

        if (FirebaseApp.getApps().isEmpty()) {

            InputStream serviceAccount =
                    new ClassPathResource(
                            firebaseConfigurationFile)
                            .getInputStream();

            FirebaseOptions options =
                    FirebaseOptions.builder()
                            .setCredentials(
                                    GoogleCredentials.fromStream(
                                            serviceAccount))
                            .build();

            FirebaseApp.initializeApp(options);
        }
    }
}