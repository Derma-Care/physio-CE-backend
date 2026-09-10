package com.clinicadmin.utils;

import org.springframework.stereotype.Component;

@Component
public class FileSignatureUtil {

	/**
	 * Detects file extension by inspecting magic bytes.
	 * Returns null if the format is unrecognized.
	 */
	public String detectImageExtension(byte[] bytes) {
		if (bytes == null || bytes.length < 12) return null;

		// JPEG: FF D8 FF
		if ((bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8 && (bytes[2] & 0xFF) == 0xFF) {
			return "jpg";
		}
		// PNG: 89 50 4E 47 0D 0A 1A 0A
		if ((bytes[0] & 0xFF) == 0x89 && bytes[1] == 0x50 && bytes[2] == 0x4E && bytes[3] == 0x47) {
			return "png";
		}
		// WEBP: "RIFF"...."WEBP"
		if (bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F'
				&& bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P') {
			return "webp";
		}
		return null;
	}

	public String detectAudioExtension(byte[] bytes) {
		if (bytes == null || bytes.length < 12) return null;

		// MP3: "ID3" tag, or frame sync 0xFFEx/0xFFFx
		if ((bytes[0] == 'I' && bytes[1] == 'D' && bytes[2] == '3')
				|| ((bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xE0) == 0xE0)) {
			return "mp3";
		}
		// WAV: "RIFF"...."WAVE"
		if (bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F'
				&& bytes[8] == 'W' && bytes[9] == 'A' && bytes[10] == 'V' && bytes[11] == 'E') {
			return "wav";
		}
		// OGG: "OggS"
		if (bytes[0] == 'O' && bytes[1] == 'g' && bytes[2] == 'g' && bytes[3] == 'S') {
			return "ogg";
		}
		// M4A/AAC (ISO base media, "ftyp" box at offset 4)
		if (bytes.length > 8 && bytes[4] == 'f' && bytes[5] == 't' && bytes[6] == 'y' && bytes[7] == 'p') {
			return "m4a";
		}
		return null;
	}
}