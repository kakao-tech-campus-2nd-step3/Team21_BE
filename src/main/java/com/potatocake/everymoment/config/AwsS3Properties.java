package com.potatocake.everymoment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aws.s3")
public record AwsS3Properties(
        String accessKey,
        String secretKey,
        String region,
        String bucket
) {
}
