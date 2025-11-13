package tech.ydb.apps.service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Meter.MeterProvider;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import tech.ydb.apps.AppConfig;
import tech.ydb.apps.AppMetrics;
import tech.ydb.apps.model.PaymentResponse;
import tech.ydb.apps.model.PaymentTask;

/**
 *
 * @author Aleksandr Gorshenin
 */
@Service
public class PaymentMachine {
    private final static Logger logger = LoggerFactory.getLogger(PaymentMachine.class);

    private final static String COLD = "cold";

    private final static Timer.Builder EXECUTION_LATENCY = Timer.builder("app.exec.latency")
            .serviceLevelObjectives(
                    Duration.ofMillis(10), Duration.ofMillis(25), Duration.ofMillis(50),
                    Duration.ofMillis(100), Duration.ofMillis(200), Duration.ofMillis(400), Duration.ofMillis(800)
            );

    private final static DistributionSummary.Builder EXECUTION_SIZE = DistributionSummary.builder("app.exec.batchsize")
            .serviceLevelObjectives(5, 10, 50, 100, 250, 500, 1000, 2500, 5000, 10000);

    private final static Counter.Builder EXECUTION_FAILED = Counter.builder("app.exec.failed");

    private final PaymentService paymentService;
    private final int maxBatchSize;

    private final MeterProvider<Timer> executionTime;
    private final MeterProvider<Counter> executionFailed;
    private final MeterProvider<DistributionSummary> executionSize;

    private final Map<String, Processor> processors = new HashMap<>();

    public PaymentMachine(PaymentService paymentService, AppConfig config, MeterRegistry registry) {
        this.paymentService = paymentService;
        this.maxBatchSize = config.getBatchMaxSize();

        this.executionTime = EXECUTION_LATENCY.withRegistry(registry);
        this.executionFailed = EXECUTION_FAILED.withRegistry(registry);
        this.executionSize = EXECUTION_SIZE.withRegistry(registry);
    }

    public void start() {
        stop();

        processors.put(COLD, new Processor(COLD));
        logger.info("registered processor {}", COLD);

        for (String code: paymentService.listProcessors()) {
            if (processors.containsKey(code)) {
                continue;
            }

            processors.put(code, new Processor(code));
            logger.info("registered processor {}", code);
        }

        logger.info("start {} processors", processors.size());
        processors.values().forEach(Processor::start);
    }

    public void stop() {
        if (processors.isEmpty()) {
            return;
        }

        logger.info("stop {} processors", processors.size());
        processors.values().forEach(Processor::stop);
        processors.values().forEach(Processor::join);
        processors.clear();

        logger.info("finish all saldo updates");
        paymentService.completeSaldoUpdates();
    }

    public void registerTask(PaymentTask task) {
        String code = paymentService.findProcessor(task.getKeyA());
        processors.getOrDefault(code != null ? code : COLD, processors.get(COLD)).addTask(task);
    }

    private class Processor implements Runnable {
        private final String name;
        private final ConcurrentLinkedQueue<PaymentTask> queue = new ConcurrentLinkedQueue<>();
        private final Thread thread;
        private final ReentrantLock lock = new ReentrantLock();
        private final Condition sleep = lock.newCondition();

        private volatile boolean isStopped = false;
        private volatile boolean isSleep = false;

        public Processor(String name) {
            this.name = name;
            this.thread = new Thread(this, "app-queue-" + name);
        }

        public void addTask(PaymentTask task) {
            queue.add(task);
            wakeUp();
        }

        public void start() {
            thread.start();
        }

        public void stop() {
            isStopped = true;
            wakeUp();
        }

        public void join() {
            try {
                thread.join();
            } catch (InterruptedException ex) {
                logger.warn("thread {} joining was interrupted", name);
            }
        }

        private void wakeUp() {
            if (isSleep) {
                lock.lock();
                try {
                    sleep.signal();
                } finally {
                    lock.unlock();
                }
            }
        }

        private void sleep() {
            lock.lock();
            try {
                isSleep = true;
                sleep.await(100, TimeUnit.MILLISECONDS);
                isSleep = false;
            } catch (InterruptedException ex) {
                logger.warn("thread {} sleeping was interrupted", name);
            } finally {
                lock.unlock();
            }
        }

        @Override
        public void run() {
            while (true) {
                PaymentTask next = queue.poll();
                if (next == null) {
                    if (isStopped) {
                        return;
                    }

                    sleep();
                    continue;
                }

                List<PaymentTask> batch = new ArrayList<>(maxBatchSize);
                while (next != null && batch.size() < maxBatchSize) {
                    batch.add(next);
                    next = queue.poll();
                }

                executionSize.withTag("processor", name).record(batch.size());

                String status = "OK";
                try {
                    Instant started = Instant.now();
                    paymentService.executePayments(started, batch);
                    long ms = Instant.now().toEpochMilli() - started.toEpochMilli();

                    executionTime.withTag("processor", name).record(ms, TimeUnit.MILLISECONDS);
                    if (logger.isDebugEnabled()) {
                        logger.debug("executed {} payments in {} ms, {} waiting", batch.size(), ms, queue.size());
                    }
                } catch (Exception ex) {
                    status = AppMetrics.printSqlException(ex);
                    logger.warn("cannot process payments: {}", status);
                    String code = AppMetrics.extractStatusCode(ex).toString();
                    executionFailed.withTags("processor", name, "reason", code).increment();
                }
                PaymentResponse resp = new PaymentResponse(status);
                batch.forEach(f -> f.getResult().complete(resp));
            }
        }
    }
}
