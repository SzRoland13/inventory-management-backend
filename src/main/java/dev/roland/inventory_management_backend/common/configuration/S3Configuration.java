package dev.roland.inventory_management_backend.common.configuration;

import java.net.URI;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/** Creates the S3 client and presigner for the configured object-storage endpoint. */
@Configuration
public class S3Configuration {
  /**
   * Creates the S3 client configured for the application storage endpoint.
   *
   * @param appConfiguration application settings
   * @return s3client result
   */
  @Bean
  public S3Client s3Client(final AppConfiguration appConfiguration) {
    return S3Client.builder()
        .endpointOverride(URI.create(appConfiguration.getS3Endpoint()))
        .region(Region.of(appConfiguration.getS3Region()))
        .credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(
                    appConfiguration.getS3AccessKey(), appConfiguration.getS3SecretKey())))
        .forcePathStyle(true)
        .build();
  }
}
