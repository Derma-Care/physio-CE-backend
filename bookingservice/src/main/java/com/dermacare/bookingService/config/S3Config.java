package com.dermacare.bookingService.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class S3Config {

	@Value("${aws.accessKeyId}")
	private String accessKey;

	@Value("${aws.secretAccessKey}")
	private String secretKey;

    @Value("${aws.region}")
    private String region;
    
 

    @Bean
    public S3Client s3Client() {

        // Create AWS credentials using access key and secret key
        AwsBasicCredentials credentials =
                AwsBasicCredentials.create(accessKey, secretKey);

        // Build and return S3 client
        // No additional changes are required here for content type handling.
        // Content-Type is set in S3Service during upload.
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(
                        StaticCredentialsProvider.create(credentials)
                )
                .build();
    }
    
    // =========================
    // S3 PRESIGNER
    // =========================
    @Bean
    public S3Presigner s3Presigner() {

        AwsBasicCredentials credentials =
                AwsBasicCredentials.create(accessKey, secretKey);

        return S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(
                        StaticCredentialsProvider.create(credentials)
                )
                .build();
    }
}