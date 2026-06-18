package com.sbsolutions.rilybricoule.dto.version;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AppVersionDTO {
    private Long id;
    private String platform;
    private String version;
    private String buildNumber;
    private LocalDate releaseDate;
    private LocalDateTime lastUpdatedAt;
    private String status;
    private boolean forceUpdate;
    private String minSupportedVersion;
    private List<ChangelogEntryDTO> changelog;
    private String note;

    @Data
    public static class ChangelogEntryDTO {
        private String type;
        private String text;
    }
}