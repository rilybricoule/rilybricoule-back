package com.sbsolutions.rilybricoule.services;

import com.sbsolutions.rilybricoule.dto.admin.*;
import com.sbsolutions.rilybricoule.entity.Client;
import com.sbsolutions.rilybricoule.entity.Paiement;
import com.sbsolutions.rilybricoule.entity.Prestataire;
import com.sbsolutions.rilybricoule.entity.Reservation;
import com.sbsolutions.rilybricoule.repository.ClientRepository;
import com.sbsolutions.rilybricoule.repository.PaiementRepository;
import com.sbsolutions.rilybricoule.repository.PrestaireRepository;
import com.sbsolutions.rilybricoule.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final ClientRepository clientRepository;
    private final PrestaireRepository prestaireRepository;
    private final ReservationRepository reservationRepository;
    private final PaiementRepository paiementRepository;

    @Transactional(readOnly = true)
    public DashboardOverviewDTO getOverview() {
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate nextMonthStart = monthStart.plusMonths(1);

        return DashboardOverviewDTO.builder()
                .totalClients(clientRepository.count())
                .totalProviders(prestaireRepository.count())
                .pendingProviders(prestaireRepository.countByStatus(Prestataire.ProviderStatus.PENDING))
                .activeReservations(reservationRepository.countByStatusIn(List.of(
                        Reservation.ReservationStatus.PENDING_PAYMENT,
                        Reservation.ReservationStatus.CONFIRMED
                )))
                .completedReservationsToday(reservationRepository.countByStatusAndReservationDate(
                        Reservation.ReservationStatus.COMPLETED,
                        today
                ))
                .monthlyRevenue(successRevenueBetween(monthStart, nextMonthStart))
                .build();
    }

    @Transactional(readOnly = true)
    public DashboardPaymentStatusDTO getPaymentStatus() {
        long paid = paiementRepository.countByPaymentStatus(Paiement.PaymentStatus.SUCCESS);
        long pending = paiementRepository.countByPaymentStatus(Paiement.PaymentStatus.PENDING);
        long refunded = paiementRepository.countByPaymentStatus(Paiement.PaymentStatus.REFUNDED);
        long failed = paiementRepository.countByPaymentStatus(Paiement.PaymentStatus.FAILED);

        return DashboardPaymentStatusDTO.builder()
                .paid(paid)
                .pending(pending)
                .refunded(refunded)
                .failed(failed)
                .total(paid + pending + refunded + failed)
                .build();
    }

    @Transactional(readOnly = true)
    public DashboardTrendsDTO getTrends() {
        LocalDate today = LocalDate.now();
        Periods periods = Periods.from(today);

        List<Client> clients = clientRepository.findAll();
        List<Prestataire> providers = prestaireRepository.findAll();
        List<Reservation> reservations = reservationRepository.findAll();

        return DashboardTrendsDTO.builder()
                .clientsWeekCurrent(countCreatedBetween(clients, Client::getCreatedAt, periods.weekStart, periods.weekEnd))
                .clientsWeekPrevious(countCreatedBetween(clients, Client::getCreatedAt, periods.previousWeekStart, periods.weekStart))
                .clientsMonthCurrent(countCreatedBetween(clients, Client::getCreatedAt, periods.monthStart, periods.monthEnd))
                .clientsMonthPrevious(countCreatedBetween(clients, Client::getCreatedAt, periods.previousMonthStart, periods.monthStart))
                .clientsYearCurrent(countCreatedBetween(clients, Client::getCreatedAt, periods.yearStart, periods.yearEnd))
                .clientsYearPrevious(countCreatedBetween(clients, Client::getCreatedAt, periods.previousYearStart, periods.yearStart))
                .providersWeekCurrent(countCreatedBetween(providers, Prestataire::getCreatedAt, periods.weekStart, periods.weekEnd))
                .providersWeekPrevious(countCreatedBetween(providers, Prestataire::getCreatedAt, periods.previousWeekStart, periods.weekStart))
                .providersMonthCurrent(countCreatedBetween(providers, Prestataire::getCreatedAt, periods.monthStart, periods.monthEnd))
                .providersMonthPrevious(countCreatedBetween(providers, Prestataire::getCreatedAt, periods.previousMonthStart, periods.monthStart))
                .providersYearCurrent(countCreatedBetween(providers, Prestataire::getCreatedAt, periods.yearStart, periods.yearEnd))
                .providersYearPrevious(countCreatedBetween(providers, Prestataire::getCreatedAt, periods.previousYearStart, periods.yearStart))
                .reservationsWeekCurrent(countReservationsBetween(reservations, periods.weekStart, periods.weekEnd))
                .reservationsWeekPrevious(countReservationsBetween(reservations, periods.previousWeekStart, periods.weekStart))
                .reservationsMonthCurrent(countReservationsBetween(reservations, periods.monthStart, periods.monthEnd))
                .reservationsMonthPrevious(countReservationsBetween(reservations, periods.previousMonthStart, periods.monthStart))
                .reservationsYearCurrent(countReservationsBetween(reservations, periods.yearStart, periods.yearEnd))
                .reservationsYearPrevious(countReservationsBetween(reservations, periods.previousYearStart, periods.yearStart))
                .revenueWeekCurrent(successRevenueBetween(periods.weekStart, periods.weekEnd))
                .revenueWeekPrevious(successRevenueBetween(periods.previousWeekStart, periods.weekStart))
                .revenueMonthCurrent(successRevenueBetween(periods.monthStart, periods.monthEnd))
                .revenueMonthPrevious(successRevenueBetween(periods.previousMonthStart, periods.monthStart))
                .revenueYearCurrent(successRevenueBetween(periods.yearStart, periods.yearEnd))
                .revenueYearPrevious(successRevenueBetween(periods.previousYearStart, periods.yearStart))
                .build();
    }

    @Transactional(readOnly = true)
    public DashboardExtraKpiDTO getExtraKpis() {
        LocalDate today = LocalDate.now();
        Periods periods = Periods.from(today);

        List<Reservation> reservations = reservationRepository.findAll();
        List<Paiement> payments = paiementRepository.findAll();

        return DashboardExtraKpiDTO.builder()
                .cancellationRate(rate(
                        countReservationsByStatusBetween(reservations, Reservation.ReservationStatus.CANCELLED, LocalDate.MIN, LocalDate.MAX),
                        reservations.size()
                ))
                .paymentFailureRate(rate(
                        payments.stream().filter(p -> p.getPaymentStatus() == Paiement.PaymentStatus.FAILED).count(),
                        payments.size()
                ))
                .paymentFailureRateWeekCurrent(paymentFailureRateBetween(payments, periods.weekStart, periods.weekEnd))
                .paymentFailureRateWeekPrevious(paymentFailureRateBetween(payments, periods.previousWeekStart, periods.weekStart))
                .paymentFailureRateMonthCurrent(paymentFailureRateBetween(payments, periods.monthStart, periods.monthEnd))
                .paymentFailureRateMonthPrevious(paymentFailureRateBetween(payments, periods.previousMonthStart, periods.monthStart))
                .paymentFailureRateYearCurrent(paymentFailureRateBetween(payments, periods.yearStart, periods.yearEnd))
                .paymentFailureRateYearPrevious(paymentFailureRateBetween(payments, periods.previousYearStart, periods.yearStart))
                .activeReservationsWeekCurrent(activeReservationsBetween(reservations, periods.weekStart, periods.weekEnd))
                .activeReservationsWeekPrevious(activeReservationsBetween(reservations, periods.previousWeekStart, periods.weekStart))
                .activeReservationsMonthCurrent(activeReservationsBetween(reservations, periods.monthStart, periods.monthEnd))
                .activeReservationsMonthPrevious(activeReservationsBetween(reservations, periods.previousMonthStart, periods.monthStart))
                .activeReservationsYearCurrent(activeReservationsBetween(reservations, periods.yearStart, periods.yearEnd))
                .activeReservationsYearPrevious(activeReservationsBetween(reservations, periods.previousYearStart, periods.yearStart))
                .cancellationRateWeekCurrent(cancellationRateBetween(reservations, periods.weekStart, periods.weekEnd))
                .cancellationRateWeekPrevious(cancellationRateBetween(reservations, periods.previousWeekStart, periods.weekStart))
                .cancellationRateMonthCurrent(cancellationRateBetween(reservations, periods.monthStart, periods.monthEnd))
                .cancellationRateMonthPrevious(cancellationRateBetween(reservations, periods.previousMonthStart, periods.monthStart))
                .cancellationRateYearCurrent(cancellationRateBetween(reservations, periods.yearStart, periods.yearEnd))
                .cancellationRateYearPrevious(cancellationRateBetween(reservations, periods.previousYearStart, periods.yearStart))
                .build();
    }

    @Transactional(readOnly = true)
    public DashboardQualityTrendsDTO getQualityTrends() {
        LocalDate today = LocalDate.now();
        Periods periods = Periods.from(today);
        List<Prestataire> providers = prestaireRepository.findAll();

        return DashboardQualityTrendsDTO.builder()
                .verifiedWeekCurrent(countProvidersByStatusBetween(providers, Prestataire.ProviderStatus.APPROVED, periods.weekStart, periods.weekEnd))
                .verifiedWeekPrevious(countProvidersByStatusBetween(providers, Prestataire.ProviderStatus.APPROVED, periods.previousWeekStart, periods.weekStart))
                .verifiedMonthCurrent(countProvidersByStatusBetween(providers, Prestataire.ProviderStatus.APPROVED, periods.monthStart, periods.monthEnd))
                .verifiedMonthPrevious(countProvidersByStatusBetween(providers, Prestataire.ProviderStatus.APPROVED, periods.previousMonthStart, periods.monthStart))
                .verifiedYearCurrent(countProvidersByStatusBetween(providers, Prestataire.ProviderStatus.APPROVED, periods.yearStart, periods.yearEnd))
                .verifiedYearPrevious(countProvidersByStatusBetween(providers, Prestataire.ProviderStatus.APPROVED, periods.previousYearStart, periods.yearStart))
                .pendingWeekCurrent(countProvidersByStatusBetween(providers, Prestataire.ProviderStatus.PENDING, periods.weekStart, periods.weekEnd))
                .pendingWeekPrevious(countProvidersByStatusBetween(providers, Prestataire.ProviderStatus.PENDING, periods.previousWeekStart, periods.weekStart))
                .pendingMonthCurrent(countProvidersByStatusBetween(providers, Prestataire.ProviderStatus.PENDING, periods.monthStart, periods.monthEnd))
                .pendingMonthPrevious(countProvidersByStatusBetween(providers, Prestataire.ProviderStatus.PENDING, periods.previousMonthStart, periods.monthStart))
                .pendingYearCurrent(countProvidersByStatusBetween(providers, Prestataire.ProviderStatus.PENDING, periods.yearStart, periods.yearEnd))
                .pendingYearPrevious(countProvidersByStatusBetween(providers, Prestataire.ProviderStatus.PENDING, periods.previousYearStart, periods.yearStart))
                .build();
    }

    @Transactional(readOnly = true)
    public DashboardPerformanceDTO getPerformance() {
        LocalDate today = LocalDate.now();
        Periods periods = Periods.from(today);
        List<Reservation> reservations = reservationRepository.findAll();

        long totalReservations = reservations.size();
        long completedTotal = reservations.stream()
                .filter(r -> r.getStatus() == Reservation.ReservationStatus.COMPLETED)
                .count();

        List<DashboardRankingItemDTO> providerRanking = rankingBy(
                reservations.stream()
                        .filter(r -> r.getStatus() == Reservation.ReservationStatus.COMPLETED)
                        .toList(),
                reservation -> providerName(reservation.getPrestataire())
        );

        List<DashboardRankingItemDTO> zoneRanking = rankingBy(
                reservations.stream()
                        .filter(r -> r.getStatus() == Reservation.ReservationStatus.COMPLETED)
                        .toList(),
                reservation -> extractCity(reservation.getPrestataire() != null ? reservation.getPrestataire().getAddress() : null)
        );

        DashboardRankingItemDTO topProvider = providerRanking.isEmpty() ? null : providerRanking.get(0);
        DashboardRankingItemDTO topZone = zoneRanking.isEmpty() ? null : zoneRanking.get(0);

        return DashboardPerformanceDTO.builder()
                .completionRate(rate(completedTotal, totalReservations))
                .avgRevenuePerCompleted(avgRevenueBetween(periods.yearStart, periods.yearEnd, completedTotal))
                .topProviderName(topProvider != null ? topProvider.getName() : "—")
                .topProviderCompletedCount(topProvider != null ? topProvider.getCount() : 0)
                .topZoneName(topZone != null ? topZone.getName() : "—")
                .topZoneCompletedCount(topZone != null ? topZone.getCount() : 0)
                .providerRanking(providerRanking)
                .zoneRanking(zoneRanking)
                .completedWeekCurrent(countReservationsByStatusBetween(reservations, Reservation.ReservationStatus.COMPLETED, periods.weekStart, periods.weekEnd))
                .completedWeekPrevious(countReservationsByStatusBetween(reservations, Reservation.ReservationStatus.COMPLETED, periods.previousWeekStart, periods.weekStart))
                .completedMonthCurrent(countReservationsByStatusBetween(reservations, Reservation.ReservationStatus.COMPLETED, periods.monthStart, periods.monthEnd))
                .completedMonthPrevious(countReservationsByStatusBetween(reservations, Reservation.ReservationStatus.COMPLETED, periods.previousMonthStart, periods.monthStart))
                .completedYearCurrent(countReservationsByStatusBetween(reservations, Reservation.ReservationStatus.COMPLETED, periods.yearStart, periods.yearEnd))
                .completedYearPrevious(countReservationsByStatusBetween(reservations, Reservation.ReservationStatus.COMPLETED, periods.previousYearStart, periods.yearStart))
                .completionRateWeekCurrent(completionRateBetween(reservations, periods.weekStart, periods.weekEnd))
                .completionRateWeekPrevious(completionRateBetween(reservations, periods.previousWeekStart, periods.weekStart))
                .completionRateMonthCurrent(completionRateBetween(reservations, periods.monthStart, periods.monthEnd))
                .completionRateMonthPrevious(completionRateBetween(reservations, periods.previousMonthStart, periods.monthStart))
                .completionRateYearCurrent(completionRateBetween(reservations, periods.yearStart, periods.yearEnd))
                .completionRateYearPrevious(completionRateBetween(reservations, periods.previousYearStart, periods.yearStart))
                .avgRevenueWeekCurrent(avgRevenueBetween(periods.weekStart, periods.weekEnd, countReservationsByStatusBetween(reservations, Reservation.ReservationStatus.COMPLETED, periods.weekStart, periods.weekEnd)))
                .avgRevenueWeekPrevious(avgRevenueBetween(periods.previousWeekStart, periods.weekStart, countReservationsByStatusBetween(reservations, Reservation.ReservationStatus.COMPLETED, periods.previousWeekStart, periods.weekStart)))
                .avgRevenueMonthCurrent(avgRevenueBetween(periods.monthStart, periods.monthEnd, countReservationsByStatusBetween(reservations, Reservation.ReservationStatus.COMPLETED, periods.monthStart, periods.monthEnd)))
                .avgRevenueMonthPrevious(avgRevenueBetween(periods.previousMonthStart, periods.monthStart, countReservationsByStatusBetween(reservations, Reservation.ReservationStatus.COMPLETED, periods.previousMonthStart, periods.monthStart)))
                .avgRevenueYearCurrent(avgRevenueBetween(periods.yearStart, periods.yearEnd, countReservationsByStatusBetween(reservations, Reservation.ReservationStatus.COMPLETED, periods.yearStart, periods.yearEnd)))
                .avgRevenueYearPrevious(avgRevenueBetween(periods.previousYearStart, periods.yearStart, countReservationsByStatusBetween(reservations, Reservation.ReservationStatus.COMPLETED, periods.previousYearStart, periods.yearStart)))
                .build();
    }

    private BigDecimal successRevenueBetween(LocalDate start, LocalDate end) {
        LocalDateTime startAt = start.atStartOfDay();
        LocalDateTime endAt = end.atStartOfDay();

        return paiementRepository.findAll().stream()
                .filter(p -> p.getPaymentStatus() == Paiement.PaymentStatus.SUCCESS)
                .filter(p -> isDateTimeBetween(paymentEffectiveDate(p), startAt, endAt))
                .map(Paiement::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal avgRevenueBetween(LocalDate start, LocalDate end, long completedCount) {
        if (completedCount <= 0) return BigDecimal.ZERO;

        return successRevenueBetween(start, end)
                .divide(BigDecimal.valueOf(completedCount), 2, RoundingMode.HALF_UP);
    }

    private <T> long countCreatedBetween(Collection<T> items, Function<T, LocalDateTime> dateExtractor, LocalDate start, LocalDate end) {
        LocalDateTime startAt = start.atStartOfDay();
        LocalDateTime endAt = end.atStartOfDay();

        return items.stream()
                .filter(item -> isDateTimeBetween(dateExtractor.apply(item), startAt, endAt))
                .count();
    }

    private long countReservationsBetween(Collection<Reservation> reservations, LocalDate start, LocalDate end) {
        return reservations.stream()
                .filter(reservation -> isDateBetween(reservation.getReservationDate(), start, end))
                .count();
    }

    private long countReservationsByStatusBetween(
            Collection<Reservation> reservations,
            Reservation.ReservationStatus status,
            LocalDate start,
            LocalDate end
    ) {
        return reservations.stream()
                .filter(reservation -> reservation.getStatus() == status)
                .filter(reservation -> isDateBetween(reservation.getReservationDate(), start, end))
                .count();
    }

    private long activeReservationsBetween(Collection<Reservation> reservations, LocalDate start, LocalDate end) {
        return reservations.stream()
                .filter(reservation ->
                        reservation.getStatus() == Reservation.ReservationStatus.PENDING_PAYMENT ||
                                reservation.getStatus() == Reservation.ReservationStatus.CONFIRMED
                )
                .filter(reservation -> isDateBetween(reservation.getReservationDate(), start, end))
                .count();
    }

    private double completionRateBetween(Collection<Reservation> reservations, LocalDate start, LocalDate end) {
        long total = countReservationsBetween(reservations, start, end);
        long completed = countReservationsByStatusBetween(reservations, Reservation.ReservationStatus.COMPLETED, start, end);
        return rate(completed, total);
    }

    private double cancellationRateBetween(Collection<Reservation> reservations, LocalDate start, LocalDate end) {
        long total = countReservationsBetween(reservations, start, end);
        long cancelled = countReservationsByStatusBetween(reservations, Reservation.ReservationStatus.CANCELLED, start, end);
        return rate(cancelled, total);
    }

    private double paymentFailureRateBetween(Collection<Paiement> payments, LocalDate start, LocalDate end) {
        LocalDateTime startAt = start.atStartOfDay();
        LocalDateTime endAt = end.atStartOfDay();

        List<Paiement> periodPayments = payments.stream()
                .filter(payment -> isDateTimeBetween(paymentEffectiveDate(payment), startAt, endAt))
                .toList();

        long failed = periodPayments.stream()
                .filter(payment -> payment.getPaymentStatus() == Paiement.PaymentStatus.FAILED)
                .count();

        return rate(failed, periodPayments.size());
    }

    private long countProvidersByStatusBetween(
            Collection<Prestataire> providers,
            Prestataire.ProviderStatus status,
            LocalDate start,
            LocalDate end
    ) {
        LocalDateTime startAt = start.atStartOfDay();
        LocalDateTime endAt = end.atStartOfDay();

        return providers.stream()
                .filter(provider -> provider.getStatus() == status)
                .filter(provider -> isDateTimeBetween(provider.getCreatedAt(), startAt, endAt))
                .count();
    }

    private List<DashboardRankingItemDTO> rankingBy(
            Collection<Reservation> reservations,
            Function<Reservation, String> nameExtractor
    ) {
        return reservations.stream()
                .map(nameExtractor)
                .filter(name -> name != null && !name.isBlank())
                .collect(Collectors.groupingBy(name -> name, Collectors.counting()))
                .entrySet()
                .stream()
                .map(entry -> DashboardRankingItemDTO.builder()
                        .name(entry.getKey())
                        .count(entry.getValue())
                        .build())
                .sorted(Comparator.comparing(DashboardRankingItemDTO::getCount).reversed())
                .limit(5)
                .toList();
    }

    private String providerName(Prestataire prestataire) {
        if (prestataire == null) return "Prestataire";
        if (prestataire.getBusinessName() != null && !prestataire.getBusinessName().isBlank()) return prestataire.getBusinessName();
        if (prestataire.getName() != null && !prestataire.getName().isBlank()) return prestataire.getName();

        String fullName = ((prestataire.getFirstName() == null ? "" : prestataire.getFirstName()) + " " +
                (prestataire.getLastName() == null ? "" : prestataire.getLastName())).trim();

        return fullName.isBlank() ? "Prestataire" : fullName;
    }

    private String extractCity(String address) {
        if (address == null || address.isBlank()) return "—";
        return address.split(",")[0].trim();
    }

    private LocalDateTime paymentEffectiveDate(Paiement paiement) {
        if (paiement.getPaymentDate() != null) return paiement.getPaymentDate();
        return paiement.getCreatedAt();
    }

    private boolean isDateBetween(LocalDate date, LocalDate start, LocalDate end) {
        return date != null && !date.isBefore(start) && date.isBefore(end);
    }

    private boolean isDateTimeBetween(LocalDateTime date, LocalDateTime start, LocalDateTime end) {
        return date != null && !date.isBefore(start) && date.isBefore(end);
    }

    private double rate(long part, long total) {
        if (total <= 0) return 0;
        return Math.round(((double) part / total) * 1000.0) / 10.0;
    }

    private record Periods(
            LocalDate weekStart,
            LocalDate weekEnd,
            LocalDate previousWeekStart,
            LocalDate monthStart,
            LocalDate monthEnd,
            LocalDate previousMonthStart,
            LocalDate yearStart,
            LocalDate yearEnd,
            LocalDate previousYearStart
    ) {
        static Periods from(LocalDate today) {
            LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            LocalDate monthStart = today.withDayOfMonth(1);
            LocalDate yearStart = today.withDayOfYear(1);

            return new Periods(
                    weekStart,
                    weekStart.plusWeeks(1),
                    weekStart.minusWeeks(1),
                    monthStart,
                    monthStart.plusMonths(1),
                    monthStart.minusMonths(1),
                    yearStart,
                    yearStart.plusYears(1),
                    yearStart.minusYears(1)
            );
        }
    }
}
