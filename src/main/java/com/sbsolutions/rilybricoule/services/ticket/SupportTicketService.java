package com.sbsolutions.rilybricoule.services.ticket;

import com.sbsolutions.rilybricoule.dto.tickets.AdminSupportTicketDTO;
import com.sbsolutions.rilybricoule.dto.tickets.CreateSupportTicketRequest;
import com.sbsolutions.rilybricoule.entity.*;
import com.sbsolutions.rilybricoule.repository.SupportTicketRepository;
import com.sbsolutions.rilybricoule.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SupportTicketService {

    private final SupportTicketRepository supportTicketRepository;
    private final UserRepository userRepository;

    public AdminSupportTicketDTO createTicket(CreateSupportTicketRequest request) {
        validateCreateRequest(request);

        User user = getAuthenticatedUser();
        String category = normalizeCategory(request.getCategory(), Boolean.TRUE.equals(request.getLitige()));

        SupportTicket ticket = SupportTicket.builder()
                .subject(request.getSubject().trim())
                .category(category)
                .status("OPEN")
                .litige(Boolean.TRUE.equals(request.getLitige()) || "DISPUTE".equals(category))
                .fromType(resolveFromType(user))
                .fromId(String.valueOf(user.getId()))
                .fromName(resolveFromName(user))
                .fromRole(resolveFromRole(user))
                .reservationId(trimToNull(request.getReservationId()))
                .reservationTitle(trimToNull(request.getReservationTitle()))
                .build();

        SupportTicketMessage firstMessage = SupportTicketMessage.builder()
                .ticket(ticket)
                .senderType(ticket.getFromType())
                .senderName(ticket.getFromName())
                .content(request.getMessage().trim())
                .build();

        ticket.getMessages().add(firstMessage);

        return toDto(supportTicketRepository.save(ticket));
    }

    public List<AdminSupportTicketDTO> getMyTickets() {
        User user = getAuthenticatedUser();
        String userId = String.valueOf(user.getId());

        return supportTicketRepository.findAll()
                .stream()
                .filter(ticket -> userId.equals(ticket.getFromId()))
                .sorted(Comparator.comparing(SupportTicket::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::toDto)
                .toList();
    }

    public AdminSupportTicketDTO getMyTicketById(Long id) {
        User user = getAuthenticatedUser();
        SupportTicket ticket = getTicketOrThrow(id);
        assertOwnership(ticket, user);
        return toDto(ticket);
    }

    public AdminSupportTicketDTO replyToMyTicket(Long id, String message) {
        User user = getAuthenticatedUser();
        SupportTicket ticket = getTicketOrThrow(id);
        assertOwnership(ticket, user);

        return reply(id, resolveFromType(user), resolveFromName(user), message);
    }

    public AdminSupportTicketDTO closeMyTicket(Long id) {
        User user = getAuthenticatedUser();
        SupportTicket ticket = getTicketOrThrow(id);
        assertOwnership(ticket, user);

        if ("RESOLVED".equalsIgnoreCase(ticket.getStatus()) || "CLOSED".equalsIgnoreCase(ticket.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ticket is already closed or resolved");
        }

        ticket.setStatus("CLOSED");
        return toDto(supportTicketRepository.save(ticket));
    }
    private String resolveFromRole(User user) {
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            return null;
        }

        return user.getRoles().stream()
                .map(role -> role.getRoleName().name())
                .findFirst()
                .orElse(null);
    }




    public AdminSupportTicketDTO reply(Long id, String senderType, String senderName, String message) {
        if (message == null || message.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message is required");
        }

        SupportTicket ticket = getTicketOrThrow(id);

        SupportTicketMessage reply = SupportTicketMessage.builder()
                .ticket(ticket)
                .senderType(senderType)
                .senderName(senderName)
                .content(message.trim())
                .build();

        ticket.getMessages().add(reply);

        if ("OPEN".equalsIgnoreCase(ticket.getStatus())) {
            ticket.setStatus("IN_PROGRESS");
        }

        return toDto(supportTicketRepository.save(ticket));
    }


    public AdminSupportTicketDTO updateStatus(Long id, String status) {
        SupportTicket ticket = getTicketOrThrow(id);
        ticket.setStatus(normalizeStatus(status));
        return toDto(supportTicketRepository.save(ticket));
    }

    public AdminSupportTicketDTO updateCategory(Long id, String category) {
        SupportTicket ticket = getTicketOrThrow(id);
        ticket.setCategory(normalizeCategory(category, false));
        if ("DISPUTE".equals(ticket.getCategory())) {
            ticket.setLitige(true);
        }
        return toDto(supportTicketRepository.save(ticket));
    }

    public AdminSupportTicketDTO updateLitige(Long id, boolean isLitige) {
        SupportTicket ticket = getTicketOrThrow(id);
        ticket.setLitige(isLitige);
        if (isLitige) {
            ticket.setCategory("DISPUTE");
        }
        return toDto(supportTicketRepository.save(ticket));
    }

    public SupportTicket getTicketOrThrow(Long id) {
        return supportTicketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));
    }

    public String normalizeStatus(String status) {
        String value = status == null ? "" : status.trim().toUpperCase();
        return switch (value) {
            case "OPEN", "IN_PROGRESS", "RESOLVED", "CLOSED" -> value;
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid ticket status");
        };
    }

    public String normalizeCategory(String category, boolean litige) {
        if (litige) {
            return "DISPUTE";
        }

        String value = category == null ? "" : category.trim().toUpperCase();
        return switch (value) {
            case "INCIDENT", "GENERAL_QUESTION", "REFUND_REQUEST", "DISPUTE" -> value;
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid ticket category");
        };
    }

    public AdminSupportTicketDTO toDto(SupportTicket ticket) {
        return AdminSupportTicketDTO.builder()
                .id(ticket.getId())
                .subject(ticket.getSubject())
                .category(ticket.getCategory())
                .status(ticket.getStatus())
                .litige(ticket.isLitige())
                .fromType(ticket.getFromType())
                .fromId(ticket.getFromId())
                .fromName(ticket.getFromName())
                .reservationId(ticket.getReservationId())
                .reservationTitle(ticket.getReservationTitle())
                .adminNote(ticket.getAdminNote())
                .resolutionNote(ticket.getResolutionNote())
                .resolutionAction(ticket.getResolutionAction())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .messages(ticket.getMessages().stream()
                        .sorted(Comparator.comparing(
                                SupportTicketMessage::getCreatedAt,
                                Comparator.nullsLast(Comparator.naturalOrder())
                        ))
                        .map(message -> AdminSupportTicketDTO.MessageDTO.builder()
                                .senderType(message.getSenderType())
                                .senderName(message.getSenderName())
                                .content(message.getContent())
                                .createdAt(message.getCreatedAt())
                                .build())
                        .toList())
                .build();
    }

    private void validateCreateRequest(CreateSupportTicketRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
        }
        if (request.getSubject() == null || request.getSubject().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Subject is required");
        }
        if (request.getMessage() == null || request.getMessage().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message is required");
        }
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));
    }

    private void assertOwnership(SupportTicket ticket, User user) {
        if (!String.valueOf(user.getId()).equals(ticket.getFromId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to this ticket");
        }
    }

    private String resolveFromType(User user) {
        if (user.getRoles() != null) {
            boolean isClient = user.getRoles().stream()
                    .anyMatch(role -> role.getRoleName() == RoleName.ROLE_CLIENT);
            if (isClient) {
                return "CLIENT";
            }

            boolean isPrestataire = user.getRoles().stream()
                    .anyMatch(role -> role.getRoleName() == RoleName.ROLE_PRESTATAIRE);
            if (isPrestataire) {
                return "PRESTATAIRE";
            }

            boolean isAdmin = user.getRoles().stream()
                    .anyMatch(role ->
                            role.getRoleName() == RoleName.ROLE_ADMIN ||
                                    role.getRoleName() == RoleName.ROLE_SUPER_ADMIN ||
                                    role.getRoleName() == RoleName.ROLE_MODERATEUR ||
                                    role.getRoleName() == RoleName.ROLE_SUPPORT
                    );
            if (isAdmin) {
                return "ADMIN";
            }
        }

        return "USER";
    }


    private String resolveFromName(User user) {
        if (user instanceof Prestataire prestataire) {
            if (prestataire.getBusinessName() != null && !prestataire.getBusinessName().isBlank()) {
                return prestataire.getBusinessName().trim();
            }
            if (prestataire.getName() != null && !prestataire.getName().isBlank()) {
                return prestataire.getName().trim();
            }
        }

        String fullName = ((user.getFirstName() == null ? "" : user.getFirstName().trim()) + " "
                + (user.getLastName() == null ? "" : user.getLastName().trim())).trim();

        if (!fullName.isBlank()) {
            return fullName;
        }

        return user.getEmail();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
