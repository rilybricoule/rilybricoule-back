package com.sbsolutions.rilybricoule.services;

import com.sbsolutions.rilybricoule.dto.admin.AdminClientDTO;
import com.sbsolutions.rilybricoule.entity.Client;
import com.sbsolutions.rilybricoule.repository.ClientRepository;
import com.sbsolutions.rilybricoule.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class AdminClientService {

    private final ClientRepository clientRepository;
    private final ReservationRepository reservationRepository;
    private final GeocodingService geocodingService;

    @Transactional(readOnly = true)
    public List<AdminClientDTO> getAll() {
        return clientRepository.findAll()
                .stream()
                .map(this::toDto)
                .sorted(Comparator.comparing(
                        AdminClientDTO::getCreatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminClientDTO getById(Long id) {
        return toDto(findClient(id));
    }

    @Transactional
    public AdminClientDTO activate(Long id) {
        Client client = findClient(id);
        client.setEnabled(true);

        return toDto(clientRepository.save(client));
    }

    @Transactional
    public AdminClientDTO deactivate(Long id) {
        Client client = findClient(id);
        client.setEnabled(false);

        return toDto(clientRepository.save(client));
    }

    private Client findClient(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client introuvable avec ID " + id));
    }

    private AdminClientDTO toDto(Client client) {
        long reservationsCount = reservationRepository.countByClient_Id(client.getId());

        long cancelledReservationsCount =
                reservationRepository.countByClient_IdAndStatus(
                        client.getId(),
                        com.sbsolutions.rilybricoule.entity.Reservation.ReservationStatus.CANCELLED
                );

        LocalDateTime lastActivityAt =
                reservationRepository.findTopByClient_IdOrderByReservationDateDesc(client.getId())
                        .map(reservation -> reservation.getReservationDate().atStartOfDay())
                        .orElse(null);

        return AdminClientDTO.builder()
                .id(client.getId())
                .firstName(client.getFirstName())
                .lastName(client.getLastName())
                .email(client.getEmail())
                .phone(client.getPhone())
                .address(client.getAddress())
                .active(client.isEnabled())
                .createdAt(client.getCreatedAt())
                .updatedAt(client.getUpdatedAt())
                .reservationsCount(reservationsCount)
                .cancelledReservationsCount(cancelledReservationsCount)
                .lastActivityAt(lastActivityAt)
                .build();
    }

    @Transactional
    public AdminClientDTO create(AdminClientDTO request) {
        Client client = Client.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .enabled(request.getActive() == null || request.getActive())
                .build();

        setLatLngIfPossible(client);

        return toDto(clientRepository.save(client));
    }

    @Transactional
    public AdminClientDTO update(Long id, AdminClientDTO request) {
        Client client = findClient(id);

        client.setFirstName(request.getFirstName());
        client.setLastName(request.getLastName());
        client.setEmail(request.getEmail());
        client.setPhone(request.getPhone());
        client.setAddress(request.getAddress());

        if (request.getActive() != null) {
            client.setEnabled(request.getActive());
        }

        setLatLngIfPossible(client);

        return toDto(clientRepository.save(client));
    }

    @Transactional
    public void delete(Long id) {
        if (!clientRepository.existsById(id)) {
            throw new RuntimeException("Client introuvable avec ID " + id);
        }

        clientRepository.deleteById(id);
    }

    private void setLatLngIfPossible(Client client) {
        if (client.getAddress() != null && !client.getAddress().isBlank()) {
            Double[] coords = geocodingService.getCoordinates(client.getAddress());

            if (coords != null) {
                client.setLatitude(coords[0]);
                client.setLongitude(coords[1]);
            }
        }
    }
}