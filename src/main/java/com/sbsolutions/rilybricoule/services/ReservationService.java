package com.sbsolutions.rilybricoule.services;

import com.sbsolutions.rilybricoule.dto.CreateReservationRequest;
import com.sbsolutions.rilybricoule.dto.ReservationResponse;
import com.sbsolutions.rilybricoule.entity.Client;
import com.sbsolutions.rilybricoule.entity.Coupon;
import com.sbsolutions.rilybricoule.entity.Prestataire;
import com.sbsolutions.rilybricoule.entity.Reservation;
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
    private final INotificationService notificationService;


    /**
     * Create a new reservation with optional coupon application.
     * Business rules:
     * - Client and prestataire must exist
     * - Coupon is applied only if provided and valid (not expired, active)
     * - Discount is calculated and applied to total price
     */
    public ReservationResponse createReservation(CreateReservationRequest request) 
            throws IllegalArgumentException {
        
        // Validate client exists
        Optional<Client> clientOpt = clientRepository.findById(request.getClientId());
        if (clientOpt.isEmpty()) {
            throw new IllegalArgumentException("Client not found with ID: " + request.getClientId());
        }
        
        // Validate prestataire exists
        Optional<Prestataire> prestaireOpt = prestaireRepository.findById(request.getPrestaireId());
        if (prestaireOpt.isEmpty()) {
            throw new IllegalArgumentException("Prestataire not found with ID: " + request.getPrestaireId());
        }
        
        Client client = clientOpt.get();
        Prestataire prestataire = prestaireOpt.get();
        
        // Initialize base reservation
        Reservation reservation = Reservation.builder()
            .reservationDate(request.getReservationDate())
            .reservationTime(request.getReservationTime())
            .description(request.getDescription())
            .client(client)
            .prestataire(prestataire)
            .totalPrice(BigDecimal.ZERO)
            .discountAmount(BigDecimal.ZERO)
            .status(Reservation.ReservationStatus.PENDING)
            .build();
        
        // Apply coupon if provided
        if (request.getCouponId() != null) {
            Optional<Coupon> couponOpt = couponService.findById(request.getCouponId());
            
            if (couponOpt.isPresent()) {
                Coupon coupon = couponOpt.get();
                
                // Check if coupon is valid
                if (couponService.isValid(coupon)) {
                    reservation.setCoupon(coupon);
                    reservation.setDiscountAmount(coupon.getDiscountAmount());
                } else {
                    // Silently ignore expired or inactive coupons
                    // Business rule: only apply valid coupons
                }
            }
        }
        
        // Calculate total price based on available services from prestataire
        // For simplified logic, we set a base amount (in real scenario, this would aggregate service prices)
        reservation.setTotalPrice(calculateTotalPrice(prestataire, reservation.getDiscountAmount()));
        
        Reservation savedReservation = reservationRepository.save(reservation);
        notificationService.notifyReservation(client, savedReservation);
        return ReservationResponse.fromEntity(savedReservation);
    }

    /**
     * Create reservation and process payment.
     * On payment success mark reservation as CONFIRMED.
     * On payment failure a PaymentFailedException is thrown.
     */
    public ReservationResponse createReservationWithPayment(CreateReservationRequest request,
                                                           com.sbsolutions.rilybricoule.dto.PaymentRequestDTO paymentRequest)
            throws IllegalArgumentException {

        // Validate client exists
        java.util.Optional<Client> clientOpt = clientRepository.findById(request.getClientId());
        if (clientOpt.isEmpty()) {
            throw new IllegalArgumentException("Client not found with ID: " + request.getClientId());
        }
        
        // Validate prestataire exists
        java.util.Optional<Prestataire> prestaireOpt = prestaireRepository.findById(request.getPrestaireId());
        if (prestaireOpt.isEmpty()) {
            throw new IllegalArgumentException("Prestataire not found with ID: " + request.getPrestaireId());
        }
        
        Client client = clientOpt.get();
        Prestataire prestataire = prestaireOpt.get();

        Reservation reservation = Reservation.builder()
            .reservationDate(request.getReservationDate())
            .reservationTime(request.getReservationTime())
            .description(request.getDescription())
            .client(client)
            .prestataire(prestataire)
            .totalPrice(java.math.BigDecimal.ZERO)
            .discountAmount(java.math.BigDecimal.ZERO)
            .status(Reservation.ReservationStatus.PENDING)
            .build();

        // Apply coupon if provided
        if (request.getCouponId() != null) {
            java.util.Optional<Coupon> couponOpt = couponService.findById(request.getCouponId());
            if (couponOpt.isPresent()) {
                Coupon coupon = couponOpt.get();
                if (couponService.isValid(coupon)) {
                    reservation.setCoupon(coupon);
                    reservation.setDiscountAmount(coupon.getDiscountAmount());
                }
            }
        }

        reservation.setTotalPrice(calculateTotalPrice(prestataire, reservation.getDiscountAmount()));

        // Persist initial reservation as PENDING
        Reservation saved = reservationRepository.save(reservation);

        // Prepare payment request (amount should match reservation total)
        com.sbsolutions.rilybricoule.dto.PaymentRequestDTO paymentReq = paymentRequest;
        if (paymentReq == null) {
            throw new IllegalArgumentException("Payment information is required");
        }
        paymentReq.setAmount(saved.getTotalPrice());
        if (paymentReq.getCurrency() == null) {
            paymentReq.setCurrency("EUR");
        }

        // Process payment (may throw PaymentFailedException)
        com.sbsolutions.rilybricoule.dto.PaymentResponseDTO paymentResp = paymentService.processPayment(paymentReq);

        if (paymentResp != null && paymentResp.isSuccess()) {
            saved.setStatus(Reservation.ReservationStatus.CONFIRMED);
            Reservation confirmed = reservationRepository.save(saved);
            notificationService.notifyReservation(client, confirmed);
            return ReservationResponse.fromEntity(confirmed);
        } else {
            throw new com.sbsolutions.rilybricoule.exceptions.PaymentFailedException("Payment failed");
        }
    }
    
    /**
     * Get a reservation by ID.
     */
    public ReservationResponse getReservation(Long id) throws IllegalArgumentException {
        Optional<Reservation> reservation = reservationRepository.findById(id);
        if (reservation.isEmpty()) {
            throw new IllegalArgumentException("Reservation not found with ID: " + id);
        }
        return ReservationResponse.fromEntity(reservation.get());
    }
    
    /**
     * Get all reservations for a client.
     */
    public List<ReservationResponse> getClientReservations(Long clientId) {
        List<Reservation> reservations = reservationRepository.findByClientId(clientId);
        return reservations.stream()
            .map(ReservationResponse::fromEntity)
            .collect(Collectors.toList());
    }
    
    /**
     * Get all reservations for a prestataire.
     */
    public List<ReservationResponse> getPrestaireReservations(Long prestaireId) {
        List<Reservation> reservations = reservationRepository.findByPrestataireId(prestaireId);
        return reservations.stream()
            .map(ReservationResponse::fromEntity)
            .collect(Collectors.toList());
    }
    
    /**
     * Update reservation status.
     */
    public ReservationResponse updateReservationStatus(Long id, String status) 
            throws IllegalArgumentException {
        
        Optional<Reservation> reservationOpt = reservationRepository.findById(id);
        if (reservationOpt.isEmpty()) {
            throw new IllegalArgumentException("Reservation not found with ID: " + id);
        }
        
        try {
            Reservation.ReservationStatus newStatus = 
                Reservation.ReservationStatus.valueOf(status.toUpperCase());
            
            Reservation reservation = reservationOpt.get();
            reservation.setStatus(newStatus);
            
            Reservation updated = reservationRepository.save(reservation);
            return ReservationResponse.fromEntity(updated);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }
    }
    
    /**
     * Cancel a reservation by setting status to CANCELLED.
     */
    public ReservationResponse cancelReservation(Long id) throws IllegalArgumentException {
        return updateReservationStatus(id, "CANCELLED");
    }
    
    /**
     * Calculate the total price for a reservation.
     * This is a simplified implementation. In a real system, this would:
     * - Sum all services included in the reservation
     * - Apply discount amount if coupon is applied
     * - Apply additional business rules
     */
    private BigDecimal calculateTotalPrice(Prestataire prestataire, BigDecimal discount) {
        // Placeholder: In a real scenario, sum the prices of all services
        // For now, we return a base amount minus discount
        BigDecimal basePrice = new BigDecimal("100.00");
        
        if (discount != null && discount.compareTo(BigDecimal.ZERO) > 0) {
            return basePrice.subtract(discount);
        }
        
        return basePrice;
    }
    
    /**
     * Get reservations by status.
     */
    public List<ReservationResponse> getReservationsByStatus(Reservation.ReservationStatus status) {
        List<Reservation> reservations = reservationRepository.findByStatus(status);
        return reservations.stream()
            .map(ReservationResponse::fromEntity)
            .collect(Collectors.toList());
    }
    
    /**
     * Get reservations by date.
     */
    public List<ReservationResponse> getReservationsByDate(LocalDate date) {
        List<Reservation> reservations = reservationRepository.findByReservationDate(date);
        return reservations.stream()
            .map(ReservationResponse::fromEntity)
            .collect(Collectors.toList());
    }
}
