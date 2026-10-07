package com.westlakers.leap_bff.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Enum for standardized error codes used throughout the application.
 * Each error code has an associated HTTP status and user-friendly message.
 */
public enum ErrorCode {
    // Resource Not Found (404)
    HOLDING_NOT_FOUND("ERR_001", HttpStatus.NOT_FOUND, "Holding not found"),
    ACCOUNT_NOT_FOUND("ERR_002", HttpStatus.NOT_FOUND, "Account not found"),
    INSTRUMENT_NOT_FOUND("ERR_003", HttpStatus.NOT_FOUND, "Instrument not found"),
    ORDER_NOT_FOUND("ERR_004", HttpStatus.NOT_FOUND, "Order not found"),
    USER_NOT_FOUND("ERR_005", HttpStatus.NOT_FOUND, "User not found"),
    RESOURCE_NOT_FOUND("ERR_006", HttpStatus.NOT_FOUND, "Resource not found"),

    // Validation Errors (400)
    INVALID_INPUT("ERR_010", HttpStatus.BAD_REQUEST, "Invalid input provided"),
    MISSING_REQUIRED_FIELD("ERR_011", HttpStatus.BAD_REQUEST, "Required field is missing"),
    ACCOUNT_ID_REQUIRED("ERR_012", HttpStatus.BAD_REQUEST, "Account ID is required"),
    INSTRUMENT_ID_REQUIRED("ERR_013", HttpStatus.BAD_REQUEST, "Instrument ID is required"),
    QUANTITY_REQUIRED("ERR_014", HttpStatus.BAD_REQUEST, "Quantity is required"),
    AVERAGE_PRICE_REQUIRED("ERR_015", HttpStatus.BAD_REQUEST, "Average Price is required"),
    INVALID_QUANTITY("ERR_016", HttpStatus.BAD_REQUEST, "Invalid quantity provided"),
    INVALID_PRICE("ERR_017", HttpStatus.BAD_REQUEST, "Invalid price provided"),
    INVALID_ACCOUNT_TYPE("ERR_018", HttpStatus.BAD_REQUEST, "Invalid account type"),
    EMPTY_HOLDINGS_LIST("ERR_019", HttpStatus.BAD_REQUEST, "Holdings list is empty"),
    EMPTY_RESULTS("ERR_020", HttpStatus.BAD_REQUEST, "No results found"),

    // Operation/Server Errors (500)
    CREATION_FAILED("ERR_030", HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create resource"),
    UPDATE_FAILED("ERR_031", HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update resource"),
    DELETE_FAILED("ERR_032", HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete resource"),
    HOLDING_CREATION_FAILED("ERR_033", HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create holding"),
    HOLDING_UPDATE_FAILED("ERR_034", HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update holding"),
    HOLDING_DELETE_FAILED("ERR_035", HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete holding"),
    ACCOUNT_CREATION_FAILED("ERR_036", HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create account"),
    ORDER_CREATION_FAILED("ERR_037", HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create order"),
    DATABASE_ERROR("ERR_038", HttpStatus.INTERNAL_SERVER_ERROR, "Database operation failed"),
    OPERATION_FAILED("ERR_039", HttpStatus.INTERNAL_SERVER_ERROR, "Operation failed"),

    // Authentication/Authorization Errors (401/403)
    UNAUTHORIZED("ERR_040", HttpStatus.UNAUTHORIZED, "User is not authenticated"),
    FORBIDDEN("ERR_041", HttpStatus.FORBIDDEN, "User does not have permission"),
    INVALID_CREDENTIALS("ERR_042", HttpStatus.UNAUTHORIZED, "Invalid credentials provided"),

    // Generic Error
    INTERNAL_SERVER_ERROR("ERR_999", HttpStatus.INTERNAL_SERVER_ERROR, "An internal server error occurred");

    private final String code;
    private final HttpStatus status;
    private final String message;

    ErrorCode(String code, HttpStatus status, String message) {
        this.code = code;
        this.status = status;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
