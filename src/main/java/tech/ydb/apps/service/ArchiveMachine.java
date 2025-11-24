package tech.ydb.apps.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tech.ydb.apps.AppConfig;
import tech.ydb.apps.entity.Transaction;
import tech.ydb.apps.repository.TransactionRepo;

/**
 *
 * @author Aleksandr Gorshenin
 */
@Service
public class ArchiveMachine {
    private final static int MAX_BATCH_SIZE = 5000;
    private final static Logger logger = LoggerFactory.getLogger(ArchiveMachine.class);
    private final static Counter.Builder TRANSACTION_ARCHIVED = Counter.builder("app.tx.archived");

    private final PaymentService paymentService;
    private final TransactionRepo transactionRepo;
    private final ExecutorService executor;
    private final int shiftSeconds;
    private final Counter archivedCounter;

    private volatile boolean isStopped = false;

    public ArchiveMachine(PaymentService payments, TransactionRepo txRepo, AppConfig config, MeterRegistry registry) {
        this.paymentService = payments;
        this.transactionRepo = txRepo;
        this.shiftSeconds = config.getArchiveShiftSeconds();
        this.archivedCounter = TRANSACTION_ARCHIVED.register(registry);

        final AtomicInteger threadCounter = new AtomicInteger(0);
        this.executor = Executors.newFixedThreadPool(
                config.getArchiveThreadsCount(),
                r -> new Thread(r, "archiver-" + threadCounter.incrementAndGet())
        );
    }

    public void stop() {
        isStopped = true;
        try {
            executor.shutdown();
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            logger.warn("archive machine stopping was interrupted");
        }
    }

    @Scheduled(fixedRate = 10, timeUnit = TimeUnit.SECONDS, initialDelay = 5)
    @Transactional(readOnly = true)
    public void executeArchive() {
        List<CompletableFuture<?>> results = new ArrayList<>();
        List<Transaction> batch = new ArrayList<>();
        Instant archiveBefore = Instant.now().minusSeconds(shiftSeconds);

        for (Transaction tx: transactionRepo.findAllByProcessedTsBeforeOrderById(archiveBefore)) {
            batch.add(tx);
            if (batch.size() >= MAX_BATCH_SIZE) {
                results.add(CompletableFuture.runAsync(new ArchiveTask(batch), executor));
                batch = new ArrayList<>();
            }
        }

        if (!batch.isEmpty()) {
            results.add(CompletableFuture.runAsync(new ArchiveTask(batch), executor));
        }

        results.forEach(CompletableFuture::join);
    }

    private class ArchiveTask implements Runnable {
        private final List<Transaction> batch;

        public ArchiveTask(List<Transaction> batch) {
            this.batch = batch;
        }

        @Override
        public void run() {
            if (isStopped) {
                return;
            }

            try {
                logger.debug("archive {} transactions", batch.size());
                paymentService.archiveTransactions(batch);
                archivedCounter.increment(batch.size());
            } catch (Exception ex) {
                logger.warn("cannot archive {} transactions by {}", batch.size(), ex.getMessage());
            }
        }
    }
}
