package tech.ydb.apps.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import tech.ydb.apps.entity.TransactionArchive;
import tech.ydb.apps.entity.TransactionArchiveKey;

/**
 *
 * @author Aleksandr Gorshenin
 */
public interface TransactionArchiveRepo extends JpaRepository<TransactionArchive, TransactionArchiveKey> {
}
