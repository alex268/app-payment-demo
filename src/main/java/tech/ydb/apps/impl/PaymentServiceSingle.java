package tech.ydb.apps.impl;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tech.ydb.apps.annotation.YdbRetryable;
import tech.ydb.apps.entity.Saldo;
import tech.ydb.apps.entity.SaldoKey;
import tech.ydb.apps.entity.Transaction;
import tech.ydb.apps.entity.TransactionArchive;
import tech.ydb.apps.model.PaymentTask;
import tech.ydb.apps.repository.SaldoRepo;
import tech.ydb.apps.repository.TransactionArchiveRepo;
import tech.ydb.apps.repository.TransactionRepo;
import tech.ydb.apps.service.PaymentService;
import tech.ydb.jdbc.YdbTracer;

/**
 *
 * @author Aleksandr Gorshenin
 */
@Service
@Profile("single")
public class PaymentServiceSingle implements PaymentService {
    private final SaldoRepo saldoRepo;
    private final TransactionRepo transactionRepo;
    private final TransactionArchiveRepo archiveRepo;

    public PaymentServiceSingle(SaldoRepo saldos, TransactionRepo transactions, TransactionArchiveRepo archives) {
        this.saldoRepo = saldos;
        this.transactionRepo = transactions;
        this.archiveRepo = archives;
    }

    @Override
    public Collection<String> listProcessors() {
        return Collections.emptyList();
    }

    @Override
    public String findProcessor(SaldoKey key) {
        return null;
    }

    @Override
    @YdbRetryable
    @Transactional
    public void executePayments(Instant accepted, List<PaymentTask> batch) {
        Set<SaldoKey> saldoIds = new HashSet<>();
        for (PaymentTask task: batch) {
            saldoIds.add(task.getKeyA());
            saldoIds.add(task.getKeyB());
        }

        // Prefetch all accounts to cache them
        Iterable<Saldo> allSaldos = saldoRepo.findAllById(saldoIds);
        List<Transaction> newTransactions = new ArrayList<>();

        for (PaymentTask task: batch) {
            Saldo a = saldoRepo.findById(task.getKeyA()).orElseThrow();
            Saldo b = saldoRepo.findById(task.getKeyB()).orElseThrow();

            BigDecimal amount = task.getAmount();

            a.updateSaldo(a.getAmount().add(amount.negate()));
            b.updateSaldo(b.getAmount().add(amount));

            newTransactions.add(new Transaction(task.getId(), a.getId(), b.getId(), amount, task.getInputTs(), accepted));
        }

        saldoRepo.saveAll(allSaldos); // batched update
        transactionRepo.saveAll(newTransactions); // batched insert
    }

    @Override
    public void completeSaldoUpdates() {
        // Nothing
    }

    @Override
    @Transactional
    public void archiveTransactions(List<Transaction> batch) {
        YdbTracer.current().markToPrint("archive");

        List<TransactionArchive> archive = new ArrayList<>();
        for (Transaction tx: batch) {
            archive.add(new TransactionArchive(tx));
        }

        transactionRepo.deleteAll(batch);
        archiveRepo.saveAll(archive);
    }
}
