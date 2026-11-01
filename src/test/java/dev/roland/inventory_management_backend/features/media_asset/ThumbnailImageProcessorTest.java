package dev.roland.inventory_management_backend.features.media_asset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

import dev.roland.inventory_management_backend.features.media_asset.service.ThumbnailImageProcessor;

class ThumbnailImageProcessorTest {
  static {
    System.setProperty("java.awt.headless", "true");
  }

  private final ThumbnailImageProcessor processor = new ThumbnailImageProcessor();

  @Test
  void scalesLargeImageWithinTheThumbnailBoundAndEncodesWebp() throws Exception {
    final BufferedImage source = new BufferedImage(4096, 2048, BufferedImage.TYPE_INT_RGB);
    source.setRGB(0, 0, Color.BLUE.getRGB());
    final ByteArrayOutputStream input = new ByteArrayOutputStream();
    ImageIO.write(source, "png", input);

    final byte[] thumbnail = processor.createThumbnail(input.toByteArray());
    final BufferedImage decoded = ImageIO.read(new ByteArrayInputStream(thumbnail));

    assertNotNull(decoded);
    assertEquals(128, decoded.getWidth());
    assertEquals(64, decoded.getHeight());
    assertEquals(0x52, thumbnail[0] & 0xff);
    assertEquals(0x49, thumbnail[1] & 0xff);
    assertEquals(0x46, thumbnail[2] & 0xff);
    assertEquals(0x46, thumbnail[3] & 0xff);
  }

  @Test
  void retainsTransparentPixels() throws Exception {
    final BufferedImage source = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
    source.setRGB(2, 2, 0x00112233);
    final ByteArrayOutputStream input = new ByteArrayOutputStream();
    ImageIO.write(source, "png", input);

    final byte[] thumbnail = processor.createThumbnail(input.toByteArray());
    final BufferedImage decoded = ImageIO.read(new ByteArrayInputStream(thumbnail));

    assertNotNull(decoded);
    assertEquals(0, decoded.getColorModel().hasAlpha() ? decoded.getRGB(2, 2) >>> 24 : 255);
  }

  @Test
  void acceptsJpegAndWebpSources() throws Exception {
    final BufferedImage source = new BufferedImage(256, 128, BufferedImage.TYPE_INT_RGB);
    final ByteArrayOutputStream jpeg = new ByteArrayOutputStream();
    ImageIO.write(source, "jpeg", jpeg);
    assertBoundedWebp(processor.createThumbnail(jpeg.toByteArray()));

    final byte[] webp = processor.createThumbnail(jpeg.toByteArray());
    assertBoundedWebp(processor.createThumbnail(webp));
  }

  private void assertBoundedWebp(final byte[] bytes) throws Exception {
    final BufferedImage decoded = ImageIO.read(new ByteArrayInputStream(bytes));
    assertNotNull(decoded);
    assertEquals(128, decoded.getWidth());
    assertEquals(64, decoded.getHeight());
  }
}
