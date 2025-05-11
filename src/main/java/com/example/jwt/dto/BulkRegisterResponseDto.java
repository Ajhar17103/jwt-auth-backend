package com.example.jwt.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BulkRegisterResponseDto {
    private int successCount;
    private int failureCount;
    private String reportDownloadUrl;
}

