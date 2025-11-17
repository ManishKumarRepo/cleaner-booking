package com.cleaning.booking.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Standard API error response model.
 */
@Schema(description = "Error response wrapper")
public record ApiErrorResponse(

        @Schema(description = "Error message", example = "Invalid start time")
        String message,

        @Schema(description = "HTTP Status code", example = "400")
        int status,

        @Schema(description = "Timestamp of error")
        LocalDateTime timestamp,

        @Schema(description = "Detailed errors")
        Object details
) {}