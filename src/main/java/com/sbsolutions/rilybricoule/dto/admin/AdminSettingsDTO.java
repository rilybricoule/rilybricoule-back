package com.sbsolutions.rilybricoule.dto.admin;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminSettingsDTO {

    private int globalRate;
    private boolean usePerCategory;
    private List<CategoryCommissionDTO> categories;
    private List<PaymentGatewayDTO> gateways;
    private String currency;
    private String language;
    private String timezone;
    private String dateFormat;
    private int cancelWindow;
    private int cancelFeePercent;
    private int freeCancelWindow;
    private boolean autoRefund;
    private String refundDelay;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoryCommissionDTO {
        private String id;
        private String label;
        private int rate;
        private String color;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentGatewayDTO {
        private String id;
        private String label;
        private String description;
        private boolean enabled;
        private boolean testMode;
        private String color;
        private String logo;
    }
}
