package com.sbsolutions.rilybricoule.dto.output;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatOutputDto {
    private Long id;
    private UserDto client;
    private UserDto prestataire;
    private List<MessageOutputDto> messages; // messages inside the chat
}

