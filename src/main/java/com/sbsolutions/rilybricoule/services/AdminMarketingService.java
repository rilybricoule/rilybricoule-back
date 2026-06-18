package com.sbsolutions.rilybricoule.services;

import com.sbsolutions.rilybricoule.dto.admin.AdminMarketingSummaryDTO;
import com.sbsolutions.rilybricoule.entity.Coupon;
import com.sbsolutions.rilybricoule.entity.Notification;
import com.sbsolutions.rilybricoule.repository.CouponRepository;
import com.sbsolutions.rilybricoule.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminMarketingService {

    private final CouponRepository couponRepository;
    private final NotificationRepository notificationRepository;

    public AdminMarketingSummaryDTO getSummary() {
        var coupons = couponRepository.findAll();
        var notifications = notificationRepository.findAll();
        LocalDate today = LocalDate.now();

        AdminMarketingSummaryDTO dto = new AdminMarketingSummaryDTO();

        long inactive = coupons.stream()
                .filter(c -> Boolean.FALSE.equals(c.getActive()))
                .count();

        long expired = coupons.stream()
                .filter(c -> Boolean.TRUE.equals(c.getActive()))
                .filter(c -> c.getExpiryDate() != null && c.getExpiryDate().isBefore(today))
                .count();

        long scheduled = coupons.stream()
                .filter(c -> Boolean.TRUE.equals(c.getActive()))
                .filter(c -> c.getStartDate() != null && c.getStartDate().isAfter(today))
                .count();

        long active = coupons.stream()
                .filter(c -> Boolean.TRUE.equals(c.getActive()))
                .filter(c -> c.getExpiryDate() == null || !c.getExpiryDate().isBefore(today))
                .filter(c -> c.getStartDate() == null || !c.getStartDate().isAfter(today))
                .count();

        dto.setTotalPromos(coupons.size());
        dto.setActivePromos(active);
        dto.setScheduledPromos(scheduled);
        dto.setExpiredPromos(expired);
        dto.setInactivePromos(inactive);

        dto.setTotalCoupons(coupons.size());
        dto.setActiveCoupons(active);
        dto.setExpiredCoupons(expired);
        dto.setInactiveCoupons(inactive);

        dto.setTotalNotifications(notifications.size());
        dto.setReadNotifications(notifications.stream().filter(Notification::isVu).count());
        dto.setUnreadNotifications(notifications.stream().filter(n -> !n.isVu()).count());

        dto.setRecentPromos(
                couponRepository.findTop5ByOrderByCreatedAtDesc()
                        .stream()
                        .map(this::toPromoRow)
                        .toList()
        );

        dto.setRecentNotifications(
                notificationRepository.findTop5ByOrderByDateDesc()
                        .stream()
                        .map(this::toNotificationRow)
                        .toList()
        );

        return dto;
    }

    private AdminMarketingSummaryDTO.MarketingPromoRowDTO toPromoRow(Coupon coupon) {
        AdminMarketingSummaryDTO.MarketingPromoRowDTO row =
                new AdminMarketingSummaryDTO.MarketingPromoRowDTO();

        row.setId(coupon.getId());
        row.setCode(coupon.getCode());
        row.setTitle(coupon.getTitle() != null ? coupon.getTitle() : coupon.getCode());
        row.setDescription(coupon.getDescription());
        row.setStatus(resolvePromoStatus(coupon));
        row.setTargetAudience(coupon.getTargetAudience());
        row.setCurrentUsage(coupon.getCurrentUsage());
        row.setMaxUsage(coupon.getMaxUsage());
        row.setStartDate(coupon.getStartDate());
        row.setEndDate(coupon.getExpiryDate());

        return row;
    }

    private AdminMarketingSummaryDTO.MarketingNotificationRowDTO toNotificationRow(Notification notification) {
        AdminMarketingSummaryDTO.MarketingNotificationRowDTO row =
                new AdminMarketingSummaryDTO.MarketingNotificationRowDTO();

        row.setId(notification.getId());
        row.setContent(notification.getContenu());
        row.setType(notification.getType() == null ? null : notification.getType().name());
        row.setRead(notification.isVu());
        row.setDate(notification.getDate());

        return row;
    }

    private String resolvePromoStatus(Coupon coupon) {
        LocalDate today = LocalDate.now();

        if (Boolean.FALSE.equals(coupon.getActive())) {
            return "INACTIVE";
        }

        if (coupon.getStartDate() != null && coupon.getStartDate().isAfter(today)) {
            return "SCHEDULED";
        }

        if (coupon.getExpiryDate() != null && coupon.getExpiryDate().isBefore(today)) {
            return "EXPIRED";
        }

        return "ACTIVE";
    }
}
