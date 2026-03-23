package com.sbsolutions.rilybricoule.services;

import com.sbsolutions.rilybricoule.dto.PrestataireSearchDTO;
import com.sbsolutions.rilybricoule.dto.ReservationDispatchResponseDTO;
import com.sbsolutions.rilybricoule.entity.DispatchStatus;
import com.sbsolutions.rilybricoule.entity.Prestataire;
import com.sbsolutions.rilybricoule.entity.Reservation;
import com.sbsolutions.rilybricoule.entity.ReservationDispatch;
import com.sbsolutions.rilybricoule.repository.PrestaireRepository;
import com.sbsolutions.rilybricoule.repository.ReservationDispatchRepository;
import com.sbsolutions.rilybricoule.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationDispatchService {

    private final ReservationRepository reservationRepository;
    private final ReservationDispatchRepository reservationDispatchRepository;
    private final PrestataireSearchService prestataireSearchService;
    private final PrestaireRepository prestaireRepository;
    private final INotificationService notificationService;

    @Value("${dispatch.retry.delay-minutes:1}")
    private long retryDelayMinutes;

    @Transactional
    public void createDispatchesForReservation(Reservation reservation) {
        createDispatchesForReservation(reservation, List.of());
    }
    @Transactional
    public void createDispatchesForReservation(Reservation reservation, List<Long> excludedPrestataireIds) {
        if (reservation.getClient() == null) {
            throw new IllegalArgumentException("La réservation doit avoir un client");
        }

        if (reservation.getStatus() != Reservation.ReservationStatus.PENDING_DISPATCH) {
            throw new IllegalStateException("La réservation doit être en statut PENDING_DISPATCH");
        }

        List<ReservationDispatch> newDispatches = buildDispatches(reservation, excludedPrestataireIds);

        if (newDispatches.isEmpty()) {
            reservation.setStatus(Reservation.ReservationStatus.DISPATCH_FAILED);
            reservationRepository.save(reservation);

            notificationService.notifyDispatchFailedToClient(
                    reservation.getClient(),
                    reservation
            );
            return;
        }

        reservationDispatchRepository.saveAll(newDispatches);

        for (ReservationDispatch dispatch : newDispatches) {
            notificationService.notifyDispatchToPrestataire(
                    dispatch.getPrestataire(),
                    dispatch.getReservation()
            );
        }
    }

    @Transactional
    public boolean acceptDispatch(Long reservationId, Long prestataireId) {
        ReservationDispatch dispatch = reservationDispatchRepository
                .findByReservation_IdAndPrestataire_Id(reservationId, prestataireId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Aucun dispatch trouvé pour la réservation " + reservationId +
                                " et le prestataire " + prestataireId
                ));

        if (dispatch.getStatus() != DispatchStatus.PENDING) {
            return false;
        }

        Reservation reservation = dispatch.getReservation();

        if (reservation.getStatus() != Reservation.ReservationStatus.PENDING_DISPATCH) {
            dispatch.setStatus(DispatchStatus.LOST);
            dispatch.setRespondedAt(LocalDateTime.now());
            reservationDispatchRepository.save(dispatch);
            return false;
        }

        dispatch.setStatus(DispatchStatus.ACCEPTED);
        dispatch.setRespondedAt(LocalDateTime.now());

        reservation.setPrestataire(dispatch.getPrestataire());
        reservation.setStatus(Reservation.ReservationStatus.PENDING_PAYMENT);

        reservationRepository.save(reservation);
        reservationDispatchRepository.save(dispatch);

        List<ReservationDispatch> otherDispatches =
                reservationDispatchRepository.findByReservation_IdAndStatus(
                        reservationId,
                        DispatchStatus.PENDING
                );

        for (ReservationDispatch other : otherDispatches) {
            if (!other.getPrestataire().getId().equals(prestataireId)) {
                other.setStatus(DispatchStatus.LOST);
                other.setRespondedAt(LocalDateTime.now());
            }
        }

        reservationDispatchRepository.saveAll(otherDispatches);

        notificationService.notifyDispatchAccepted(
                dispatch.getPrestataire(),
                reservation
        );

        return true;
    }

    @Transactional
    public void rejectDispatch(Long reservationId, Long prestataireId) {
        ReservationDispatch dispatch = reservationDispatchRepository
                .findByReservation_IdAndPrestataire_Id(reservationId, prestataireId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Aucun dispatch trouvé pour la réservation " + reservationId +
                                " et le prestataire " + prestataireId
                ));

        if (dispatch.getStatus() != DispatchStatus.PENDING) {
            throw new IllegalStateException("Ce dispatch n'est plus en attente de réponse");
        }

        dispatch.setStatus(DispatchStatus.REJECTED);
        dispatch.setRespondedAt(LocalDateTime.now());

        reservationDispatchRepository.save(dispatch);
    }

    @Transactional(readOnly = true)
    public List<ReservationDispatchResponseDTO> getDispatchesForPrestataire(Long prestataireId) {
        return reservationDispatchRepository.findByPrestataire_Id(prestataireId).stream()
                .map(this::mapToDto)
                .toList();
    }

    private ReservationDispatchResponseDTO mapToDto(ReservationDispatch dispatch) {
        Reservation reservation = dispatch.getReservation();
        Prestataire prestataire = dispatch.getPrestataire();

        return ReservationDispatchResponseDTO.builder()
                .dispatchId(dispatch.getId())
                .reservationId(reservation.getId())
                .description(reservation.getDescription())
                .category(reservation.getCategory())
                .subCategory(reservation.getSubCategory())
                .reservationDate(reservation.getReservationDate())
                .reservationTime(reservation.getReservationTime())
                .status(dispatch.getStatus())
                .sentAt(dispatch.getSentAt())
                .respondedAt(dispatch.getRespondedAt())
                .clientId(reservation.getClient() != null ? reservation.getClient().getId() : null)
                .clientFirstName(reservation.getClient() != null ? reservation.getClient().getFirstName() : null)
                .clientLastName(reservation.getClient() != null ? reservation.getClient().getLastName() : null)
                .prestataireId(prestataire != null ? prestataire.getId() : null)
                .prestataireName(prestataire != null ? prestataire.getName() : null)
                .build();
    }

    private List<ReservationDispatch> buildDispatches(
            Reservation reservation,
            List<Long> excludedPrestataireIds
    ) {
        double radiusKm = 20.0;

        List<PrestataireSearchDTO> rankedPrestataires = prestataireSearchService.searchForDispatch(
                reservation.getClient(),
                radiusKm,
                reservation.getCategory(),
                reservation.getSubCategory()
        );

        return rankedPrestataires.stream()
                .filter(dto -> excludedPrestataireIds == null || !excludedPrestataireIds.contains(dto.getId()))
                .filter(dto -> {
                    var prestataireOpt = prestaireRepository.findById(dto.getId());
                    return prestataireOpt.isPresent() && prestataireOpt.get().isAvailable();
                })
                .map(dto -> {
                    var prestataire = prestaireRepository.findById(dto.getId())
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Prestataire introuvable : " + dto.getId()
                            ));

                    return reservationDispatchRepository
                            .findByReservation_IdAndPrestataire_Id(reservation.getId(), prestataire.getId())
                            .map(existingDispatch -> {
                                existingDispatch.setStatus(DispatchStatus.PENDING);
                                existingDispatch.setSentAt(LocalDateTime.now());
                                existingDispatch.setRespondedAt(null);
                                return existingDispatch;
                            })
                            .orElseGet(() -> ReservationDispatch.builder()
                                    .reservation(reservation)
                                    .prestataire(prestataire)
                                    .status(DispatchStatus.PENDING)
                                    .sentAt(LocalDateTime.now())
                                    .build());
                })
                .toList();
    }

    @Transactional
    public void processDispatchRetry(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Réservation introuvable avec ID : " + reservationId
                ));

        if (reservation.getStatus() != Reservation.ReservationStatus.PENDING_DISPATCH) {
            return;
        }

        boolean hasAccepted = reservationDispatchRepository
                .existsByReservation_IdAndStatus(reservationId, DispatchStatus.ACCEPTED);

        if (hasAccepted) {
            return;
        }

        LocalDateTime lastSentAt = reservationDispatchRepository
                .findLastSentAtByReservationId(reservationId);

        if (lastSentAt == null) {
            return;
        }

        boolean retryDelayReached = !lastSentAt.plusMinutes(retryDelayMinutes).isAfter(LocalDateTime.now());

        if (!retryDelayReached) {
            return;
        }

        // CAS 1 : aucun retry n'a encore été fait -> on tente une seule relance
        if (!Boolean.TRUE.equals(reservation.getDispatchRetryDone())) {
            List<Long> alreadyContactedPrestataireIds =
                    reservationDispatchRepository.findPrestataireIdsByReservationId(reservationId);

            List<ReservationDispatch> newDispatches = buildDispatches(
                    reservation,
                    alreadyContactedPrestataireIds
            );

            reservation.setDispatchRetryDone(true);
            reservationRepository.save(reservation);

            if (!newDispatches.isEmpty()) {
                reservationDispatchRepository.saveAll(newDispatches);

                for (ReservationDispatch dispatch : newDispatches) {
                    notificationService.notifyDispatchToPrestataire(
                            dispatch.getPrestataire(),
                            dispatch.getReservation()
                    );
                }
            }

            return;
        }

        // CAS 2 : le retry a déjà été fait, et le délai est à nouveau dépassé
        List<ReservationDispatch> pendingDispatches =
                reservationDispatchRepository.findByReservation_IdAndStatus(
                        reservationId,
                        DispatchStatus.PENDING
                );

        if (!pendingDispatches.isEmpty()) {
            for (ReservationDispatch dispatch : pendingDispatches) {
                dispatch.setStatus(DispatchStatus.EXPIRED);
                dispatch.setRespondedAt(LocalDateTime.now());
            }
            reservationDispatchRepository.saveAll(pendingDispatches);
        }

        boolean hasAcceptedAfterExpire = reservationDispatchRepository
                .existsByReservation_IdAndStatus(reservationId, DispatchStatus.ACCEPTED);

        if (hasAcceptedAfterExpire) {
            return;
        }

        reservation.setStatus(Reservation.ReservationStatus.DISPATCH_FAILED);
        reservationRepository.save(reservation);

        notificationService.notifyDispatchFailedToClient(
                reservation.getClient(),
                reservation
        );
    }

    @Transactional
    public void redispatchByClient(Long reservationId, Long clientId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Réservation introuvable avec ID : " + reservationId
                ));

        if (reservation.getClient() == null || !reservation.getClient().getId().equals(clientId)) {
            throw new IllegalStateException("Cette réservation n'appartient pas à ce client");
        }

        if (reservation.getStatus() != Reservation.ReservationStatus.PENDING_PAYMENT) {
            throw new IllegalStateException(
                    "Le redispatch n'est autorisé que pour une réservation en PENDING_PAYMENT"
            );
        }

        if (reservation.getPrestataire() == null) {
            throw new IllegalStateException("Aucun prestataire sélectionné pour cette réservation");
        }

        Long previousPrestataireId = reservation.getPrestataire().getId();

        ReservationDispatch acceptedDispatch = reservationDispatchRepository
                .findByReservation_IdAndPrestataire_Id(reservationId, reservation.getPrestataire().getId())
                .orElseThrow(() -> new IllegalStateException(
                        "Dispatch accepté introuvable pour cette réservation"
                ));

        acceptedDispatch.setStatus(DispatchStatus.CLIENT_REJECTED);
        acceptedDispatch.setRespondedAt(LocalDateTime.now());
        reservationDispatchRepository.save(acceptedDispatch);

        List<Long> excludedPrestataireIds = new java.util.ArrayList<>(
                reservationDispatchRepository.findPrestataireIdsByReservationIdAndStatus(
                        reservationId,
                        DispatchStatus.CLIENT_REJECTED
                )
        );

        if (!excludedPrestataireIds.contains(previousPrestataireId)) {
            excludedPrestataireIds.add(previousPrestataireId);
        }

        reservation.setPrestataire(null);
        reservation.setStatus(Reservation.ReservationStatus.PENDING_DISPATCH);
        reservation.setDispatchRetryDone(false);
        reservationRepository.save(reservation);

        createDispatchesForReservation(reservation, excludedPrestataireIds);
    }
}