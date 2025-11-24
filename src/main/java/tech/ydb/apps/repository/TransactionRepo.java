package tech.ydb.apps.repository;


import java.time.Instant;

import org.springframework.data.jpa.repository.JpaRepository;

import tech.ydb.apps.entity.Transaction;
import tech.ydb.apps.entity.TransactionKey;

/**
 *
 * @author Aleksandr Gorshenin
 */
public interface TransactionRepo extends JpaRepository<Transaction, TransactionKey> {
    Iterable<Transaction> findAllByProcessedTsBeforeOrderById(Instant processedAt);
}
