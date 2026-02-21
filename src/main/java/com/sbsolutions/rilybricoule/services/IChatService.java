package com.sbsolutions.rilybricoule.services;



import com.sbsolutions.rilybricoule.entity.Chat;
import org.springframework.stereotype.Service;

@Service
public interface IChatService {

    Chat startOrGetChat(Long clientId, Long prestataireId, Long reservationId);

}
