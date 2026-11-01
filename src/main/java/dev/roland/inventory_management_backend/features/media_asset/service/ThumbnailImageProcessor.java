package dev.roland.inventory_management_backend.features.media_asset.service;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;

import org.springframework.stereotype.Component;

import com.luciad.imageio.webp.WebPWriteParam;

import dev.roland.inventory_management_backend.features.media_asset.MediaThumbnailException;
import dev.roland.inventory_management_backend.features.media_asset.ProductImageFormat;
import dev.roland.inventory_management_backend.features.media_asset.message.MediaMessageKey;

/** Creates bounded WebP thumbnails from supported product images. */
@Component
public class ThumbnailImageProcessor {
  public static final int MAX_DIMENSION = 128;
  private static final float WEBP_QUALITY = 0.82f;

  /**
   * Decodes, scales, and WebP-encodes an image while preserving transparency.
   *
   * @param original source image bytes
   * @return encoded thumbnail bytes
   * @throws MediaThumbnailException if the source image cannot be decoded or encoded
   */
  public byte[] createThumbnail(final byte[] original) {
    try {
      final BufferedImage source = ImageIO.read(new ByteArrayInputStream(original));
      if (source == null) {
        throw new MediaThumbnailException(MediaMessageKey.THUMBNAIL_IMAGE_INVALID, true);
      }
      final double scale =
          Math.min(1d, (double) MAX_DIMENSION / Math.max(source.getWidth(), source.getHeight()));
      final int width = Math.max(1, (int) Math.round(source.getWidth() * scale));
      final int height = Math.max(1, (int) Math.round(source.getHeight() * scale));
      final BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
      final AffineTransform transform = AffineTransform.getScaleInstance(scale, scale);
      new AffineTransformOp(transform, AffineTransformOp.TYPE_BICUBIC).filter(source, scaled);
      source.flush();
      return encodeWebp(scaled);
    } catch (IOException error) {
      throw new MediaThumbnailException(MediaMessageKey.THUMBNAIL_IMAGE_INVALID, true, error);
    }
  }

  private byte[] encodeWebp(final BufferedImage image) throws IOException {
    final var writers = ImageIO.getImageWritersByMIMEType(ProductImageFormat.WEBP.getMimeType());
    if (!writers.hasNext()) {
      throw new MediaThumbnailException(MediaMessageKey.THUMBNAIL_ENCODER_UNAVAILABLE, false);
    }
    final ImageWriter writer = writers.next();
    try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        MemoryCacheImageOutputStream output = new MemoryCacheImageOutputStream(bytes)) {
      final ImageWriteParam parameters = writer.getDefaultWriteParam();
      parameters.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
      parameters.setCompressionType(
          parameters.getCompressionTypes()[WebPWriteParam.LOSSY_COMPRESSION]);
      parameters.setCompressionQuality(WEBP_QUALITY);
      writer.setOutput(output);
      writer.write(null, new IIOImage(image, null, null), parameters);
      output.flush();
      return bytes.toByteArray();
    } finally {
      writer.dispose();
      image.flush();
    }
  }
}
