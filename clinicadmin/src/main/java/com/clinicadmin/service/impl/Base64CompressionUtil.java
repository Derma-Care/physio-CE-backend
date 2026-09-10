package com.clinicadmin.service.impl;

import java.util.Base64;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class Base64CompressionUtil {

    public static String compressBase64(String data) {
        if (data == null || data.isBlank()) {
            return data; // nothing to compress
        }
        try {
            byte[] input = Base64.getDecoder().decode(data);
            Deflater deflater = new Deflater();
            deflater.setInput(input);
            deflater.finish();

            byte[] buffer = new byte[1024];
            int compressedDataLength = deflater.deflate(buffer);

            byte[] compressedBytes = new byte[compressedDataLength];
            System.arraycopy(buffer, 0, compressedBytes, 0, compressedDataLength);

            return Base64.getEncoder().encodeToString(compressedBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return data;
        }
    }

    public static String decompressBase64(String data) {
        if (data == null || data.isBlank()) {
            return data; // nothing to decompress
        }
        try {
            byte[] compressedData = Base64.getDecoder().decode(data);
            Inflater inflater = new Inflater();
            inflater.setInput(compressedData);

            byte[] buffer = new byte[4096]; // large buffer
            int decompressedDataLength = inflater.inflate(buffer);

            byte[] decompressedBytes = new byte[decompressedDataLength];
            System.arraycopy(buffer, 0, decompressedBytes, 0, decompressedDataLength);

            return Base64.getEncoder().encodeToString(decompressedBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return data;
        }
    }
}
