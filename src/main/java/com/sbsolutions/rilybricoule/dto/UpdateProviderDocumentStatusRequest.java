package com.sbsolutions.rilybricoule.dto;

import lombok.Data;

@Data
public class UpdateProviderDocumentStatusRequest {
    private String status;
    private String reviewNote;
}