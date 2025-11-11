package tech.ydb.apps.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import com.google.common.io.CharStreams;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tech.ydb.apps.annotation.YdbRetryable;

/**
 *
 * @author Aleksandr Gorshenin
 */
@Service
public class ScriptService {
    private static final Logger logger = LoggerFactory.getLogger(ScriptService.class);

    private final EntityManager em;
    private final ResourceLoader rl;

    public ScriptService(EntityManager em, ResourceLoader rl) {
        this.em = em;
        this.rl = rl;
    }

    @Transactional
    @YdbRetryable
    public void executeClean() {
        executeScript("sql/drop.sql");
    }

    @Transactional
    @YdbRetryable
    public void executeInit() {
        executeScript("sql/init.sql");
    }

    @Transactional
    @YdbRetryable
    public void executeLoad() {
        executeScript("sql/load.sql");
    }

    private void executeScript(String path) {
        String script = readResourceFile(path);
        if (script == null) {
            logger.warn("cannot find script {} in classpath", path);
            return;
        }
        em.createNativeQuery(script).executeUpdate();
    }

    private String readResourceFile(String location) {
        Resource resource = rl.getResource("classpath:" + location);
        try (InputStream is = resource.getInputStream()) {
            return CharStreams.toString(new InputStreamReader(is, StandardCharsets.UTF_8));
        } catch (IOException e) {
            return null;
        }
    }
}
