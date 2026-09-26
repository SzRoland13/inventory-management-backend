package dev.roland.inventory_management_backend.common.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.roland.inventory_management_backend.common.configuration.AppConfiguration;
import dev.roland.inventory_management_backend.features.media_usage.enumeration.MediaEntityType;
import dev.roland.inventory_management_backend.features.media_usage.enumeration.MediaUsageType;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@ExtendWith(MockitoExtension.class)
class ObjectStorageServiceTest {

  @Mock private S3Client s3Client;
  @Mock private AppConfiguration configuration;

  private ObjectStorageService service;

  @BeforeEach
  void setUp() {
    service = new ObjectStorageService(s3Client, configuration);
  }

  @Test
  void createsStableEntityPathAndUniqueTemporaryNames() {
    assertEquals(
        "user/42/avatar/profile.png",
        service.generateSolidObjectPath(
            "profile.png", MediaEntityType.USER, 42L, MediaUsageType.AVATAR));

    final var first = service.generateTempObjectPath("report.final.pdf");
    final var second = service.generateTempObjectPath("README");
    assertTrue(first.getObjectPath().startsWith("temp/"));
    assertTrue(first.getFilename().endsWith(".pdf"));
    assertEquals("temp/" + first.getFilename(), first.getObjectPath());
    assertTrue(second.getFilename().matches("[0-9a-f]{32}"));
    assertTrue(!first.getFilename().equals(second.getFilename()));
  }

  @Test
  void uploadsWithConfiguredBucketAndMovesByCopyingBeforeDeleting() {
    when(configuration.getS3Bucket()).thenReturn("inventory");
    service.upload("temp/file.txt", 4L, "text/plain", new ByteArrayInputStream("data".getBytes()));
    service.move("temp/source.txt", "users/1/avatar/source.txt");

    verify(s3Client)
        .putObject(
            any(PutObjectRequest.class), any(software.amazon.awssdk.core.sync.RequestBody.class));
    final InOrder order = inOrder(s3Client);
    order.verify(s3Client).copyObject(any(CopyObjectRequest.class));
    order.verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
  }

  @Test
  void deletesRequestedKey() {
    when(configuration.getS3Bucket()).thenReturn("inventory");
    service.delete("unused/object");

    verify(s3Client)
        .deleteObject(
            org.mockito.ArgumentMatchers.argThat(
                (DeleteObjectRequest request) ->
                    request.bucket().equals("inventory") && request.key().equals("unused/object")));
  }

  @Test
  void generatesSignedReadAndUploadUrlsWithExpectedExpiries() {
    when(configuration.getS3Bucket()).thenReturn("inventory");
    when(configuration.getS3Endpoint()).thenReturn("http://localhost:8333");
    when(configuration.getS3Region()).thenReturn("us-east-1");
    when(configuration.getS3AccessKey()).thenReturn("test-key");
    when(configuration.getS3SecretKey()).thenReturn("test-secret");

    final var getUrl = service.generatePresignedGetUrl("users/1/avatar.png");
    final var putUrl = service.generatePresignedPutUrl("temp/avatar.png", "image/png");

    assertTrue(getUrl.getUrl().startsWith("http://localhost:8333/inventory/users/1/avatar.png"));
    assertTrue(putUrl.getUrl().startsWith("http://localhost:8333/inventory/temp/avatar.png"));
    assertTrue(getUrl.getExpiry().isAfter(java.time.Instant.now().plusSeconds(7_000)));
    assertTrue(putUrl.getExpiry().isAfter(java.time.Instant.now().plusSeconds(500)));
  }
}
