package com.sbsolutions.rilybricoule.services;

import com.sbsolutions.rilybricoule.entity.*;

public interface INotificationService {
    void notifyNewMessage(Client sender, Prestataire receiver, Message message);
    void notifyReservation(Client client, Reservation reservation);


}
