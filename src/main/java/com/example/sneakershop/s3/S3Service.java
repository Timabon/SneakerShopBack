package com.example.sneakershop.s3;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;

@Service
public class S3Service {
    private final S3Client s3Client;
    private final String bucketName;

    public S3Service(
            @Value("${aws.access-key}") String accessKey,
            @Value("${aws.secret-key}") String secretKey,
            @Value("${aws.region}") String region,
            @Value("${aws.s3.bucket-name}") String bucketName
    ) {
        this.bucketName = bucketName;
        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
    }


    public String uploadFile(String fileName, byte[] fileData) {
        String fileKey = "products/" + fileName;

        // Detect Content-Type (or set it manually if known)
        String contentType = detectContentType(fileName);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileKey)
                .contentType(contentType) // Set correct Content-Type
                .acl(ObjectCannedACL.PUBLIC_READ) // Make it public if needed
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(fileData));

        return "https://" + bucketName + ".s3.amazonaws.com/" + fileKey;
    }

    // Utility method to detect content type
    private String detectContentType(String fileName) {
        try {
            return Files.probeContentType(Paths.get(fileName));
        } catch (Exception e) {
            return "application/octet-stream"; // Fallback if detection fails
        }

    }
}
