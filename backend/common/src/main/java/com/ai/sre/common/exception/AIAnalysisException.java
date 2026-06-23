package com.ai.sre.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when AI analysis fails (LLM timeout, parsing error, rate limit, etc.).
 */
public class AIAnalysisException extends SreException {

    public AIAnalysisException(String message) {
        super(message, HttpStatus.SERVICE_UNAVAILABLE, "AI_ANALYSIS_FAILED");
    }

    public AIAnalysisException(String message, Throwable cause) {
        super(message, cause, HttpStatus.SERVICE_UNAVAILABLE, "AI_ANALYSIS_FAILED");
    }
}
