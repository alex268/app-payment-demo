package tech.ydb.apps.service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import tech.ydb.apps.model.PaymentRequest;
import tech.ydb.apps.model.PaymentResponse;
import tech.ydb.apps.model.PaymentTask;

/**
 *
 * @author Aleksandr Gorshenin
 */
@RestController
public class PaymentWebResource {
    private final PaymentMachine processor;
    private final ConfigService config;

    private final Timer paymentLatency;
    private final Counter paymentCount;

    public PaymentWebResource(MeterRegistry metrics, PaymentMachine processor, ConfigService config) {
        this.processor = processor;
        this.config = config;
        this.paymentLatency = Timer.builder("app.payment.latency")
                .publishPercentiles(0.5, 0.9, 0.95, 0.99, 1.0)
                .register(metrics);
        this.paymentCount = Counter.builder("app.payment").register(metrics);
    }

    @Async
    @PostMapping("/payment")
    public CompletableFuture<PaymentResponse> makePayment(PaymentRequest request) {
        long accepted = System.currentTimeMillis();
        try {
            paymentCount.increment();
            PaymentTask task = config.createPaymentTask(request);
            processor.registerTask(task);
            return task.getResult().whenComplete((resp, th) -> {
                long ms = System.currentTimeMillis() - accepted;
                paymentLatency.record(ms, TimeUnit.MILLISECONDS);
            });
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    @PostMapping("/randomPayment")
    public CompletableFuture<PaymentResponse> makeRandomPayment() {
        return makePayment(config.createRandomPayment());
    }
}
