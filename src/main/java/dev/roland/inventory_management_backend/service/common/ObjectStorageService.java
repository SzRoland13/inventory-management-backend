package dev.roland.inventory_management_backend.service.common;

import java.io.InputStream;
import java.net.URI;
import java.time.Duration;

import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.configuration.AppConfiguration;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@Service
@RequiredArgsConstructor
public class ObjectStorageService {
  private final S3Client s3Client;
  private final AppConfiguration appConfiguration;

  public void upload(
      String objectPath, long contentLength, String contentType, InputStream inputStream) {
    PutObjectRequest request =
        PutObjectRequest.builder()
            .bucket(appConfiguration.getS3bucket())
            .key(objectPath)
            .contentType(contentType)
            .build();

    s3Client.putObject(request, RequestBody.fromInputStream(inputStream, contentLength));
  }

  public void delete(String objectPath) {
    s3Client.deleteObject(
        DeleteObjectRequest.builder()
            .bucket(appConfiguration.getS3bucket())
            .key(objectPath)
            .build());
  }

  public String generatePresignedUrl(String objectPath) {
    try (S3Presigner presigner =
        S3Presigner.builder()
            .endpointOverride(URI.create(appConfiguration.getS3Endpoint()))
            .region(Region.of(appConfiguration.getS3Region()))
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(
                        appConfiguration.getS3AccessKey(), appConfiguration.getS3SecretKey())))
            .serviceConfiguration(
                software.amazon.awssdk.services.s3.S3Configuration.builder()
                    .pathStyleAccessEnabled(true)
                    .build())
            .build()) {

      GetObjectRequest objectRequest =
          GetObjectRequest.builder().bucket(appConfiguration.getS3bucket()).key(objectPath).build();

      GetObjectPresignRequest presignRequest =
          GetObjectPresignRequest.builder()
              .signatureDuration(Duration.ofHours(2))
              .getObjectRequest(objectRequest)
              .build();

      PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);

      return presignedRequest.url().toExternalForm();
    }
  }
}
