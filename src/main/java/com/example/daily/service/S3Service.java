package com.example.daily.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
public class S3Service {

    private final S3Client s3Client;
    private final String bucket;
    private final String region;
    private final String cloudfrontDomain;

    public S3Service(S3Client s3Client,
                     @Value("${cloud.aws.s3.bucket}") String bucket,
                     @Value("${cloud.aws.region.static}") String region,
                     @Value("${cloud.aws.cloudfront.domain}") String cloudfrontDomain) {
        this.s3Client = s3Client;
        this.bucket = bucket;
        this.region = region;
        this.cloudfrontDomain = cloudfrontDomain;
    }

    public String upload(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) return null;

        String originalFilename = file.getOriginalFilename();
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String filename = UUID.randomUUID().toString() + ext;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(filename)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(putObjectRequest,
                RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        return String.format("https://%s/%s", cloudfrontDomain, filename);
    }

    // ✅ S3 이미지 삭제
    public void delete(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) return;
        try {
            String filename = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(filename)
                    .build());
        } catch (Exception e) {
            System.err.println("S3 이미지 삭제 실패: " + e.getMessage());
        }
    }
}