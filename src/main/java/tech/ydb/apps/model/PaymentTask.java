package tech.ydb.apps.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jetty.util.Promise;

import tech.ydb.apps.entity.SaldoKey;

/**
 *
 * @author Aleksandr Gorshenin
 */
public class PaymentTask {
    private final String id;
    private final SaldoKey keyA;
    private final SaldoKey keyB;
    private final BigDecimal amount;

    private final Instant input = Instant.now();
    private final CompletableFuture<PaymentResponse> result = new Promise.Completable<>();

    public PaymentTask(String id, SaldoKey keyA, SaldoKey keyB, BigDecimal amount) {
        this.id = id;
        this.keyA = keyA;
        this.keyB = keyB;
        this.amount = amount;
    }

    public String getId() {
        return id;
    }

    public SaldoKey getKeyA() {
        return keyA;
    }

    public SaldoKey getKeyB() {
        return keyB;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Instant getInputTs() {
        return input;
    }

    public CompletableFuture<PaymentResponse> getResult() {
        return this.result;
    }
}
