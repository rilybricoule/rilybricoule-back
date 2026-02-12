package com.sbsolutions.rilybricoule.services;

import com.sbsolutions.rilybricoule.dto.ReservationRequestDTO;
import com.sbsolutions.rilybricoule.entity.*;
import com.sbsolutions.rilybricoule.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReservationService Unit Tests")
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private PrestataireRepository prestataireRepository;

    @Mock
    private CouponRepository couponRepository;

    @InjectMocks
    private ReservationService reservationService;

    private Client client;
    private Prestataire prestataire;
    private Coupon coupon;
    private ReservationRequestDTO validRequest;

    @BeforeEach
    void setup() {

        client = Client.builder()
                .id(1L)
                .build();

        prestataire = Prestataire.builder()
                .id(1L)
                .hourlyRate(new BigDecimal("50.00"))
                .build();

        coupon = Coupon.builder()
                .id(1L)
                .code("PROMO10")
                .discountPercentage(10)
                .active(true)
                .build();

        validRequest = ReservationRequestDTO.builder()
                .clientId(1L)
                .prestataireId(1L)
                .date(LocalDate.now().plusDays(3))
                .time(LocalTime.of(10, 0))
                .durationInHours(2)
                .couponCode("PROMO10")
                .build();
    }

    // ===============================
    // SUCCESS SCENARIOS
    // ===============================

    @Test
    void shouldCreateReservationSuccessfullyWithCoupon() {

        mockValidClientPrestataire();
        when(couponRepository.findByCode("PROMO10"))
                .thenReturn(Optional.of(coupon));

        when(reservationRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Reservation result = reservationService.createReservation(validRequest);

        assertNotNull(result);
        assertEquals(Reservation.ReservationStatus.PENDING_PAYMENT, result.getStatus());
        assertEquals(new BigDecimal("90.00"), result.getTotalPrice());
        assertEquals(client, result.getClient());
        assertEquals(prestataire, result.getPrestataire());
    }

    @Test
    void shouldCreateReservationWithoutCoupon() {

        validRequest.setCouponCode(null);

        mockValidClientPrestataire();
        when(reservationRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Reservation result = reservationService.createReservation(validRequest);

        assertEquals(new BigDecimal("100.00"), result.getTotalPrice());
        verify(couponRepository, never()).findByCode(any());
    }

    // ===============================
    // BUSINESS VALIDATIONS
    // ===============================

    @Test
    void shouldThrowAndNotSaveWhenClientNotFound() {

        when(clientRepository.findById(1L))
                .thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> reservationService.createReservation(validRequest)
        );

        assertTrue(ex.getMessage().contains("Client"));
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void shouldThrowAndNotSaveWhenPrestataireNotFound() {

        when(clientRepository.findById(1L))
                .thenReturn(Optional.of(client));

        when(prestataireRepository.findById(1L))
                .thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> reservationService.createReservation(validRequest)
        );

        assertTrue(ex.getMessage().contains("Prestataire"));
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenDateIsInPast() {

        validRequest.setDate(LocalDate.now().minusDays(1));

        mockValidClientPrestataire();

        assertThrows(IllegalArgumentException.class,
                () -> reservationService.createReservation(validRequest));

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenDurationInvalid() {

        validRequest.setDurationInHours(0);

        mockValidClientPrestataire();

        assertThrows(IllegalArgumentException.class,
                () -> reservationService.createReservation(validRequest));
    }

    @Test
    void shouldThrowWhenCouponInvalidOrInactive() {

        mockValidClientPrestataire();

        when(couponRepository.findByCode("PROMO10"))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> reservationService.createReservation(validRequest));

        verify(reservationRepository, never()).save(any());
    }

    // ===============================
    // PRICE CALCULATION
    // ===============================

    @Test
    void shouldCalculatePriceCorrectlyWithoutDiscount() {

        validRequest.setCouponCode(null);
        validRequest.setDurationInHours(3);

        mockValidClientPrestataire();

        when(reservationRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Reservation result = reservationService.createReservation(validRequest);

        assertEquals(new BigDecimal("150.00"), result.getTotalPrice());
    }

    @Test
    void shouldApplyPercentageDiscountCorrectly() {

        validRequest.setDurationInHours(4); // 200€
        coupon.setDiscountPercentage(25);  // 25% → 150€

        mockValidClientPrestataire();
        when(couponRepository.findByCode("PROMO10"))
                .thenReturn(Optional.of(coupon));

        when(reservationRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Reservation result = reservationService.createReservation(validRequest);

        assertEquals(new BigDecimal("150.00"), result.getTotalPrice());
    }

    // ===============================
    // HELPER
    // ===============================

    private void mockValidClientPrestataire() {
        when(clientRepository.findById(1L))
                .thenReturn(Optional.of(client));

        when(prestataireRepository.findById(1L))
                .thenReturn(Optional.of(prestataire));
    }
}
