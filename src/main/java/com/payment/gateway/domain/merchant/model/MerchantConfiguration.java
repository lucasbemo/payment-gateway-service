package com.payment.gateway.domain.merchant.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Value Object representing merchant configuration.
 */
@Getter
@EqualsAndHashCode
public class MerchantConfiguration {

    private final Map<String, String> settings;

    public MerchantConfiguration(Map<String, String> settings) {
        this.settings = settings != null ? new HashMap<>(settings) : new HashMap<>();
    }

    public static MerchantConfiguration empty() {
        return new MerchantConfiguration(null);
    }

    public static MerchantConfiguration of(Map<String, String> settings) {
        return new MerchantConfiguration(settings);
    }

    public String get(String key) {
        return settings.get(key);
    }

    public String getOrDefault(String key, String defaultValue) {
        return settings.getOrDefault(key, defaultValue);
    }

    public boolean containsKey(String key) {
        return settings.containsKey(key);
    }

    public boolean isEmpty() {
        return settings.isEmpty();
    }
}
