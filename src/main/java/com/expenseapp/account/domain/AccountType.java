package com.expenseapp.account.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

/**
 * Enum representing different types of accounts.
 *
 * <p><strong>Wire format:</strong> the stable enum constant name is used for JSON
 * serialization (for example {@code "SALARY"}, {@code "MUTUAL_FUND"}), which is
 * the value the client sends and expects back.</p>
 *
 * <p><strong>Deserialization is tolerant:</strong> it accepts the constant name,
 * the human readable display name and variants that differ only by case or by
 * {@code _} / {@code -} / space separators. So all of the following resolve to
 * {@link #SALARY}: {@code "SALARY"}, {@code "salary"}, {@code "Salary"}.</p>
 */
public enum AccountType {
    SAVINGS("Savings"),
    SALARY("Salary"),
    CURRENT("Current"),
    INVESTMENT("Investment"),
    MUTUAL_FUND("Mutual Funds"),
    CREDIT_CARD("Credit Cards");

    private final String displayName;

    AccountType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Canonical JSON value: the enum constant name, e.g. {@code "MUTUAL_FUND"}.
     */
    @JsonValue
    public String getValue() {
        return name();
    }

    /**
     * Tolerant JSON deserialization.
     *
     * @param raw the raw request value (may be {@code null} or blank)
     * @return the matching {@link AccountType}, or {@code null} for a null/blank input
     * @throws IllegalArgumentException if the value does not match any account type
     */
    @JsonCreator
    public static AccountType fromValue(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        String normalized = normalize(raw);
        for (AccountType type : values()) {
            if (normalize(type.name()).equals(normalized)
                    || normalize(type.displayName).equals(normalized)) {
                return type;
            }
        }

        throw new IllegalArgumentException(
                "Unknown account type: '" + raw + "'. Accepted values: " + Arrays.toString(values()));
    }

    private static String normalize(String value) {
        return value.trim().replace(' ', '_').replace('-', '_').toUpperCase();
    }

    @Override
    public String toString() {
        return displayName;
    }
}
