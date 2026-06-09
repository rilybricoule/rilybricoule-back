package com.sbsolutions.rilybricoule.services;

import com.sbsolutions.rilybricoule.entity.Reservation;
import com.sbsolutions.rilybricoule.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DispatchRetryScheduler {

    private final ReservationRepository reservationRepository;
    private final ReservationDispatchService reservationDispatchService;

    @Scheduled(fixedDelayString = "${dispatch.retry.scheduler-delay-ms:60000}")
    public void processPendingDispatchReservations() {
        List<Reservation> pendingReservations =
                reservationRepository.findByStatus(Reservation.ReservationStatus.PENDING_DISPATCH);

        for (Reservation reservation : pendingReservations) {
            try {
                reservationDispatchService.processDispatchRetry(reservation.getId());
            } catch (Exception e) {
                System.err.println(
                        "[DISPATCH-SCHEDULER] Erreur lors du traitement de la réservation "
                                + reservation.getId() + " : " + e.getMessage()
                );
            }
        }
    }
}