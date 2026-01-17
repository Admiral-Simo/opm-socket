package com.network.opmsocket.backend.chat.service;

import io.awspring.cloud.s3.S3Resource;
import io.awspring.cloud.s3.S3Template;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
public class S3Service {

    private final S3Template s3Template;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;

    public S3Service(S3Template s3Template) {
        this.s3Template = s3Template;
    }

    /**
     * Uploads the file to S3 and returns the unique Key (filename)
     */
    public String uploadFile(MultipartFile file) throws IOException {
        // 1. Generate a unique key (e.g., "chat-images/uuid-originalFilename")
        // This prevents users from overwriting each other's files.
        String key = "chat-uploads/" +  file.getOriginalFilename() + "-" + UUID.randomUUID() ;

        // 2. Upload the file stream to S3
        try (InputStream inputStream = file.getInputStream()) {
            S3Resource resource = s3Template.upload(bucketName, key, inputStream);

            return resource.getURL().toString();
        }
    }
}