package com.sbsolutions.rilybricoule.services;



import com.sbsolutions.rilybricoule.entity.Chat;
import com.sbsolutions.rilybricoule.entity.Prestataire;
import com.sbsolutions.rilybricoule.entity.Reservation;
import com.sbsolutions.rilybricoule.entity.User;

public interface IChatService {


    Chat startOrGetChat(User client, Prestataire prestataire, Reservation reservation);

}
