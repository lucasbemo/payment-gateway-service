package com.payment.gateway.domain.payment.model;

import java.util.HashMap;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Value Object representing payment metadata.
 */
@Getter
@EqualsAndHashCode
public class PaymentMetadata {

    private final Map<String, String> data;

    public PaymentMetadata(Map<String, String> data) {
        this.data = data != null ? new HashMap<>(data) : new HashMap<>();
    }

    public static PaymentMetadata empty() {
        return new PaymentMetadata(null);
    }

    public static PaymentMetadata of(Map<String, String> data) {
        return new PaymentMetadata(data);
    }

    public String get(String key) {
        return data.get(key);
    }

    public boolean containsKey(String key) {
        return data.containsKey(key);
    }

    public boolean isEmpty() {
        return data.isEmpty();
    }
}
