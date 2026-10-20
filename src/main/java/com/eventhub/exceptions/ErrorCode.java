package com.eventhub.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "ERR_500", "An unexpected error occurred"),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "ERR_404", "Resource not found"),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "ERR_400", "Bad request"),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "ERR_400_VALIDATION", "Validation failed"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "ERR_401", "Unauthorized access"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "ERR_403", "Access denied"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "ERR_405", "Method not allowed"),
    CONFLICT(HttpStatus.CONFLICT, "ERR_409", "Resource conflict or already exists");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
