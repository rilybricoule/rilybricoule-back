package com.sbsolutions.rilybricoule.dto.tickets;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Data
@Setter
public class ResolveTicketRequestDTO {
    private String action;
    private String target;
    private Long newPrestataireId;
    private String note;
}
