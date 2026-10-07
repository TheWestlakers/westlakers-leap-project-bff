package com.westlakers.leap_bff.dtos;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

public record ErrorResponse(
    String errorCode,
    String message,
    String details,
    int status,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime timestamp,
    String path
) {
}
