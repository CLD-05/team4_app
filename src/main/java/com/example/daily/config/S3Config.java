package com.example.daily.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {

    @Value("${cloud.aws.credentials.access-key:}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key:}")
    private String secretKey;

    @Value("${cloud.aws.region.static}")
    private String region;

    @Bean
    public S3Client s3Client() {
        AwsCredentialsProvider credentialsProvider;

        // 💡 핵심: 키가 비어있으면 DefaultCredentialsProvider를 사용한다!
        if (accessKey == null || accessKey.trim().isEmpty()) {
            // 이 녀석이 로컬 환경변수나 EKS의 IRSA 토큰을 알아서 감지하는 똑똑한 녀석입니다.
            credentialsProvider = DefaultCredentialsProvider.create();
        } else {
            // 프로퍼티스에 진짜 키가 적혀있다면 그걸 사용한다.
            credentialsProvider = StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey)
            );
        }

        return S3Client.builder()
            .region(Region.of(region))
            .credentialsProvider(credentialsProvider)
            .build();
    }
}