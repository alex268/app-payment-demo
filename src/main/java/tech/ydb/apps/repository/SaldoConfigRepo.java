package tech.ydb.apps.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import tech.ydb.apps.entity.SaldoConfig;
import tech.ydb.apps.entity.SaldoKey;

/**
 *
 * @author Aleksandr Gorshenin
 */
public interface SaldoConfigRepo extends JpaRepository<SaldoConfig, SaldoKey> {

}
