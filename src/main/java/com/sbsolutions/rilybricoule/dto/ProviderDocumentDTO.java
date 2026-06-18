package com.sbsolutions.rilybricoule.dto.admin;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProviderDocumentDTO {

    private Long id;

    private Long prestataireId;

    private String type;

    private String fileUrl;

    private String originalFileName;

    private String contentType;

    private Long fileSize;

    private String status;

    private String reviewNote;

    private Long reviewedByAdminId;

    private LocalDateTime createdAt;

    private LocalDateTime reviewedAt;
}