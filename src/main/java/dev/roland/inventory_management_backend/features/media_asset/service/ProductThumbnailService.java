package dev.roland.inventory_management_backend.features.media_asset.service;

import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.common.async.AsyncJobType;
import dev.roland.inventory_management_backend.common.async.OutboxService;
import dev.roland.inventory_management_backend.features.media_asset.MediaAsset;
import dev.roland.inventory_management_backend.features.media_asset.MediaThumbnailStatus;
import dev.roland.inventory_management_backend.features.media_asset.dto.ProductImageThumbnailJobPayload;
import lombok.RequiredArgsConstructor;

/** Shared rules for assigning and enqueueing product image thumbnails. */
@Service
@RequiredArgsConstructor
public class ProductThumbnailService {
  private static final String OBJECT_DIRECTORY = "thumbnails";
  private static final String FILE_EXTENSION = ".webp";

  private final OutboxService outboxService;

  public String objectPath(final Long mediaAssetId) {
    return OBJECT_DIRECTORY + "/" + mediaAssetId + FILE_EXTENSION;
  }

  public boolean enqueueIfNeeded(final MediaAsset asset, final Long companyId) {
    if (asset.getThumbnailObjectPath() != null
        && (asset.getThumbnailStatus() == MediaThumbnailStatus.READY
            || asset.getThumbnailStatus() == MediaThumbnailStatus.PENDING)) {
      return false;
    }
    asset.setThumbnailObjectPath(objectPath(asset.getId()));
    asset.setThumbnailStatus(MediaThumbnailStatus.PENDING);
    outboxService.enqueue(
        AsyncJobType.PRODUCT_IMAGE_THUMBNAIL,
        new ProductImageThumbnailJobPayload(asset.getId()),
        companyId,
        null);

    return true;
  }
}
