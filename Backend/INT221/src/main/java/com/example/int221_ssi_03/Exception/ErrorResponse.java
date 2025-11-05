package com.example.int221_ssi_03.Exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@Schema(description = "Error response object when an exception occurs")
public class ErrorResponse {
    @Schema(example = "2025-05-01T02:32:36.863")
    private LocalDateTime timestamp;

    @Schema(example = "404")
    private int status;

    @Schema(example = "Not Found")
    private String error;

    @Schema(example = "Sale item not found for this id :: 5")
    private String message;

    @Schema(example = "/v1/sale-items/5")
    private String path;


    public ErrorResponse(int status, String message, String path, String error) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.message = message;
        this.path = path;
        this.error = error;
    }
}
