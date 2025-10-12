package com.matheus.VehicleManager.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.io.IOException;
import java.net.URLConnection;
import java.time.Duration;
import java.util.UUID;

@Service
@Profile("prod")
public class S3StorageService implements FileStorageService {

    @Value("${aws.access-key}")
    private String accessKey;

    @Value("${aws.secret-key}")
    private String secretKey;

    @Value("${aws.region}")
    private String region;

    @Value("${aws.s3-bucket-name}")
    private String bucketName;

    private S3Client s3Client;
    private S3Presigner s3Presigner;

    private S3Client getS3Client() {
        if (s3Client == null) {
            AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
            s3Client = S3Client.builder()
                    .region(Region.of(region))
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .build();
        }
        return s3Client;
    }

    private S3Presigner getS3Presigner() {
        if (s3Presigner == null) {
            AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
            s3Presigner = S3Presigner.builder()
                    .region(Region.of(region))
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .build();
        }
        return s3Presigner;
    }

    public String generatePresignedUrl(String filename, String contentType) {
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(filename)
                .contentType(contentType)
                .build();

        PresignedPutObjectRequest presignedRequest = getS3Presigner()
                .presignPutObject(builder -> builder
                        .signatureDuration(Duration.ofMinutes(10))
                        .putObjectRequest(objectRequest)
                );

        return presignedRequest.url().toString();
    }

    public String storeFile(MultipartFile file) throws IOException {
        throw new UnsupportedOperationException("Direct upload not supported when using presigned URLs.");
    }

    public void deleteFile(String filePath) throws IOException {
        String key = filePath;
        if (filePath.startsWith("https://")) {
            key = filePath.substring(filePath.lastIndexOf("/") + 1);
        }

        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        getS3Client().deleteObject(deleteRequest);
    }
}
