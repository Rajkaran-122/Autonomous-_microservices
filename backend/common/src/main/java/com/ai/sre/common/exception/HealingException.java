package com.ai.sre.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a self-healing action fails to execute.
 */
public class HealingException extends SreException {

    public HealingException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR, "HEALING_FAILED");
    }

    public HealingException(String message, Throwable cause) {
        super(message, cause, HttpStatus.INTERNAL_SERVER_ERROR, "HEALING_FAILED");
    }
}
