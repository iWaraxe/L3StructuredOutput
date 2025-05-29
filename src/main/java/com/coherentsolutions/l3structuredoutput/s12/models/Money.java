package com.coherentsolutions.l3structuredoutput.s12.models;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

/**
 * Custom money type with currency and formatting support.
 */
public record Money(
        BigDecimal amount,
        Currency currency
) {
    public Money {
        if (amount != null) {
            amount = amount.setScale(2, RoundingMode.HALF_UP);
        }
    }
    
    public static Money of(double amount, String currencyCode) {
        return new Money(
                BigDecimal.valueOf(amount),
                Currency.getInstance(currencyCode)
        );
    }
    
    public static Money of(String amount, String currencyCode) {
        return new Money(
                new BigDecimal(amount),
                Currency.getInstance(currencyCode)
        );
    }
    
    public String formatted() {
        return currency.getSymbol() + amount.toPlainString();
    }
    
    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot add money with different currencies");
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }
    
    public Money multiply(int quantity) {
        return new Money(
                this.amount.multiply(BigDecimal.valueOf(quantity)),
                this.currency
        );
    }
}