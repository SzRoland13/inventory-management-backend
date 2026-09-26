package dev.roland.inventory_management_backend.common.configuration;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
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
   * @param endpoint endpoint supplied to this method
   * @param region region supplied to this method
   * @param accessKey access key supplied to this method
   * @param secretKey secret key supplied to this method
   * @return s3client result
   */
  @Bean
  public S3Client s3Client(
      @Value("${app.storage.endpoint}") final String endpoint,
      @Value("${app.storage.region}") final String region,
      @Value("${app.storage.access-key}") final String accessKey,
      @Value("${app.storage.secret-key}") final String secretKey) {
    return S3Client.builder()
        .endpointOverride(URI.create(endpoint))
        .region(Region.of(region))
        .credentialsProvider(
            StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
        .forcePathStyle(true)
        .build();
  }
}
