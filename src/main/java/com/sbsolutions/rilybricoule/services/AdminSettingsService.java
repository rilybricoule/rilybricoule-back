package com.sbsolutions.rilybricoule.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sbsolutions.rilybricoule.dto.admin.AdminSettingsDTO;
import com.sbsolutions.rilybricoule.entity.AdminSettings;
import com.sbsolutions.rilybricoule.repository.AdminSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class AdminSettingsService {

    private static final Long SETTINGS_ID = 1L;

    private final AdminSettingsRepository repository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public AdminSettingsDTO get() {
        AdminSettings settings = repository.findById(SETTINGS_ID)
                .orElseGet(this::defaultEntity);

        return toDto(settings);
    }

    @Transactional
    public AdminSettingsDTO update(AdminSettingsDTO request) {
        AdminSettings settings = repository.findById(SETTINGS_ID)
                .orElseGet(this::defaultEntity);

        settings.setGlobalRate(request.getGlobalRate());
        settings.setUsePerCategory(request.isUsePerCategory());
        settings.setCategoriesJson(writeJson(request.getCategories()));
        settings.setGatewaysJson(writeJson(request.getGateways()));
        settings.setCurrency(valueOrDefault(request.getCurrency(), "MAD"));
        settings.setLanguage(valueOrDefault(request.getLanguage(), "fr"));
        settings.setTimezone(valueOrDefault(request.getTimezone(), "Africa/Casablanca"));
        settings.setDateFormat(valueOrDefault(request.getDateFormat(), "DD/MM/YYYY"));
        settings.setCancelWindow(request.getCancelWindow());
        settings.setCancelFeePercent(request.getCancelFeePercent());
        settings.setFreeCancelWindow(request.getFreeCancelWindow());
        settings.setAutoRefund(request.isAutoRefund());
        settings.setRefundDelay(valueOrDefault(request.getRefundDelay(), "5"));

        return toDto(repository.save(settings));
    }

    private AdminSettings defaultEntity() {
        return AdminSettings.builder()
                .id(SETTINGS_ID)
                .globalRate(15)
                .usePerCategory(true)
                .categoriesJson(writeJson(defaultCategories()))
                .gatewaysJson(writeJson(defaultGateways()))
                .currency("MAD")
                .language("fr")
                .timezone("Africa/Casablanca")
                .dateFormat("DD/MM/YYYY")
                .cancelWindow(24)
                .cancelFeePercent(20)
                .freeCancelWindow(48)
                .autoRefund(true)
                .refundDelay("5")
                .build();
    }

    private AdminSettingsDTO toDto(AdminSettings settings) {
        return AdminSettingsDTO.builder()
                .globalRate(settings.getGlobalRate())
                .usePerCategory(settings.isUsePerCategory())
                .categories(readJson(
                        settings.getCategoriesJson(),
                        new TypeReference<List<AdminSettingsDTO.CategoryCommissionDTO>>() {},
                        defaultCategories()
                ))
                .gateways(readJson(
                        settings.getGatewaysJson(),
                        new TypeReference<List<AdminSettingsDTO.PaymentGatewayDTO>>() {},
                        defaultGateways()
                ))
                .currency(settings.getCurrency())
                .language(settings.getLanguage())
                .timezone(settings.getTimezone())
                .dateFormat(settings.getDateFormat())
                .cancelWindow(settings.getCancelWindow())
                .cancelFeePercent(settings.getCancelFeePercent())
                .freeCancelWindow(settings.getFreeCancelWindow())
                .autoRefund(settings.isAutoRefund())
                .refundDelay(settings.getRefundDelay())
                .build();
    }

    private List<AdminSettingsDTO.CategoryCommissionDTO> defaultCategories() {
        return List.of(
                category("plomberie", "Plomberie", 15, "#38bdf8"),
                category("electricite", "Electricite", 15, "#f59e0b"),
                category("nettoyage", "Nettoyage", 12, "#22c55e"),
                category("jardinage", "Jardinage", 12, "#84cc16"),
                category("bricolage", "Bricolage", 15, "#a78bfa"),
                category("transport", "Transport", 18, "#f97316")
        );
    }

    private AdminSettingsDTO.CategoryCommissionDTO category(String id, String label, int rate, String color) {
        return AdminSettingsDTO.CategoryCommissionDTO.builder()
                .id(id)
                .label(label)
                .rate(rate)
                .color(color)
                .build();
    }

    private List<AdminSettingsDTO.PaymentGatewayDTO> defaultGateways() {
        return List.of(
                gateway("cmi", "CMI", "Paiement carte bancaire au Maroc", true, true, "#38bdf8", "CMI"),
                gateway("stripe", "Stripe", "Paiement international par carte", false, true, "#a78bfa", "ST"),
                gateway("paypal", "PayPal", "Paiement via compte PayPal", false, true, "#60a5fa", "PP"),
                gateway("cash", "Especes", "Paiement en especes au prestataire", true, false, "#22c55e", "MAD")
        );
    }

    private AdminSettingsDTO.PaymentGatewayDTO gateway(
            String id,
            String label,
            String description,
            boolean enabled,
            boolean testMode,
            String color,
            String logo
    ) {
        return AdminSettingsDTO.PaymentGatewayDTO.builder()
                .id(id)
                .label(label)
                .description(description)
                .enabled(enabled)
                .testMode(testMode)
                .color(color)
                .logo(logo)
                .build();
    }

    private <T> List<T> readJson(String json, TypeReference<List<T>> type, List<T> fallback) {
        if (json == null || json.isBlank()) return fallback;

        try {
            return objectMapper.readValue(json, type);
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value == null ? List.of() : value);
        } catch (Exception e) {
            throw new RuntimeException("Erreur serialization parametres", e);
        }
    }

    private String valueOrDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
