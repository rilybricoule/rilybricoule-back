package com.sbsolutions.rilybricoule.services;

import com.sbsolutions.rilybricoule.dto.PaymentRequestDTO;
import com.sbsolutions.rilybricoule.dto.PaymentResponseDTO;
import com.sbsolutions.rilybricoule.entity.*;
import com.sbsolutions.rilybricoule.exceptions.PaymentFailedException;
import com.sbsolutions.rilybricoule.repository.PaiementRepository;
import com.sbsolutions.rilybricoule.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
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
@DisplayName("PaymentService Unit Tests")
class PaymentServiceTest {

    @Mock
    private PaiementRepository paiementRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private PaymentService paymentService;

    private PaymentRequestDTO validRequest;
    private PaymentRequestDTO failingRequest;
    private Reservation reservation;

    @BeforeEach
    void setup() {
        reservation = Reservation.builder()
                .id(1L)
                .reservationDate(LocalDate.now().plusDays(3))
                .reservationTime(LocalTime.of(10, 0))
                .totalPrice(new BigDecimal("100.00"))
                .status(Reservation.ReservationStatus.PENDING_PAYMENT)
                .build();

        validRequest = PaymentRequestDTO.builder()
                .amount(new BigDecimal("100.00"))
                .paymentMethodToken("valid-token")
                .currency("EUR")
                .build();

        failingRequest = PaymentRequestDTO.builder()
                .amount(new BigDecimal("100.00"))
                .paymentMethodToken("fail")
                .currency("EUR")
                .build();
    }

    // ===============================
    // BASIC PAYMENT PROCESSING
    // ===============================

    @Test
    void shouldProcessPaymentSuccessfully() {
        PaymentResponseDTO response = paymentService.processPayment(validRequest);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertNotNull(response.getTransactionId());
    }

    @Test
    void shouldThrowExceptionWhenPaymentFails() {
        PaymentFailedException ex = assertThrows(
                PaymentFailedException.class,
                () -> paymentService.processPayment(failingRequest)
        );

        assertTrue(ex.getMessage().contains("declined"));
    }

    @Test
    void shouldValidatePaymentRequest() {
        assertThrows(IllegalArgumentException.class,
                () -> paymentService.processPayment(null));

        PaymentRequestDTO noAmount = PaymentRequestDTO.builder()
                .paymentMethodToken("valid-token")
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> paymentService.processPayment(noAmount));
    }

    // ===============================
    // PAYMENT FOR RESERVATION
    // ===============================

    @Test
    void shouldProcessPaymentForReservationSuccessfully() {
        when(reservationRepository.findById(1L))
                .thenReturn(Optional.of(reservation));

        when(paiementRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(reservationRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponseDTO response =
                paymentService.processPaymentForReservation(1L, validRequest, "CARD");

        assertTrue(response.isSuccess());

        InOrder inOrder = inOrder(paiementRepository, reservationRepository);
        inOrder.verify(paiementRepository).save(any());
        inOrder.verify(reservationRepository).save(any());
    }

    @Test
    void shouldUpdateReservationAndPaymentOnSuccess() {
        when(reservationRepository.findById(1L))
                .thenReturn(Optional.of(reservation));

        when(paiementRepository.save(any()))
                .thenAnswer(invocation -> {
                    Paiement p = invocation.getArgument(0);
                    assertEquals(Paiement.PaymentStatus.SUCCESS, p.getPaymentStatus());
                    assertNotNull(p.getTransactionId());
                    assertEquals("CARD", p.getPaymentMode());
                    assertNotNull(p.getPaymentDate());
                    return p;
                });

        when(reservationRepository.save(any()))
                .thenAnswer(invocation -> {
                    Reservation r = invocation.getArgument(0);
                    assertEquals(Reservation.ReservationStatus.CONFIRMED, r.getStatus());
                    assertNotNull(r.getPaiement());
                    return r;
                });

        paymentService.processPaymentForReservation(1L, validRequest, "CARD");
    }

    @Test
    void shouldThrowWhenReservationNotFound() {
        when(reservationRepository.findById(99L))
                .thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.processPaymentForReservation(99L, validRequest, "CARD")
        );

        assertTrue(ex.getMessage().contains("Reservation"));
        verify(paiementRepository, never()).save(any());
    }

    @Test
    void shouldNotPersistWhenPaymentFails() {
        when(reservationRepository.findById(1L))
                .thenReturn(Optional.of(reservation));

        assertThrows(PaymentFailedException.class,
                () -> paymentService.processPaymentForReservation(1L, failingRequest, "CARD"));

        verify(paiementRepository, never()).save(any());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void shouldStoreCorrectAmountAndMode() {
        BigDecimal customAmount = new BigDecimal("150.50");

        PaymentRequestDTO customRequest = PaymentRequestDTO.builder()
                .amount(customAmount)
                .paymentMethodToken("valid-token")
                .currency("EUR")
                .build();

        when(reservationRepository.findById(1L))
                .thenReturn(Optional.of(reservation));

        when(paiementRepository.save(any()))
                .thenAnswer(invocation -> {
                    Paiement p = invocation.getArgument(0);
                    assertEquals(customAmount, p.getAmount());
                    assertEquals("BANK_TRANSFER", p.getPaymentMode());
                    return p;
                });

        when(reservationRepository.save(any()))
                .thenReturn(reservation);

        paymentService.processPaymentForReservation(1L, customRequest, "BANK_TRANSFER");
    }
}
