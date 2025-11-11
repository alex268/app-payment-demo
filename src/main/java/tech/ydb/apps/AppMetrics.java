package tech.ydb.apps;

import java.sql.SQLException;
import java.time.Duration;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.config.MeterFilterReply;
import io.micrometer.core.instrument.logging.LoggingMeterRegistry;
import io.micrometer.core.instrument.logging.LoggingRegistryConfig;
import io.micrometer.core.instrument.util.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;

import tech.ydb.core.StatusCode;
import tech.ydb.jdbc.exception.YdbStatusable;

/**
 *
 * @author Aleksandr Gorshenin
 */
@Configuration
public class AppMetrics {
    private final static Logger logger = LoggerFactory.getLogger(AppMetrics.class);

    private final static Counter.Builder TX_COUNT = Counter.builder("app.tx");
    private final static Counter.Builder TX_RETRIES = Counter.builder("app.tx.retries");
//    private final static Timer.Builder TX_LATENCY = Timer.builder("app.tx.latency")
//            .serviceLevelObjectives(
//                    Duration.ofMillis(10), Duration.ofMillis(20), Duration.ofMillis(40),
//                    Duration.ofMillis(80), Duration.ofMillis(120), Duration.ofMillis(160), Duration.ofMillis(200),
//                    Duration.ofMillis(300), Duration.ofMillis(400), Duration.ofMillis(500), Duration.ofMillis(1000)
//            );

    private final static String[] LOG_METRICS = new String[] {
            "app.payment.latency",
//            "app.exec.latency",
            "app.tx.retries",
    };

    @Bean
    public LoggingMeterRegistry loggingMeterRegistry() {
        LoggingRegistryConfig cfg = new LoggingRegistryConfig() {
            @Override
            public String get(String s) {
                return null;
            }
            @Override
            public Duration step() {
                return Duration.ofSeconds(1);
            }
        };

        LoggingMeterRegistry meterRegistry = LoggingMeterRegistry.builder(cfg)
                .clock(Clock.SYSTEM)
                .loggingSink(logger::info)
                .threadFactory(new NamedThreadFactory("ticker"))
                .build();

        meterRegistry.config().meterFilter(new MeterFilter() {
            @Override
            public MeterFilterReply accept(Meter.Id id) {
                for (String name: LOG_METRICS) {
                    if (name.equals(id.getName())) {
                        return MeterFilterReply.ACCEPT;
                    }
                }
                return MeterFilterReply.DENY;
            }
        });

        return meterRegistry;
    }

    @Bean
    public RetryListener getRetryListener(MeterRegistry registry) {
        Counter txCount = TX_COUNT.register(registry);
        Meter.MeterProvider<Counter> retries = TX_RETRIES.withRegistry(registry);

        return new RetryListener() {
            @Override
            public <T, E extends Throwable> boolean open(RetryContext ctx, RetryCallback<T, E> cb) {
                txCount.increment();
                return true;
            }

            @Override
            public <T, E extends Throwable> void onError(RetryContext ctx, RetryCallback<T, E> cb, Throwable th) {
                logger.debug("retry operation with error {} ", printSqlException(th));
                retries.withTag("status", extractStatusCode(th).name()).increment();
            }

            @Override
            public <T, E extends Throwable> void onSuccess(RetryContext context, RetryCallback<T, E> cb, T result) {
                // nothing
            }
        };
    }

    public static String printSqlException(Throwable th) {
        Throwable ex = th;
        while (ex != null) {
            if (ex instanceof SQLException) {
                return ex.getMessage();
            }
            ex = ex.getCause();
        }
        return th.getMessage();
    }

    public static StatusCode extractStatusCode(Throwable th) {
        Throwable ex = th;
        while (ex != null) {
            if (ex instanceof YdbStatusable ydbException) {
                return ydbException.getStatus().getCode();
            }
            ex = ex.getCause();
        }
        return StatusCode.CLIENT_INTERNAL_ERROR;
    }
}
