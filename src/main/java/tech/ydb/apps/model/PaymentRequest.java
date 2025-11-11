package tech.ydb.apps.model;

import java.math.BigDecimal;

/**
 *
 * @author Aleksandr Gorshenin
 */
public class PaymentRequest {
    private final String bicA;
    private final String bicB;
    private final BigDecimal amount;

    public PaymentRequest(String bicA, String bicB, BigDecimal amount) {
        this.bicA = bicA;
        this.bicB = bicB;
        this.amount = amount;
    }

    public String getBicA() {
        return bicA;
    }

    public String getBicB() {
        return bicB;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        return "Payment[" + bicA + "->" + bicB + ", " + amount + "]";
    }
}
