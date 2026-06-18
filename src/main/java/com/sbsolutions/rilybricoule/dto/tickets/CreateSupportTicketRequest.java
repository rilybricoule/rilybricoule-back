package com.sbsolutions.rilybricoule.dto.tickets;

import lombok.Data;

@Data
public class CreateSupportTicketRequest {
    private String subject;
    private String category;
    private String message;
    private String reservationId;
    private String reservationTitle;
    private Boolean litige;
}
