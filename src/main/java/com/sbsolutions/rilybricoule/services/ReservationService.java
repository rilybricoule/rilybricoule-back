package com.sbsolutions.rilybricoule.services;

import com.sbsolutions.rilybricoule.dto.CreateReservationRequest;
import com.sbsolutions.rilybricoule.dto.ReservationResponse;
import com.sbsolutions.rilybricoule.dto.PaymentRequestDTO;
import com.sbsolutions.rilybricoule.dto.PaymentResponseDTO;
import com.sbsolutions.rilybricoule.entity.Client;
import com.sbsolutions.rilybricoule.entity.Coupon;
import com.sbsolutions.rilybricoule.entity.Prestataire;
import com.sbsolutions.rilybricoule.entity.Reservation;
import com.sbsolutions.rilybricoule.exceptions.PaymentFailedException;
import com.sbsolutions.rilybricoule.repository.ClientRepository;
import com.sbsolutions.rilybricoule.repository.CouponRepository;
import com.sbsolutions.rilybricoule.repository.PrestaireRepository;
import com.sbsolutions.rilybricoule.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ClientRepository clientRepository;
    private final PrestaireRepository prestaireRepository;
    private final CouponRepository couponRepository;
    private final CouponService couponService;
    private final PaymentService paymentService;

    public ReservationResponse createReservation(CreateReservationRequest request) {
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new IllegalArgumentException("Client not found with ID: " + request.getClientId()));
        Prestataire prestataire = prestaireRepository.findById(request.getPrestaireId())
                .orElseThrow(() -> new IllegalArgumentException("Prestataire not found with ID: " + request.getPrestaireId()));

        Reservation reservation = Reservation.builder()
                .reservationDate(request.getReservationDate())
                .reservationTime(request.getReservationTime())
                .description(request.getDescription())
                .client(client)
                .prestataire(prestataire)
                .totalPrice(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .status(Reservation.ReservationStatus.PENDING_PAYMENT)
                .build();

        // Apply coupon if valid
        if (request.getCouponId() != null) {
            couponService.findById(request.getCouponId()).ifPresent(coupon -> {
                if (couponService.isValid(coupon)) {
                    reservation.setCoupon(coupon);
                    reservation.setDiscountAmount(coupon.getDiscountAmount());
                }
            });
        }

        reservation.setTotalPrice(calculateTotalPrice(prestataire, reservation.getDiscountAmount()));

        Reservation savedReservation = reservationRepository.save(reservation);
        return ReservationResponse.fromEntity(savedReservation);
    }

    public ReservationResponse createReservationWithPayment(CreateReservationRequest request, PaymentRequestDTO paymentRequest) {
        ReservationResponse reservationResp = createReservation(request);
        Reservation reservation = reservationRepository.findById(reservationResp.getId())
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));

        // Ensure payment request matches reservation
        if (paymentRequest == null) throw new IllegalArgumentException("Payment information is required");
        paymentRequest.setAmount(reservation.getTotalPrice());
        if (paymentRequest.getCurrency() == null) paymentRequest.setCurrency("EUR");

        PaymentResponseDTO paymentResponse = paymentService.processPayment(paymentRequest);
        if (paymentResponse.isSuccess()) {
            reservation.setStatus(Reservation.ReservationStatus.CONFIRMED);
            reservationRepository.save(reservation);
            return ReservationResponse.fromEntity(reservation);
        } else {
            throw new PaymentFailedException("Payment failed");
        }
    }

    public ReservationResponse getReservation(Long id) {
        return reservationRepository.findById(id)
                .map(ReservationResponse::fromEntity)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found with ID: " + id));
    }

    public List<ReservationResponse> getClientReservations(Long clientId) {
        return reservationRepository.findByClient_Id(clientId).stream()
                .map(ReservationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<ReservationResponse> getPrestaireReservations(Long prestataireId) {
        return reservationRepository.findByPrestataire_Id(prestataireId).stream()
                .map(ReservationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public ReservationResponse updateReservationStatus(Long id, String status) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found with ID: " + id));

        try {
            Reservation.ReservationStatus newStatus = Reservation.ReservationStatus.valueOf(status.toUpperCase());
            reservation.setStatus(newStatus);
            reservationRepository.save(reservation);
            return ReservationResponse.fromEntity(reservation);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }
    }

    public ReservationResponse cancelReservation(Long id) {
        return updateReservationStatus(id, "CANCELLED");
    }

    public List<ReservationResponse> getReservationsByStatus(Reservation.ReservationStatus status) {
        return reservationRepository.findByStatus(status).stream()
                .map(ReservationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<ReservationResponse> getReservationsByDate(LocalDate date) {
        return reservationRepository.findByReservationDate(date).stream()
                .map(ReservationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    private BigDecimal calculateTotalPrice(Prestataire prestataire, BigDecimal discount) {
        BigDecimal basePrice = new BigDecimal("100.00");
        return (discount != null && discount.compareTo(BigDecimal.ZERO) > 0)
                ? basePrice.subtract(discount)
                : basePrice;
    }
}
