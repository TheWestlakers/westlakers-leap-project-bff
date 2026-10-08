package com.westlakers.leap_bff.exceptions;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import com.westlakers.leap_bff.dtos.ErrorResponse;

@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles ApiException thrown from service layer.
     * @param ex the ApiException
     * @param request the web request
     * @return ResponseEntity with ErrorResponse
     */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
            ex.getErrorCode().getCode(),
            ex.getMessage(),
            ex.getDetails(),
            ex.getErrorCode().getStatus().value(),
            LocalDateTime.now(),
            request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(errorResponse, ex.getErrorCode().getStatus());
    }

    /**
     * Handles IllegalArgumentException.
     * @param ex the IllegalArgumentException
     * @param request the web request
     * @return ResponseEntity with ErrorResponse
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
            ErrorCode.INVALID_INPUT.getCode(),
            ErrorCode.INVALID_INPUT.getMessage(),
            ex.getMessage(),
            HttpStatus.BAD_REQUEST.value(),
            LocalDateTime.now(),
            request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles all other exceptions.
     * @param ex the exception
     * @param request the web request
     * @return ResponseEntity with ErrorResponse
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
            ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
            ErrorCode.INTERNAL_SERVER_ERROR.getMessage(),
            ex.getMessage(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            LocalDateTime.now(),
            request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
