package com.sbsolutions.rilybricoule.dto.input;

import com.sbsolutions.rilybricoule.entity.ChatType;
import lombok.Data;

@Data
public class OpenAdminChatRequest {
    private Long adminId;
    private Long participantId;
    private ChatType type;
}