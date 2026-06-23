package com.ai.sre.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Base exception for all SRE platform errors.
 * Extends RuntimeException for unchecked exception semantics.
 */
public class SreException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final String errorCode;

    public SreException(String message) {
        this(message, HttpStatus.INTERNAL_SERVER_ERROR, "SRE_ERROR");
    }

    public SreException(String message, HttpStatus httpStatus) {
        this(message, httpStatus, "SRE_ERROR");
    }

    public SreException(String message, HttpStatus httpStatus, String errorCode) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }

    public SreException(String message, Throwable cause) {
        this(message, cause, HttpStatus.INTERNAL_SERVER_ERROR, "SRE_ERROR");
    }

    public SreException(String message, Throwable cause, HttpStatus httpStatus, String errorCode) {
        super(message, cause);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }

    public HttpStatus getHttpStatus() { return httpStatus; }
    public String getErrorCode() { return errorCode; }
}
