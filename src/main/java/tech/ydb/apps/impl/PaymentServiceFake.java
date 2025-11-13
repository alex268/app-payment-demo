package tech.ydb.apps.impl;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import tech.ydb.apps.entity.SaldoKey;
import tech.ydb.apps.model.PaymentTask;
import tech.ydb.apps.service.ConfigService;
import tech.ydb.apps.service.PaymentService;

/**
 *
 * @author Aleksandr Gorshenin
 */
@Service
@Profile("fake")
public class PaymentServiceFake implements PaymentService {
    protected final ConfigService config;

//    protected final LongAdder total = new LongAdder();
//    protected final LongAdder accA = new LongAdder();
//    protected final LongAdder accB = new LongAdder();

    public PaymentServiceFake(ConfigService config) {
        this.config = config;
    }

    @Override
    public Collection<String> listProcessors() {
        return config.listProcessors();
    }

    @Override
    public String findProcessor(SaldoKey key) {
        return config.findProcessor(key);
    }

    @Override
    public void executePayments(Instant accepted, List<PaymentTask> batch) {
//        total.add(batch.size());
//        for (PaymentTask task: batch) {
//            if (task.getKeyA().getAccountId() == 1L) {
//                accA.increment();
//            }
//            if (task.getKeyB().getAccountId() == 1L) {
//                accB.increment();
//            }
//        }
//
        try {
            Thread.sleep(50);
        } catch (InterruptedException ex) {

        }
    }

    @Override
    public void completeSaldoUpdates() {
        // Nothing
    }

//    @PreDestroy
//    public void close() {
//        System.out.println("TOTAL = " + total.longValue());
//        System.out.println("A1 = " + accA.longValue());
//        System.out.println("B1 = " + accB.longValue());
//    }
}
