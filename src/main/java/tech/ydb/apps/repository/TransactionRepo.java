package tech.ydb.apps.repository;


import org.springframework.data.repository.CrudRepository;

import tech.ydb.apps.entity.Transaction;

/**
 *
 * @author Aleksandr Gorshenin
 */
public interface TransactionRepo extends CrudRepository<Transaction, String> {
}
