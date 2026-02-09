package com.sbsolutions.rilybricoule.dto.output;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageOutputDto {
    private Long id;
    private String contenu;
    private LocalDateTime date;
    private UserDto auteur; // minimal info of the author
}