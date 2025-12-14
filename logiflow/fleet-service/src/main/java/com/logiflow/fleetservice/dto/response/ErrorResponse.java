package com.logiflow.fleetservice.dto.response;

import lombok.*;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
  private LocalDateTime timestamp;
  private Integer status;
  private String error;
  private String message;
  private String path;
  private Map<String, String> validationErrors;
}