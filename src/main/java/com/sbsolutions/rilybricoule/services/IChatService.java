package com.sbsolutions.rilybricoule.services;



import com.sbsolutions.rilybricoule.entity.Chat;
import com.sbsolutions.rilybricoule.entity.Prestataire;
import com.sbsolutions.rilybricoule.entity.Reservation;
import com.sbsolutions.rilybricoule.entity.User;
import org.springframework.stereotype.Service;

@Service
public interface IChatService {


    Chat startOrGetChat(Long senderId, Long receiverId, Long reservationId);

}
