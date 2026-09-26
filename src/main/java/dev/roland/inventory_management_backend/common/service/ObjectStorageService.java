package dev.roland.inventory_management_backend.common.service;

import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.common.configuration.AppConfiguration;
import dev.roland.inventory_management_backend.features.media_asset.dto.GeneratedMediaPathAndName;
import dev.roland.inventory_management_backend.features.media_asset.dto.PresignedUrlData;
import dev.roland.inventory_management_backend.features.media_usage.enumeration.MediaEntityType;
import dev.roland.inventory_management_backend.features.media_usage.enumeration.MediaUsageType;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

/** Stores media objects and creates access URLs through the configured S3-compatible service. */
@Service
@RequiredArgsConstructor
public class ObjectStorageService {
  private final S3Client s3Client;
  private final AppConfiguration appConfiguration;

  /**
   * Uploads an object to the configured S3-compatible bucket.
   *
   * @param objectPath object key to write
   * @param contentLength number of bytes in the input stream
   * @param contentType MIME type to store with the object
   * @param inputStream object content stream
   */
  public void upload(
      final String objectPath,
      final long contentLength,
      final String contentType,
      final InputStream inputStream) {
    final PutObjectRequest request =
        PutObjectRequest.builder()
            .bucket(appConfiguration.getS3Bucket())
            .key(objectPath)
            .contentType(contentType)
            .build();

    s3Client.putObject(request, RequestBody.fromInputStream(inputStream, contentLength));
  }

  /**
   * Deletes an object from the configured S3-compatible bucket.
   *
   * @param objectPath object key to delete
   */
  public void delete(final String objectPath) {
    s3Client.deleteObject(
        DeleteObjectRequest.builder()
            .bucket(appConfiguration.getS3Bucket())
            .key(objectPath)
            .build());
  }

  /**
   * Generates a temporary URL for reading an object.
   *
   * @param objectPath object key to expose
   * @return presigned GET URL and expiry timestamp
   */
  public PresignedUrlData generatePresignedGetUrl(final String objectPath) {
    try (S3Presigner presigner = createPresigner()) {

      final GetObjectRequest objectRequest =
          GetObjectRequest.builder().bucket(appConfiguration.getS3Bucket()).key(objectPath).build();

      final Duration duration = Duration.ofHours(2);

      final GetObjectPresignRequest presignRequest =
          GetObjectPresignRequest.builder()
              .signatureDuration(duration)
              .getObjectRequest(objectRequest)
              .build();

      final PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);

      return PresignedUrlData.builder()
          .url(presignedRequest.url().toExternalForm())
          .expiry(Instant.now().plus(duration))
          .build();
    }
  }

  /**
   * Builds the permanent object path for media attached to a domain entity.
   *
   * @param fileName stored filename
   * @param entityType entity type that owns the media
   * @param entityId owning entity id
   * @param usageType usage role of the media
   * @return permanent object key
   */
  public String generateSolidObjectPath(
      final String fileName,
      final MediaEntityType entityType,
      final Long entityId,
      final MediaUsageType usageType) {
    return entityType.getName().toLowerCase(Locale.ROOT)
        + "/"
        + entityId
        + "/"
        + usageType.getName().toLowerCase(Locale.ROOT)
        + "/"
        + fileName;
  }

  /**
   * Generates a temporary object path and sanitized generated filename for an upload.
   *
   * @param originalFilename filename supplied by the client
   * @return generated temporary object path and filename
   */
  public GeneratedMediaPathAndName generateTempObjectPath(final String originalFilename) {
    String extension = "";

    final int dotIndex = originalFilename.lastIndexOf(".");
    if (dotIndex != -1) {
      extension = originalFilename.substring(dotIndex);
    }

    final String newFileName = UUID.randomUUID().toString().replace("-", "") + extension;

    return GeneratedMediaPathAndName.builder()
        .objectPath("temp/" + newFileName)
        .filename(newFileName)
        .build();
  }

  /**
   * Moves an object by copying it to a new key and deleting the source key.
   *
   * @param sourceKey existing object key
   * @param destinationKey target object key
   */
  public void move(final String sourceKey, final String destinationKey) {
    final CopyObjectRequest copyRequest =
        CopyObjectRequest.builder()
            .sourceBucket(appConfiguration.getS3Bucket())
            .sourceKey(sourceKey)
            .destinationBucket(appConfiguration.getS3Bucket())
            .destinationKey(destinationKey)
            .build();

    s3Client.copyObject(copyRequest);

    delete(sourceKey);
  }

  /**
   * Generates a temporary URL for uploading an object directly to storage.
   *
   * @param objectPath object key to write
   * @param mimeType expected MIME type for the upload
   * @return presigned PUT URL and expiry timestamp
   */
  public PresignedUrlData generatePresignedPutUrl(final String objectPath, final String mimeType) {
    try (S3Presigner presigner = createPresigner()) {
      final PutObjectRequest objectRequest =
          PutObjectRequest.builder()
              .bucket(appConfiguration.getS3Bucket())
              .key(objectPath)
              .contentType(mimeType)
              .build();

      final Duration duration = Duration.ofMinutes(10);

      final PutObjectPresignRequest presignRequest =
          PutObjectPresignRequest.builder()
              .signatureDuration(duration)
              .putObjectRequest(objectRequest)
              .build();

      final PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(presignRequest);

      return PresignedUrlData.builder()
          .url(presignedRequest.url().toExternalForm())
          .expiry(Instant.now().plus(duration))
          .build();
    }
  }

  private S3Presigner createPresigner() {
    return S3Presigner.builder()
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
        .build();
  }
}
