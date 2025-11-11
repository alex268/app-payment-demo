package tech.ydb.apps.repository;

import org.springframework.data.repository.CrudRepository;

import tech.ydb.apps.entity.SaldoConfig;
import tech.ydb.apps.entity.SaldoKey;

/**
 *
 * @author Aleksandr Gorshenin
 */
public interface SaldoConfigRepo extends CrudRepository<SaldoConfig, SaldoKey> {

}
