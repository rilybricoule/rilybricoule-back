package com.sbsolutions.rilybricoule.dto.input;

import com.sbsolutions.rilybricoule.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageInputDto {
    private String Contenu;
    private Long chatId;

    private User sender;

}