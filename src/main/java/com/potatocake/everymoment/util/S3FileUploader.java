package com.potatocake.everymoment.util;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.potatocake.everymoment.config.AwsS3Properties;
import com.potatocake.everymoment.exception.ErrorCode;
import com.potatocake.everymoment.exception.GlobalException;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@EnableConfigurationProperties(AwsS3Properties.class)
@Component
public class S3FileUploader {

    private AmazonS3 amazonS3;
    private final AwsS3Properties properties;

    @PostConstruct
    private void s3Client() {
        AWSCredentials awsCredentials = new BasicAWSCredentials(properties.accessKey(), properties.secretKey());

        amazonS3 = AmazonS3ClientBuilder.standard()
                .withRegion(properties.region())
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .build();
    }

    public String uploadFile(MultipartFile file) {
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();

        try {
            validateFileType(file);

            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentType(file.getContentType());
            objectMetadata.setContentLength(file.getSize());

            amazonS3.putObject(
                    new PutObjectRequest(properties.bucket(), filename, file.getInputStream(), objectMetadata)
                            .withCannedAcl(CannedAccessControlList.PublicRead));
        } catch (IOException e) {
            throw new GlobalException(ErrorCode.FILE_STORE_FAILED);
        }

        return amazonS3.getUrl(properties.bucket(), filename).toString();
    }

    private void validateFileType(MultipartFile file) throws IOException {
        String fileType = file.getContentType();

        if (fileType == null || (!fileType.equals(MediaType.IMAGE_JPEG_VALUE) && !fileType.equals(
                MediaType.IMAGE_PNG_VALUE))) {
            throw new GlobalException(ErrorCode.INVALID_FILE_TYPE);
        }
    }

}
