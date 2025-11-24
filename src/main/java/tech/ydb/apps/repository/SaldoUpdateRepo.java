package tech.ydb.apps.repository;

import java.time.Instant;
import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import tech.ydb.apps.entity.SaldoKey;
import tech.ydb.apps.entity.SaldoUpdate;
import tech.ydb.apps.entity.SaldoUpdateKey;

/**
 *
 * @author Aleksandr Gorshenin
 */
public interface SaldoUpdateRepo extends JpaRepository<SaldoUpdate, SaldoUpdateKey> {
    @Query("select u from SaldoUpdate u where u.id.key in (:keys) and u.id.createdTs < :ts")
    Iterable<SaldoUpdate> findAllBySaldoAndCreatedBefore(Collection<SaldoKey> keys, Instant ts);
}
