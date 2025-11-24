package tech.ydb.apps.impl;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tech.ydb.apps.AppConfig;
import tech.ydb.apps.annotation.YdbRetryable;
import tech.ydb.apps.entity.Saldo;
import tech.ydb.apps.entity.SaldoKey;
import tech.ydb.apps.entity.SaldoUpdate;
import tech.ydb.apps.entity.Transaction;
import tech.ydb.apps.entity.TransactionArchive;
import tech.ydb.apps.model.PaymentTask;
import tech.ydb.apps.repository.SaldoRepo;
import tech.ydb.apps.repository.SaldoUpdateRepo;
import tech.ydb.apps.repository.TransactionArchiveRepo;
import tech.ydb.apps.repository.TransactionRepo;
import tech.ydb.apps.service.ConfigService;
import tech.ydb.apps.service.PaymentService;

/**
 *
 * @author Aleksandr Gorshenin
 */
@Service
@Profile("default")
public class PaymentServiceDefault implements PaymentService {
    private final ConfigService config;
    private final SaldoRepo saldoRepo;
    private final SaldoUpdateRepo updatesRepo;
    private final TransactionRepo transactionRepo;
    private final TransactionArchiveRepo archiveRepo;
    private final int saldoShiftMs;


    public PaymentServiceDefault(AppConfig appConfig, ConfigService config, SaldoRepo saldos, SaldoUpdateRepo updates,
            TransactionRepo transactions, TransactionArchiveRepo archives) {
        this.config = config;
        this.saldoRepo = saldos;
        this.updatesRepo = updates;
        this.transactionRepo = transactions;
        this.archiveRepo = archives;
        this.saldoShiftMs = appConfig.getSaldoShiftMs();
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
    @Transactional
    @YdbRetryable
    public void executePayments(Instant accepted, List<PaymentTask> batch) {
        Set<SaldoKey> saldoIds = new HashSet<>();
        for (PaymentTask task: batch) {
            saldoIds.add(task.getKeyA());
        }

        List<Transaction> transactions = new ArrayList<>();
        List<SaldoUpdate> updatesToInsert = new ArrayList<>();
        List<SaldoUpdate> updatesToDelete = new ArrayList<>();

        Iterable<Saldo> allSaldos = saldoRepo.findAllById(saldoIds);

        // execute all delayed updates
        Instant listBefore = accepted.minusMillis(saldoShiftMs);
        for (SaldoUpdate up: updatesRepo.findAllBySaldoAndCreatedBefore(saldoIds, listBefore)) {
            Saldo saldo = saldoRepo.findById(up.getId().getSaldoKey()).orElseThrow();
            saldo.updateSaldo(saldo.getAmount().add(up.getAmount()));
            updatesToDelete.add(up);
        }

        for (PaymentTask task: batch) {
            SaldoKey keyA = task.getKeyA();
            SaldoKey keyB = task.getKeyB();
            BigDecimal payment = task.getAmount();

            Saldo saldo = saldoRepo.findById(keyA).orElseThrow();
            saldo.updateSaldo(saldo.getAmount().add(payment.negate()));

            Transaction tx = new Transaction(task.getId(), keyA, keyB, payment, task.getInputTs(), accepted);
            updatesToInsert.add(new SaldoUpdate(keyB, tx, payment));
            transactions.add(tx);
        }

        saldoRepo.saveAll(allSaldos);
        transactionRepo.saveAll(transactions);
        updatesRepo.deleteAll(updatesToDelete);
        updatesRepo.saveAll(updatesToInsert);
    }

    @Override
    @Transactional
    @YdbRetryable
    public void completeSaldoUpdates() {
        List<SaldoUpdate> updatesToDelete = new ArrayList<>();

        Iterable<Saldo> allSaldos = saldoRepo.findAll();
        for (SaldoUpdate up: updatesRepo.findAll()) {
            Saldo saldo = saldoRepo.findById(up.getId().getSaldoKey()).orElseThrow();
            saldo.updateSaldo(saldo.getAmount().add(up.getAmount()));
            updatesToDelete.add(up);
        }

        saldoRepo.saveAll(allSaldos);
        updatesRepo.deleteAll(updatesToDelete);
    }

    @Override
    @Transactional
    public void archiveTransactions(List<Transaction> batch) {
        List<TransactionArchive> archive = new ArrayList<>();

        for (Transaction tx: batch) {
            archive.add(new TransactionArchive(tx));
        }

        transactionRepo.deleteAllInBatch(batch);
        archiveRepo.saveAll(archive);
    }
}
