package com.sbsolutions.rilybricoule.dto.output;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationOutputDto {
    private String contenu;
    private LocalDateTime date;

}
