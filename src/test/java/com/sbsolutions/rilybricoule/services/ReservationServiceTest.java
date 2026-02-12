package com.sbsolutions.rilybricoule.services;

import com.sbsolutions.rilybricoule.dto.CreateReservationRequest;
import com.sbsolutions.rilybricoule.dto.ReservationResponse;
import com.sbsolutions.rilybricoule.entity.*;
import com.sbsolutions.rilybricoule.repository.ClientRepository;
import com.sbsolutions.rilybricoule.repository.CouponRepository;
import com.sbsolutions.rilybricoule.repository.PrestaireRepository;
import com.sbsolutions.rilybricoule.repository.ReservationRepository;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReservationService Unit Tests")
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private PrestaireRepository prestaireRepository;

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CouponService couponService;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private ReservationService reservationService;

    private CreateReservationRequest validRequest;
    private Client testClient;
    private Prestataire testPrestataire;
    private Coupon validCoupon;
    private Reservation savedReservation;

    @BeforeEach
    void setUp() {
        // Setup test data
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

        validCoupon = Coupon.builder()
                .id(1L)
                .code("SAVE10")
                .discountAmount(new BigDecimal("10.00"))
                .discountPercentage(10)
                .expiryDate(LocalDate.now().plusDays(30))
                .active(true)
                .build();

        validRequest = CreateReservationRequest.builder()
                .clientId(1L)
                .prestaireId(1L)
                .reservationDate(LocalDate.now().plusDays(5))
                .reservationTime(LocalTime.of(10, 0))
                .description("Home repair service")
                .couponId(null)
                .build();

        savedReservation = Reservation.builder()
                .id(1L)
                .client(testClient)
                .prestataire(testPrestataire)
                .reservationDate(validRequest.getReservationDate())
                .reservationTime(validRequest.getReservationTime())
                .description(validRequest.getDescription())
                .totalPrice(new BigDecimal("100.00"))
                .discountAmount(BigDecimal.ZERO)
                .status(Reservation.ReservationStatus.PENDING_PAYMENT)
                .build();
    }

    // ===== SUCCESS CASES =====

    @Test
    @DisplayName("shouldCreateReservationSuccessfully")
    void shouldCreateReservationSuccessfully() {
        // Arrange
        when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));
        when(prestaireRepository.findById(1L)).thenReturn(Optional.of(testPrestataire));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(savedReservation);

        // Act
        ReservationResponse response = reservationService.createReservation(validRequest);

        // Assert
        assertNotNull(response);
        assertEquals(Reservation.ReservationStatus.PENDING_PAYMENT.toString(), response.getStatus());
        assertEquals(testClient.getId(), response.getClient().getId());
        assertEquals(testPrestataire.getId(), response.getPrestataire().getId());

        // Verify
        verify(clientRepository, times(1)).findById(1L);
        verify(prestaireRepository, times(1)).findById(1L);
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    @Test
    @DisplayName("shouldCreateReservationWithInitialStatusPendingPayment")
    void shouldCreateReservationWithInitialStatusPendingPayment() {
        // Arrange
        when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));
        when(prestaireRepository.findById(1L)).thenReturn(Optional.of(testPrestataire));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> {
            Reservation reservation = invocation.getArgument(0);
            assertEquals(Reservation.ReservationStatus.PENDING_PAYMENT, reservation.getStatus());
            reservation.setId(1L);
            return reservation;
        });

        // Act
        ReservationResponse response = reservationService.createReservation(validRequest);

        // Assert
        assertEquals(Reservation.ReservationStatus.PENDING_PAYMENT.toString(), response.getStatus());
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    @Test
    @DisplayName("shouldCreateReservationWithoutCoupon")
    void shouldCreateReservationWithoutCoupon() {
        // Arrange
        validRequest.setCouponId(null);
        Reservation reservationNoCoupon = Reservation.builder()
                .id(1L)
                .client(testClient)
                .prestataire(testPrestataire)
                .reservationDate(validRequest.getReservationDate())
                .reservationTime(validRequest.getReservationTime())
                .description(validRequest.getDescription())
                .totalPrice(new BigDecimal("100.00"))
                .discountAmount(BigDecimal.ZERO)
                .status(Reservation.ReservationStatus.PENDING_PAYMENT)
                .coupon(null)
                .build();

        when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));
        when(prestaireRepository.findById(1L)).thenReturn(Optional.of(testPrestataire));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservationNoCoupon);

        // Act
        ReservationResponse response = reservationService.createReservation(validRequest);

        // Assert
        assertNotNull(response);
        assertNull(response.getCoupon());
        assertEquals(new BigDecimal("100.00").toString(), response.getTotalPrice().toString());
        verify(couponService, never()).findById(any());
    }

    @Test
    @DisplayName("shouldApplyCouponWhenValidCouponProvided")
    void shouldApplyCouponWhenValidCouponProvided() {
        // Arrange
        validRequest.setCouponId(1L);
        Reservation reservationWithCoupon = Reservation.builder()
                .id(1L)
                .client(testClient)
                .prestataire(testPrestataire)
                .reservationDate(validRequest.getReservationDate())
                .reservationTime(validRequest.getReservationTime())
                .description(validRequest.getDescription())
                .coupon(validCoupon)
                .discountAmount(validCoupon.getDiscountAmount())
                .totalPrice(new BigDecimal("90.00"))
                .status(Reservation.ReservationStatus.PENDING_PAYMENT)
                .build();

        when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));
        when(prestaireRepository.findById(1L)).thenReturn(Optional.of(testPrestataire));
        when(couponService.findById(1L)).thenReturn(Optional.of(validCoupon));
        when(couponService.isValid(validCoupon)).thenReturn(true);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservationWithCoupon);

        // Act
        ReservationResponse response = reservationService.createReservation(validRequest);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getCoupon());
        assertEquals(1L, response.getCoupon().getId());
        assertEquals(new BigDecimal("10.00").toString(), response.getDiscountAmount().toString());

        // Verify
        verify(couponService, times(1)).findById(1L);
        verify(couponService, times(1)).isValid(validCoupon);
    }

    @Test
    @DisplayName("shouldIgnoreInvalidCoupon")
    void shouldIgnoreInvalidCoupon() {
        // Arrange
        validRequest.setCouponId(1L);
        Coupon expiredCoupon = Coupon.builder()
                .id(1L)
                .code("SAVE10")
                .discountAmount(new BigDecimal("10.00"))
                .discountPercentage(10)
                .expiryDate(LocalDate.now().minusDays(1))
                .active(true)
                .build();

        when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));
        when(prestaireRepository.findById(1L)).thenReturn(Optional.of(testPrestataire));
        when(couponService.findById(1L)).thenReturn(Optional.of(expiredCoupon));
        when(couponService.isValid(expiredCoupon)).thenReturn(false);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(savedReservation);

        // Act
        ReservationResponse response = reservationService.createReservation(validRequest);

        // Assert
        assertNotNull(response);
        assertNull(response.getCoupon());
        assertEquals(BigDecimal.ZERO.toString(), response.getDiscountAmount().toString());

        // Verify
        verify(couponService, times(1)).findById(1L);
        verify(couponService, times(1)).isValid(expiredCoupon);
    }

    @Test
    @DisplayName("shouldIgnoreNonExistentCoupon")
    void shouldIgnoreNonExistentCoupon() {
        // Arrange
        validRequest.setCouponId(999L);

        when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));
        when(prestaireRepository.findById(1L)).thenReturn(Optional.of(testPrestataire));
        when(couponService.findById(999L)).thenReturn(Optional.empty());
        when(reservationRepository.save(any(Reservation.class))).thenReturn(savedReservation);

        // Act
        ReservationResponse response = reservationService.createReservation(validRequest);

        // Assert
        assertNotNull(response);
        assertNull(response.getCoupon());
        verify(couponService, times(1)).findById(999L);
        verify(couponService, never()).isValid(any());
    }

    // ===== FAILURE CASES =====

    @Test
    @DisplayName("shouldThrowExceptionWhenClientNotFound")
    void shouldThrowExceptionWhenClientNotFound() {
        // Arrange
        validRequest.setClientId(999L);
        when(clientRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            reservationService.createReservation(validRequest);
        });

        // Verify
        verify(clientRepository, times(1)).findById(999L);
        verify(prestaireRepository, never()).findById(any());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("shouldThrowExceptionWhenPrestaireNotFound")
    void shouldThrowExceptionWhenPrestaireNotFound() {
        // Arrange
        validRequest.setPrestaireId(999L);
        when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));
        when(prestaireRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            reservationService.createReservation(validRequest);
        });

        // Verify
        verify(clientRepository, times(1)).findById(1L);
        verify(prestaireRepository, times(1)).findById(999L);
        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("shouldThrowExceptionWithCorrectMessageWhenClientNotFound")
    void shouldThrowExceptionWithCorrectMessageWhenClientNotFound() {
        // Arrange
        validRequest.setClientId(999L);
        when(clientRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reservationService.createReservation(validRequest);
        });

        assertTrue(exception.getMessage().contains("Client not found"));
        assertTrue(exception.getMessage().contains("999"));
    }

    @Test
    @DisplayName("shouldThrowExceptionWithCorrectMessageWhenPrestaireNotFound")
    void shouldThrowExceptionWithCorrectMessageWhenPrestaireNotFound() {
        // Arrange
        validRequest.setPrestaireId(999L);
        when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));
        when(prestaireRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reservationService.createReservation(validRequest);
        });

        assertTrue(exception.getMessage().contains("Prestataire not found"));
        assertTrue(exception.getMessage().contains("999"));
    }

    @Test
    @DisplayName("shouldSetCorrectReservationDetails")
    void shouldSetCorrectReservationDetails() {
        // Arrange
        when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));
        when(prestaireRepository.findById(1L)).thenReturn(Optional.of(testPrestataire));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> {
            Reservation reservation = invocation.getArgument(0);
            assertEquals(testClient, reservation.getClient());
            assertEquals(testPrestataire, reservation.getPrestataire());
            assertEquals(validRequest.getReservationDate(), reservation.getReservationDate());
            assertEquals(validRequest.getReservationTime(), reservation.getReservationTime());
            assertEquals(validRequest.getDescription(), reservation.getDescription());
            reservation.setId(1L);
            return reservation;
        });

        // Act
        ReservationResponse response = reservationService.createReservation(validRequest);

        // Assert
        assertNotNull(response);
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    @Test
    @DisplayName("shouldCalculateTotalPriceCorrectly")
    void shouldCalculateTotalPriceCorrectly() {
        // Arrange
        when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));
        when(prestaireRepository.findById(1L)).thenReturn(Optional.of(testPrestataire));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(savedReservation);

        // Act
        ReservationResponse response = reservationService.createReservation(validRequest);

        // Assert
        assertNotNull(response);
        assertEquals(new BigDecimal("100.00").toString(), response.getTotalPrice().toString());
    }

    @Test
    @DisplayName("shouldCalculateTotalPriceWithDiscountCorrectly")
    void shouldCalculateTotalPriceWithDiscountCorrectly() {
        // Arrange
        validRequest.setCouponId(1L);
        Reservation reservationWithDiscount = Reservation.builder()
                .id(1L)
                .client(testClient)
                .prestataire(testPrestataire)
                .reservationDate(validRequest.getReservationDate())
                .reservationTime(validRequest.getReservationTime())
                .description(validRequest.getDescription())
                .coupon(validCoupon)
                .discountAmount(validCoupon.getDiscountAmount())
                .totalPrice(new BigDecimal("90.00"))
                .status(Reservation.ReservationStatus.PENDING_PAYMENT)
                .build();

        when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));
        when(prestaireRepository.findById(1L)).thenReturn(Optional.of(testPrestataire));
        when(couponService.findById(1L)).thenReturn(Optional.of(validCoupon));
        when(couponService.isValid(validCoupon)).thenReturn(true);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservationWithDiscount);

        // Act
        ReservationResponse response = reservationService.createReservation(validRequest);

        // Assert
        assertEquals(new BigDecimal("90.00").toString(), response.getTotalPrice().toString());
    }

    @Test
    @DisplayName("shouldNotSaveReservationWhenClientNotFound")
    void shouldNotSaveReservationWhenClientNotFound() {
        // Arrange
        validRequest.setClientId(999L);
        when(clientRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            reservationService.createReservation(validRequest);
        });

        // Verify
        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("shouldNotSaveReservationWhenPrestaireNotFound")
    void shouldNotSaveReservationWhenPrestaireNotFound() {
        // Arrange
        when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));
        when(prestaireRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            reservationService.createReservation(validRequest);
        });

        // Verify
        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("shouldSetDiscountAmountToZeroWhenNoCoupon")
    void shouldSetDiscountAmountToZeroWhenNoCoupon() {
        // Arrange
        when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));
        when(prestaireRepository.findById(1L)).thenReturn(Optional.of(testPrestataire));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(savedReservation);

        // Act
        ReservationResponse response = reservationService.createReservation(validRequest);

        // Assert
        assertEquals(BigDecimal.ZERO.toString(), response.getDiscountAmount().toString());
    }
}
