package com.ai.sre.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a requested resource is not found.
 */
public class ResourceNotFoundException extends SreException {

    public ResourceNotFoundException(String resourceType, Object resourceId) {
        super(
                String.format("%s not found with id: %s", resourceType, resourceId),
                HttpStatus.NOT_FOUND,
                "RESOURCE_NOT_FOUND"
        );
    }

    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND");
    }
}
