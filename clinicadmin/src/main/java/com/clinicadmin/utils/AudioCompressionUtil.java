package com.clinicadmin.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

@Component
public class AudioCompressionUtil {

	private static final long MAX_SIZE_BYTES = 250 * 1024;

	/**
	 * Requires ffmpeg + ffprobe on the host. Computes a target bitrate from
	 * duration so the output lands under 250KB, then transcodes to mono mp3.
	 */
	public byte[] compressAudio(byte[] originalBytes, String extension) throws Exception {
		if (originalBytes.length <= MAX_SIZE_BYTES) {
			return originalBytes;
		}

		Path inputFile = Files.createTempFile("audio_in_", "." + extension);
		Path outputFile = Files.createTempFile("audio_out_", ".mp3");

		try {
			Files.write(inputFile, originalBytes);

			double durationSeconds = getAudioDurationSeconds(inputFile);
			if (durationSeconds <= 0) {
				durationSeconds = 60; // safe fallback if ffprobe can't read duration
			}

			int targetBitrateKbps = (int) Math.floor((MAX_SIZE_BYTES * 8.0) / (durationSeconds * 1000));
			targetBitrateKbps = Math.max(16, Math.min(targetBitrateKbps, 64));

			runFfmpeg(inputFile, outputFile, targetBitrateKbps);

			byte[] compressed = Files.readAllBytes(outputFile);

			// One more aggressive pass if still over the limit
			// One more aggressive pass if still over the limit
			if (compressed.length > MAX_SIZE_BYTES) {
			    Path secondPass = Files.createTempFile("audio_out2_", ".mp3");
			    try {
			        runFfmpeg(inputFile, secondPass, 16);
			        compressed = Files.readAllBytes(secondPass);
			    } finally {
			        Files.deleteIfExists(secondPass);
			    }
			}

			// Final validation
			if (compressed.length > MAX_SIZE_BYTES) {
			    throw new IllegalArgumentException(
			            "Unable to compress audio below 250 KB.");
			}

			return compressed;
		} finally {
			Files.deleteIfExists(inputFile);
			Files.deleteIfExists(outputFile);
		}
	}

	private void runFfmpeg(Path input, Path output, int bitrateKbps) throws Exception {
		ProcessBuilder pb = new ProcessBuilder(
				"ffmpeg", "-y", "-i", input.toString(),
				"-b:a", bitrateKbps + "k",
				"-ac", "1",
				output.toString());
		pb.redirectErrorStream(true);
		Process process = pb.start();

		try (InputStream is = process.getInputStream()) {
			is.readAllBytes(); // drain to prevent process hanging on full buffer
		}

		boolean finished = process.waitFor(60, TimeUnit.SECONDS);
		if (!finished || process.exitValue() != 0) {
			throw new RuntimeException("Audio compression failed (ffmpeg exited abnormally)");
		}
	}

	private double getAudioDurationSeconds(Path file) {
		try {
			ProcessBuilder pb = new ProcessBuilder(
					"ffprobe", "-v", "error", "-show_entries", "format=duration",
					"-of", "default=noprint_wrappers=1:nokey=1", file.toString());
			pb.redirectErrorStream(true);
			Process process = pb.start();

			String result;
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				result = reader.readLine();
			}
			process.waitFor(20, TimeUnit.SECONDS);
			return result != null ? Double.parseDouble(result.trim()) : -1;
		} catch (Exception e) {
			return -1;
		}
	}
}