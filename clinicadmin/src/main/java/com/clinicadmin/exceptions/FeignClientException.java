package com.clinicadmin.exceptions;

public class FeignClientException extends RuntimeException {
    private final int statusCode;

    public FeignClientException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
