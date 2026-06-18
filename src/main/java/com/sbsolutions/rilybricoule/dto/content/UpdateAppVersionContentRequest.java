package com.sbsolutions.rilybricoule.dto.content;



import com.sbsolutions.rilybricoule.dto.version.AppVersionDTO;
import lombok.Data;

import java.util.List;

@Data
public class UpdateAppVersionContentRequest {
    private List<AppVersionDTO.ChangelogEntryDTO> changelog;
    private String note;
}
