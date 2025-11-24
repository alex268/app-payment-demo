package tech.ydb.apps;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.Name;

/**
 *
 * @author Aleksandr Gorshenin
 */

@ConfigurationProperties(prefix = "app")
public class AppConfig {
    private final String connection;
    private final int threadsCount;
    private final int workloadDurationSec;
    private final int testRps;
    private final int batchMaxSize;
    private final int saldoShiftMs;
    private final int archiveThreadsCount;
    private final int archiveShiftSeconds;

    public AppConfig(
            @Name("connection") String connection,
            @Name("threadsCount") int threadsCount,
            @Name("workloadDuration") int workloadDuration,
            @Name("testRps") int testRps,
            @Name("batchMaxSize") int batchMaxSize,
            @Name("saldoShiftMs") int saldoShiftMs,
            @Name("archiveThreadsCount") int archiveThreadsCount,
            @Name("archiveShiftSeconds") int archiveShiftSeconds
    ) {
        this.connection = connection;
        this.threadsCount = threadsCount <= 0 ? Runtime.getRuntime().availableProcessors() : threadsCount;
        this.workloadDurationSec = workloadDuration;
        this.testRps = testRps;
        this.batchMaxSize = batchMaxSize;
        this.saldoShiftMs = saldoShiftMs;
        this.archiveThreadsCount = archiveThreadsCount;
        this.archiveShiftSeconds = archiveShiftSeconds;
    }

    public String getConnection() {
        return this.connection;
    }

    public int getThreadCount() {
        return this.threadsCount;
    }

    public int getWorkloadDurationSec() {
        return workloadDurationSec;
    }

    public int getTestRps() {
        return testRps;
    }

    public int getBatchMaxSize() {
        return batchMaxSize;
    }

    public int getSaldoShiftMs() {
        return saldoShiftMs;
    }

    public int getArchiveThreadsCount() {
        return archiveThreadsCount;
    }

    public int getArchiveShiftSeconds() {
        return archiveShiftSeconds;
    }
}
