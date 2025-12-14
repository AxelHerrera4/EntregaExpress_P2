package com.logiflow.fleetservice.exception;

import com.logiflow.fleetservice.dto.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleResourceNotFound(
          ResourceNotFoundException ex,
          WebRequest request
  ) {
    log.error("Resource not found: {}", ex.getMessage());

    ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.NOT_FOUND.value())
            .error("Not Found")
            .message(ex.getMessage())
            .path(request.getDescription(false).replace("uri=", ""))
            .build();

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
  }

  @ExceptionHandler(DuplicateResourceException.class)
  public ResponseEntity<ErrorResponse> handleDuplicateResource(
          DuplicateResourceException ex,
          WebRequest request
  ) {
    log.error("Duplicate resource: {}", ex.getMessage());

    ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.CONFLICT.value())
            .error("Conflict")
            .message(ex.getMessage())
            .path(request.getDescription(false).replace("uri=", ""))
            .build();

    return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
  }

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ErrorResponse> handleBusinessException(
          BusinessException ex,
          WebRequest request
  ) {
    log.error("Business rule violation: {}", ex.getMessage());

    ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Bad Request")
            .message(ex.getMessage())
            .path(request.getDescription(false).replace("uri=", ""))
            .build();

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationExceptions(
          MethodArgumentNotValidException ex,
          WebRequest request
  ) {
    log.error("Validation error: {}", ex.getMessage());

    Map<String, String> validationErrors = new HashMap<>();
    ex.getBindingResult().getAllErrors().forEach(error -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage();
      validationErrors.put(fieldName, errorMessage);
    });

    ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Validation Failed")
            .message("Error en la validación de datos")
            .path(request.getDescription(false).replace("uri=", ""))
            .validationErrors(validationErrors)
            .build();

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgument(
          IllegalArgumentException ex,
          WebRequest request
  ) {
    log.error("Illegal argument: {}", ex.getMessage());

    ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Bad Request")
            .message(ex.getMessage())
            .path(request.getDescription(false).replace("uri=", ""))
            .build();

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
          HttpMessageNotReadableException ex,
          WebRequest request
  ) {
    log.error("JSON parsing error: {}", ex.getMessage());

    String message = "Error al leer el JSON. Verifique el formato de los datos enviados";
    
    // Extraer detalles específicos si es un error de enum
    Throwable cause = ex.getCause();
    if (cause instanceof InvalidFormatException invalidFormatEx) {
      String fieldName = invalidFormatEx.getPath().stream()
              .map(ref -> ref.getFieldName())
              .reduce((first, second) -> second)
              .orElse("campo desconocido");
      
      Object invalidValue = invalidFormatEx.getValue();
      Class<?> targetType = invalidFormatEx.getTargetType();
      
      if (targetType.isEnum()) {
        String validValues = String.join(", ", getEnumValues(targetType));
        message = String.format(
                "Valor inválido '%s' para el campo '%s'. Valores permitidos: [%s]",
                invalidValue, fieldName, validValues
        );
      } else {
        message = String.format(
                "El valor '%s' no es válido para el campo '%s'. Se esperaba tipo: %s",
                invalidValue, fieldName, targetType.getSimpleName()
        );
      }
    }

    ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Bad Request")
            .message(message)
            .path(request.getDescription(false).replace("uri=", ""))
            .build();

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  /**
   * Obtiene los valores permitidos de un enum
   */
  private String[] getEnumValues(Class<?> enumClass) {
    Object[] enumConstants = enumClass.getEnumConstants();
    String[] values = new String[enumConstants.length];
    for (int i = 0; i < enumConstants.length; i++) {
      values[i] = enumConstants[i].toString();
    }
    return values;
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGlobalException(
          Exception ex,
          WebRequest request
  ) {
    log.error("Unexpected error", ex);

    ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .error("Internal Server Error")
            .message("Ocurrió un error inesperado. Por favor contacte al administrador")
            .path(request.getDescription(false).replace("uri=", ""))
            .build();

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
  }
}