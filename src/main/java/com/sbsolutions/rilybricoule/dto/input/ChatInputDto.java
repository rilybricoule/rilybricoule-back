package com.sbsolutions.rilybricoule.dto.input;

import com.sbsolutions.rilybricoule.entity.Prestataire;
import com.sbsolutions.rilybricoule.entity.Reservation;
import com.sbsolutions.rilybricoule.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatInputDto {


    private Long senderId;
    private Long receiverId;
    private Long reservationId;
}
