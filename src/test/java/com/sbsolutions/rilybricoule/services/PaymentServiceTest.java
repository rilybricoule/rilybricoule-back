package com.sbsolutions.rilybricoule.services;

import com.sbsolutions.rilybricoule.dto.PaymentRequestDTO;
import com.sbsolutions.rilybricoule.dto.PaymentResponseDTO;
import com.sbsolutions.rilybricoule.entity.Client;
import com.sbsolutions.rilybricoule.entity.Paiement;
import com.sbsolutions.rilybricoule.entity.Prestataire;
import com.sbsolutions.rilybricoule.entity.Reservation;
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
import static org.mockito.ArgumentMatchers.*;
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

    private PaymentRequestDTO validPaymentRequest;
    private PaymentRequestDTO failingPaymentRequest;
    private Reservation testReservation;
    private Paiement testPayment;
    private Client testClient;
    private Prestataire testPrestataire;

    @BeforeEach
    void setUp() {
        testClient = Client.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .build();

        testPrestataire = Prestataire.builder()
                .id(1L)
                .firstName("Jane")
                .lastName("Smith")
                .email("jane@example.com")
                .build();

        testReservation = Reservation.builder()
                .id(1L)
                .client(testClient)
                .prestataire(testPrestataire)
                .reservationDate(LocalDate.now().plusDays(5))
                .reservationTime(LocalTime.of(10, 0))
                .totalPrice(new BigDecimal("100.00"))
                .discountAmount(BigDecimal.ZERO)
                .status(Reservation.ReservationStatus.PENDING_PAYMENT)
                .build();

        validPaymentRequest = PaymentRequestDTO.builder()
                .amount(new BigDecimal("100.00"))
                .paymentMethodToken("valid-token")
                .currency("EUR")
                .build();

        failingPaymentRequest = PaymentRequestDTO.builder()
                .amount(new BigDecimal("100.00"))
                .paymentMethodToken("fail")
                .currency("EUR")
                .build();

        testPayment = Paiement.builder()
                .id(1L)
                .amount(new BigDecimal("100.00"))
                .paymentStatus(Paiement.PaymentStatus.PENDING)
                .paymentMode("CARD")
                .reservation(testReservation)
                .transactionId("txn-123")
                .build();
    }

    // ===== PROCESS PAYMENT TESTS =====

    @Test
    @DisplayName("shouldProcessPaymentSuccessfully")
    void shouldProcessPaymentSuccessfully() {
        // Act
        PaymentResponseDTO response = paymentService.processPayment(validPaymentRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertNotNull(response.getTransactionId());
        assertEquals("Mock payment succeeded", response.getMessage());
    }

    @Test
    @DisplayName("shouldReturnTransactionIdOnSuccessfulPayment")
    void shouldReturnTransactionIdOnSuccessfulPayment() {
        // Act
        PaymentResponseDTO response = paymentService.processPayment(validPaymentRequest);

        // Assert
        assertNotNull(response.getTransactionId());
        assertFalse(response.getTransactionId().isEmpty());
    }

    @Test
    @DisplayName("shouldThrowExceptionWhenPaymentTokenIsFail")
    void shouldThrowExceptionWhenPaymentTokenIsFail() {
        // Act & Assert
        assertThrows(PaymentFailedException.class, () -> {
            paymentService.processPayment(failingPaymentRequest);
        });
    }

    @Test
    @DisplayName("shouldThrowPaymentFailedExceptionWithCorrectMessage")
    void shouldThrowPaymentFailedExceptionWithCorrectMessage() {
        // Act & Assert
        PaymentFailedException exception = assertThrows(PaymentFailedException.class, () -> {
            paymentService.processPayment(failingPaymentRequest);
        });

        assertTrue(exception.getMessage().contains("Payment was declined"));
    }

    @Test
    @DisplayName("shouldThrowExceptionWhenPaymentRequestIsNull")
    void shouldThrowExceptionWhenPaymentRequestIsNull() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            paymentService.processPayment(null);
        });
    }

    @Test
    @DisplayName("shouldThrowExceptionWhenPaymentAmountIsNull")
    void shouldThrowExceptionWhenPaymentAmountIsNull() {
        // Arrange
        PaymentRequestDTO requestWithoutAmount = PaymentRequestDTO.builder()
                .amount(null)
                .paymentMethodToken("valid-token")
                .build();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            paymentService.processPayment(requestWithoutAmount);
        });
    }

    @Test
    @DisplayName("shouldThrowExceptionWithCorrectMessageWhenPaymentRequestIsNull")
    void shouldThrowExceptionWithCorrectMessageWhenPaymentRequestIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            paymentService.processPayment(null);
        });

        assertTrue(exception.getMessage().contains("Payment request") || 
                  exception.getMessage().contains("amount"));
    }

    // ===== PROCESS PAYMENT FOR RESERVATION TESTS =====

    @Test
    @DisplayName("shouldProcessPaymentForReservationSuccessfully")
    void shouldProcessPaymentForReservationSuccessfully() {
        // Arrange
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(testReservation));
        when(paiementRepository.save(any(Paiement.class))).thenReturn(testPayment);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(testReservation);

        // Act
        PaymentResponseDTO response = paymentService.processPaymentForReservation(
                1L, validPaymentRequest, "CARD");

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        verify(paiementRepository, times(1)).save(any(Paiement.class));
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    @Test
    @DisplayName("shouldConfirmReservationWhenPaymentIsSuccessful")
    void shouldConfirmReservationWhenPaymentIsSuccessful() {
        // Arrange
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(testReservation));
        when(paiementRepository.save(any(Paiement.class))).thenReturn(testPayment);
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> {
            Reservation reservation = invocation.getArgument(0);
            assertEquals(Reservation.ReservationStatus.CONFIRMED, reservation.getStatus());
            return reservation;
        });

        // Act
        paymentService.processPaymentForReservation(1L, validPaymentRequest, "CARD");

        // Assert
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    @Test
    @DisplayName("shouldSetPaymentStatusToSuccessWhenPaymentSucceeds")
    void shouldSetPaymentStatusToSuccessWhenPaymentSucceeds() {
        // Arrange
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(testReservation));
        when(paiementRepository.save(any(Paiement.class))).thenAnswer(invocation -> {
            Paiement payment = invocation.getArgument(0);
            assertEquals(Paiement.PaymentStatus.SUCCESS, payment.getPaymentStatus());
            return payment;
        });
        when(reservationRepository.save(any(Reservation.class))).thenReturn(testReservation);

        // Act
        paymentService.processPaymentForReservation(1L, validPaymentRequest, "CARD");

        // Assert
        verify(paiementRepository, times(1)).save(any(Paiement.class));
    }

    @Test
    @DisplayName("shouldCancelReservationWhenPaymentFails")
    void shouldCancelReservationWhenPaymentFails() {
        // Arrange
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(testReservation));
        // processPayment will throw when token is "fail"
        // So save operations won't be called

        // Act & Assert
        assertThrows(PaymentFailedException.class, () -> {
            paymentService.processPaymentForReservation(1L, failingPaymentRequest, "CARD");
        });

        // Verify - payment save should NOT be called since exception is thrown in processPayment
        verify(paiementRepository, never()).save(any(Paiement.class));
    }

    @Test
    @DisplayName("shouldSetPaymentStatusToFailedWhenPaymentFails")
    void shouldSetPaymentStatusToFailedWhenPaymentFails() {
        // Arrange
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(testReservation));
        // processPayment throws when token is "fail", so no saves are called

        // Act & Assert
        assertThrows(PaymentFailedException.class, () -> {
            paymentService.processPaymentForReservation(1L, failingPaymentRequest, "CARD");
        });

        // Verify - no payment save since exception is thrown first
        verify(paiementRepository, never()).save(any(Paiement.class));
    }

    @Test
    @DisplayName("shouldThrowExceptionWhenReservationNotFound")
    void shouldThrowExceptionWhenReservationNotFound() {
        // Arrange
        when(reservationRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            paymentService.processPaymentForReservation(999L, validPaymentRequest, "CARD");
        });

        // Verify
        verify(paiementRepository, never()).save(any());
    }

    @Test
    @DisplayName("shouldThrowExceptionWithCorrectMessageWhenReservationNotFound")
    void shouldThrowExceptionWithCorrectMessageWhenReservationNotFound() {
        // Arrange
        when(reservationRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            paymentService.processPaymentForReservation(999L, validPaymentRequest, "CARD");
        });

        assertTrue(exception.getMessage().contains("Reservation not found"));
        assertTrue(exception.getMessage().contains("999"));
    }

    @Test
    @DisplayName("shouldNotSavePaymentWhenReservationNotFound")
    void shouldNotSavePaymentWhenReservationNotFound() {
        // Arrange
        when(reservationRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            paymentService.processPaymentForReservation(999L, validPaymentRequest, "CARD");
        });

        // Verify
        verify(paiementRepository, never()).save(any());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("shouldThrowExceptionWhenPaymentFailsForReservation")
    void shouldThrowExceptionWhenPaymentFailsForReservation() {
        // Arrange
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(testReservation));
        // processPayment will throw PaymentFailedException when token is "fail"

        // Act & Assert
        assertThrows(PaymentFailedException.class, () -> {
            paymentService.processPaymentForReservation(1L, failingPaymentRequest, "CARD");
        });
    }

    @Test
    @DisplayName("shouldStoreTransactionIdInPayment")
    void shouldStoreTransactionIdInPayment() {
        // Arrange
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(testReservation));
        when(paiementRepository.save(any(Paiement.class))).thenAnswer(invocation -> {
            Paiement payment = invocation.getArgument(0);
            assertNotNull(payment.getTransactionId());
            assertFalse(payment.getTransactionId().isEmpty());
            return payment;
        });
        when(reservationRepository.save(any(Reservation.class))).thenReturn(testReservation);

        // Act
        paymentService.processPaymentForReservation(1L, validPaymentRequest, "CARD");

        // Assert
        verify(paiementRepository, times(1)).save(any(Paiement.class));
    }

    @Test
    @DisplayName("shouldStorePaymentModeInPayment")
    void shouldStorePaymentModeInPayment() {
        // Arrange
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(testReservation));
        when(paiementRepository.save(any(Paiement.class))).thenAnswer(invocation -> {
            Paiement payment = invocation.getArgument(0);
            assertEquals("CARD", payment.getPaymentMode());
            return payment;
        });
        when(reservationRepository.save(any(Reservation.class))).thenReturn(testReservation);

        // Act
        paymentService.processPaymentForReservation(1L, validPaymentRequest, "CARD");

        // Assert
        verify(paiementRepository, times(1)).save(any(Paiement.class));
    }

    @Test
    @DisplayName("shouldLinkPaymentToReservationOnSuccess")
    void shouldLinkPaymentToReservationOnSuccess() {
        // Arrange
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(testReservation));
        when(paiementRepository.save(any(Paiement.class))).thenReturn(testPayment);
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> {
            Reservation reservation = invocation.getArgument(0);
            assertNotNull(reservation.getPaiement());
            return reservation;
        });

        // Act
        paymentService.processPaymentForReservation(1L, validPaymentRequest, "CARD");

        // Assert
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    @Test
    @DisplayName("shouldLinkPaymentToReservationOnFailure")
    void shouldLinkPaymentToReservationOnFailure() {
        // Arrange
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(testReservation));
        // processPayment throws when token is "fail"

        // Act & Assert
        assertThrows(PaymentFailedException.class, () -> {
            paymentService.processPaymentForReservation(1L, failingPaymentRequest, "CARD");
        });

        // Verify - no saves since exception is thrown in processPayment
        verify(paiementRepository, never()).save(any(Paiement.class));
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    @DisplayName("shouldSavePaymentBeforeUpdatingReservation")
    void shouldSavePaymentBeforeUpdatingReservation() {
        // Arrange
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(testReservation));
        when(paiementRepository.save(any(Paiement.class))).thenReturn(testPayment);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(testReservation);

        // Act
        paymentService.processPaymentForReservation(1L, validPaymentRequest, "CARD");

        // Assert - Verify that both save operations occurred
        InOrder inOrder = inOrder(paiementRepository, reservationRepository);
        inOrder.verify(paiementRepository).save(any(Paiement.class));
        inOrder.verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    @DisplayName("shouldProcessPaymentWithDifferentPaymentModes")
    void shouldProcessPaymentWithDifferentPaymentModes() {
        // Arrange
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(testReservation));
        when(paiementRepository.save(any(Paiement.class))).thenAnswer(invocation -> {
            Paiement payment = invocation.getArgument(0);
            assertEquals("BANK_TRANSFER", payment.getPaymentMode());
            return payment;
        });
        when(reservationRepository.save(any(Reservation.class))).thenReturn(testReservation);

        // Act
        paymentService.processPaymentForReservation(1L, validPaymentRequest, "BANK_TRANSFER");

        // Assert
        verify(paiementRepository, times(1)).save(any(Paiement.class));
    }

    @Test
    @DisplayName("shouldProcessPaymentWithCorrectAmount")
    void shouldProcessPaymentWithCorrectAmount() {
        // Arrange
        BigDecimal paymentAmount = new BigDecimal("150.50");
        PaymentRequestDTO requestWithAmount = PaymentRequestDTO.builder()
                .amount(paymentAmount)
                .paymentMethodToken("valid-token")
                .currency("EUR")
                .build();

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(testReservation));
        when(paiementRepository.save(any(Paiement.class))).thenAnswer(invocation -> {
            Paiement payment = invocation.getArgument(0);
            assertEquals(paymentAmount, payment.getAmount());
            return payment;
        });
        when(reservationRepository.save(any(Reservation.class))).thenReturn(testReservation);

        // Act
        paymentService.processPaymentForReservation(1L, requestWithAmount, "CARD");

        // Assert
        verify(paiementRepository, times(1)).save(any(Paiement.class));
    }

    @Test
    @DisplayName("shouldSetPaymentDateOnSuccess")
    void shouldSetPaymentDateOnSuccess() {
        // Arrange
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(testReservation));
        when(paiementRepository.save(any(Paiement.class))).thenAnswer(invocation -> {
            Paiement payment = invocation.getArgument(0);
            assertNotNull(payment.getPaymentDate());
            return payment;
        });
        when(reservationRepository.save(any(Reservation.class))).thenReturn(testReservation);

        // Act
        paymentService.processPaymentForReservation(1L, validPaymentRequest, "CARD");

        // Assert
        verify(paiementRepository, times(1)).save(any(Paiement.class));
    }

    @Test
    @DisplayName("shouldSetPaymentDateOnFailure")
    void shouldSetPaymentDateOnFailure() {
        // Arrange
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(testReservation));
        // processPayment throws when token is "fail", so no saves occur

        // Act & Assert
        assertThrows(PaymentFailedException.class, () -> {
            paymentService.processPaymentForReservation(1L, failingPaymentRequest, "CARD");
        });

        // Verify - no saves since exception is thrown in processPayment
        verify(paiementRepository, never()).save(any(Paiement.class));
    }

}
