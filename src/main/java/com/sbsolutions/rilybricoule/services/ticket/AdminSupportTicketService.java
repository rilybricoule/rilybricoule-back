package com.sbsolutions.rilybricoule.services.ticket;

import com.sbsolutions.rilybricoule.dto.input.NotificationInputDto;
import com.sbsolutions.rilybricoule.dto.tickets.AdminSupportTicketDTO;
import com.sbsolutions.rilybricoule.entity.*;
import com.sbsolutions.rilybricoule.repository.PrestaireRepository;
import com.sbsolutions.rilybricoule.repository.ReservationRepository;
import com.sbsolutions.rilybricoule.repository.SupportTicketRepository;
import com.sbsolutions.rilybricoule.services.NotificationService;
import com.sbsolutions.rilybricoule.services.PaymentService;
import com.sbsolutions.rilybricoule.services.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminSupportTicketService {

    private final SupportTicketRepository supportTicketRepository;
    private final SupportTicketService supportTicketService;
    private final PaymentService paymentService;
    private final ReservationRepository reservationRepository;
    private final NotificationService notificationService;
    private final PrestaireRepository prestaireRepository;


    public List<AdminSupportTicketDTO> getTickets() {
        return supportTicketRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(SupportTicket::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(supportTicketService::toDto)
                .toList();
    }

    public AdminSupportTicketDTO reply(Long id, String message) {
        return supportTicketService.reply(id, "ADMIN", "Admin", message);
    }

    public AdminSupportTicketDTO updateStatus(Long id, String status) {
        return supportTicketService.updateStatus(id, status);
    }

    public AdminSupportTicketDTO updateCategory(Long id, String category) {
        return supportTicketService.updateCategory(id, category);
    }

    public AdminSupportTicketDTO updateLitige(Long id, boolean isLitige) {
        return supportTicketService.updateLitige(id, isLitige);
    }

    public AdminSupportTicketDTO updateAdminNote(Long id, String note) {
        SupportTicket ticket = supportTicketService.getTicketOrThrow(id);
        ticket.setAdminNote(note == null ? null : note.trim());
        return supportTicketService.toDto(supportTicketRepository.save(ticket));
    }

    private void applyRefundClient(SupportTicket ticket) {
        Long reservationId = parseReservationId(ticket);
        paymentService.refundReservation(reservationId);
    }

    private Reservation getReservationFromTicket(SupportTicket ticket) {
        Long reservationId = parseReservationId(ticket);

        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found"));
    }


    private void applyReassignProvider(SupportTicket ticket, Long newPrestataireId) {
        if (newPrestataireId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New provider is required");
        }

        Reservation reservation = getReservationFromTicket(ticket);
        Prestataire oldPrestataire = reservation.getPrestataire();

        Prestataire newPrestataire = prestaireRepository.findById(newPrestataireId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "New provider not found"));

        if (oldPrestataire != null && oldPrestataire.getId().equals(newPrestataire.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New provider must be different from current provider");
        }

        reservation.setPrestataire(newPrestataire);
        reservationRepository.save(reservation);

        if (oldPrestataire != null) {
            NotificationInputDto oldProviderNotification = new NotificationInputDto();
            oldProviderNotification.setReceiverId(oldPrestataire.getId());
            oldProviderNotification.setContenu("La reservation #" + reservation.getId() + " vous a ete retiree suite a un litige.");
            oldProviderNotification.setType(NotificationType.WARNING);
            oldProviderNotification.setDate(LocalDateTime.now());
            notificationService.createNotification(oldProviderNotification);
        }

        NotificationInputDto newProviderNotification = new NotificationInputDto();
        newProviderNotification.setReceiverId(newPrestataire.getId());
        newProviderNotification.setContenu("Une reservation #" + reservation.getId() + " vous a ete reassignee suite a un litige.");
        newProviderNotification.setType(NotificationType.RESERVATION);
        newProviderNotification.setDate(LocalDateTime.now());
        notificationService.createNotification(newProviderNotification);

        Client client = reservation.getClient();
        if (client != null) {
            NotificationInputDto clientNotification = new NotificationInputDto();
            clientNotification.setReceiverId(client.getId());
            clientNotification.setContenu("Votre reservation #" + reservation.getId() + " a ete reassignee a un nouveau prestataire.");
            clientNotification.setType(NotificationType.RESERVATION);
            clientNotification.setDate(LocalDateTime.now());
            notificationService.createNotification(clientNotification);
        }
    }


    private void applyProviderWarning(SupportTicket ticket) {
        Reservation reservation = getReservationFromTicket(ticket);
        Prestataire prestataire = reservation.getPrestataire();

        NotificationInputDto inputDto = new NotificationInputDto();
        inputDto.setReceiverId(prestataire.getId());
        inputDto.setContenu("Un avertissement vous a ete adresse suite a un litige sur la reservation #" + reservation.getId());
        inputDto.setType(NotificationType.WARNING);
        inputDto.setDate(LocalDateTime.now());
        notificationService.createNotification(inputDto);
    }


    private void applyClientWarning(SupportTicket ticket) {
        Reservation reservation = getReservationFromTicket(ticket);
        Client client = reservation.getClient();

        NotificationInputDto inputDto = new NotificationInputDto();
        inputDto.setReceiverId(client.getId());
        inputDto.setContenu("Un avertissement vous a ete adresse suite a un litige sur la reservation #" + reservation.getId());
        inputDto.setType(NotificationType.WARNING);
        inputDto.setDate(LocalDateTime.now());

        notificationService.createNotification(inputDto);
    }


    private void applyNoAction(SupportTicket ticket) {

    }



    private Long parseReservationId(SupportTicket ticket) {
        if (ticket.getReservationId() == null || ticket.getReservationId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reservation is required for this resolution action");
        }

        try {
            return Long.valueOf(ticket.getReservationId());
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid reservation id");
        }
    }

    public AdminSupportTicketDTO resolve(Long id, String action, String note, Long newPrestataireId) {
        if (action == null || action.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Resolution action is required");
        }

        SupportTicket ticket = supportTicketService.getTicketOrThrow(id);
        String normalizedAction = action.trim().toUpperCase();

        switch (normalizedAction) {
            case "REFUND_CLIENT" -> applyRefundClient(ticket);

            case "REASSIGN_PROVIDER" -> applyReassignProvider(ticket, newPrestataireId);
            case "PROVIDER_WARNING" -> applyProviderWarning(ticket);
            case "CLIENT_WARNING" -> applyClientWarning(ticket);
            case "NO_ACTION" -> applyNoAction(ticket);
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid resolution action");
        }


        ticket.setResolutionAction(normalizedAction);
        ticket.setResolutionNote(note == null ? null : note.trim());
        ticket.setStatus("RESOLVED");

        return supportTicketService.toDto(supportTicketRepository.save(ticket));
    }

}
