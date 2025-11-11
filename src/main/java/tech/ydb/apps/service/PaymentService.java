package tech.ydb.apps.service;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

import tech.ydb.apps.entity.SaldoKey;
import tech.ydb.apps.model.PaymentTask;

/**
 *
 * @author Aleksandr Gorshenin
 */
public interface PaymentService {
    Collection<String> listProcessors();
    String findProcessor(SaldoKey key);

    void executePayments(Instant accepted, List<PaymentTask> batch);
}
