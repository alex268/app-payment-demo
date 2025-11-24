package tech.ydb.apps;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PreDestroy;

import com.google.common.util.concurrent.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import tech.ydb.apps.service.ArchiveMachine;
import tech.ydb.apps.service.ConfigService;
import tech.ydb.apps.service.PaymentMachine;
import tech.ydb.apps.service.PaymentWebResource;
import tech.ydb.apps.service.ScriptService;


/**
 *
 * @author Aleksandr Gorshenin
 */
@EnableRetry
@EnableAsync
@EnableScheduling
@EnableConfigurationProperties(AppConfig.class)
@SpringBootApplication
public class Application implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        try {
            ConfigurableApplicationContext app = SpringApplication.run(Application.class, args);
            if (app.getBean(Application.class).isCompleted) {
                app.close();
            }
        } catch (RuntimeException ex) {
            logger.error("app finished with error", ex);
        }
    }

    private final ApplicationContext ctx;
    private final AppConfig config;
    private boolean isCompleted = false;
    private volatile boolean isStopped = false;

    public Application(ApplicationContext ctx, AppConfig config) {
        this.ctx = ctx;
        this.config = config;
    }

    @PreDestroy
    public void close() throws Exception {
        isStopped = true;
        logger.info("CLI app is waiting for finishing");
        ctx.getBean(PaymentMachine.class).stop();
        ctx.getBean(ArchiveMachine.class).stop();
        logger.info("CLI app has finished");
    }

    @Override
    public void run(String... args) {
        logger.info("CLI app has started with database {}", config.getConnection());

        for (String arg : args) {
            if (arg.startsWith("--")) { // skip Spring parameters
                continue;
            }

            logger.info("execute {} step", arg);

            if ("clean".equalsIgnoreCase(arg)) {
                runClean();
            }

            if ("init".equalsIgnoreCase(arg)) {
                runInit();
            }

            if ("test".equalsIgnoreCase(arg)) {
                runTest();
            }
        }

        if (!isCompleted) {
            loadConfigAndStart();
        }
    }

    private void loadConfigAndStart() {
        ctx.getBean(ConfigService.class).init();
        ctx.getBean(PaymentMachine.class).start();
    }

    private void runClean() {
        ctx.getBean(ScriptService.class).executeClean();
        // notify to close app
        isCompleted = true;
    }

    private void runInit() {
        ctx.getBean(ScriptService.class).executeInit();
        ctx.getBean(ScriptService.class).executeLoad();
        // notify to close app
        isCompleted = true;
    }

    private void runTest() {
        logger.info("CLI app execute test workload with RPS {}", config.getTestRps());

        loadConfigAndStart();

        PaymentWebResource paymentResource = ctx.getBean(PaymentWebResource.class);

        int workThreads = Math.max(1, config.getTestRps() / 2000);
        AtomicInteger tc = new AtomicInteger(0);
        ExecutorService workers = Executors.newFixedThreadPool(workThreads,
                r -> new Thread(r, "work-" + tc.incrementAndGet())
        );

        RateLimiter rl = RateLimiter.create(config.getTestRps());
        long finishAt = System.currentTimeMillis() + config.getWorkloadDurationSec() * 1000;

        for (int idx = 0; idx < workThreads; idx++) {
            workers.execute(() -> {
                long now = System.currentTimeMillis();
                while (now < finishAt && !isStopped) {
                    rl.acquire();
                    paymentResource.makeRandomPayment();
                    now = System.currentTimeMillis();
                }
            });
        }

        try {
            workers.shutdown();
            workers.awaitTermination(config.getWorkloadDurationSec() + 10, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            logger.warn("workload was interrupted");
        }

        // notify to close app
        isCompleted = true;
    }
}
