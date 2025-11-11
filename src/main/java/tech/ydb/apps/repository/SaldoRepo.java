package tech.ydb.apps.repository;

import org.springframework.data.repository.CrudRepository;

import tech.ydb.apps.entity.Saldo;
import tech.ydb.apps.entity.SaldoKey;

/**
 *
 * @author Aleksandr Gorshenin
 */
public interface SaldoRepo extends CrudRepository<Saldo, SaldoKey> {

}
