package com.sbsolutions.rilybricoule.dto.admin;



import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AdminMarketingSummaryDTO {

    private long totalPromos;
    private long activePromos;
    private long scheduledPromos;
    private long expiredPromos;
    private long inactivePromos;

    private long totalCoupons;
    private long activeCoupons;
    private long expiredCoupons;
    private long inactiveCoupons;

    private long totalNotifications;
    private long readNotifications;
    private long unreadNotifications;

    private List<MarketingPromoRowDTO> recentPromos;
    private List<MarketingNotificationRowDTO> recentNotifications;

    @Data
    public static class MarketingPromoRowDTO {
        private Long id;
        private String code;
        private String title;
        private String description;
        private String status;
        private String targetAudience;
        private Integer currentUsage;
        private Integer maxUsage;
        private LocalDate startDate;
        private LocalDate endDate;
    }

    @Data
    public static class MarketingNotificationRowDTO {
        private Long id;
        private String content;
        private String type;
        private boolean read;
        private LocalDateTime date;
    }
}
