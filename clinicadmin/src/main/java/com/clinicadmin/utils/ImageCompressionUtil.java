package com.clinicadmin.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.springframework.stereotype.Component;

import net.coobird.thumbnailator.Thumbnails;

@Component
public class ImageCompressionUtil {

	private static final int MAX_SIZE_BYTES = 250 * 1024;

	/**
	 * Iteratively reduces quality, then dimensions, until under 250KB.
	 * Supports jpg/jpeg/png (webp is intentionally excluded — handled
	 * separately by the caller since ImageIO/Thumbnailator can't
	 * re-encode webp without an extra plugin).
	 */
	public byte[] compressImage(byte[] originalBytes, String formatName) throws Exception {
		String format = ("jpg".equalsIgnoreCase(formatName) || "jpeg".equalsIgnoreCase(formatName))
				? "jpg" : formatName;

		byte[] output = originalBytes;

		// Pass 1: reduce JPEG quality (meaningful only for jpg; png is lossless)
		if ("jpg".equals(format)) {
			float quality = 0.85f;
			while (output.length > MAX_SIZE_BYTES && quality > 0.1f) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				Thumbnails.of(new ByteArrayInputStream(output))
				       .scale(1.0)
						.outputQuality(quality)
						.outputFormat("jpg")
						.toOutputStream(baos);
				output = baos.toByteArray();
				quality -= 0.1f;
			}
		}

		// Pass 2: scale down dimensions if still too large (handles png too)
		// Pass 2: scale down dimensions if still too large
		double scale = 0.9;
		while (output.length > MAX_SIZE_BYTES && scale > 0.15) {
		    ByteArrayOutputStream baos = new ByteArrayOutputStream();
		    Thumbnails.of(new ByteArrayInputStream(output))
		            .scale(scale)
		            .outputQuality(0.7)
		            .outputFormat(format)
		            .toOutputStream(baos);
		    output = baos.toByteArray();
		    scale -= 0.1;
		
		}
		
		if (output.length > MAX_SIZE_BYTES) {
		    throw new IllegalArgumentException(
		            "Unable to compress image below 250 KB.");
		}

		return output;
	}
}